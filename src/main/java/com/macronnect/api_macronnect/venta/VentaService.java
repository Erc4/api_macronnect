package com.macronnect.api_macronnect.venta;

import com.macronnect.api_macronnect.articulo.Articulo;
import com.macronnect.api_macronnect.articulo.ArticuloRepository;
import com.macronnect.api_macronnect.cliente.Cliente;
import com.macronnect.api_macronnect.cliente.ClienteRepository;
import com.macronnect.api_macronnect.common.dto.PagedResponse;
import com.macronnect.api_macronnect.common.exception.BusinessException;
import com.macronnect.api_macronnect.common.exception.InsufficientStockException;
import com.macronnect.api_macronnect.common.exception.ResourceNotFoundException;
import com.macronnect.api_macronnect.venta.dto.VentaRequest;
import com.macronnect.api_macronnect.venta.dto.VentaResponse;
import com.macronnect.api_macronnect.venta.EstadoVenta;
import com.macronnect.api_macronnect.venta.dto.DetalleVentaRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final ArticuloRepository articuloRepository;
    private final FolioSequenceRepository folioSequenceRepository;

    private static final String SECUENCIA_FOLIO = "venta_folio";

    public VentaService(VentaRepository ventaRepository,
                        ClienteRepository clienteRepository,
                        ArticuloRepository articuloRepository,
                        FolioSequenceRepository folioSequenceRepository) {
        this.ventaRepository = ventaRepository;
        this.clienteRepository = clienteRepository;
        this.articuloRepository = articuloRepository;
        this.folioSequenceRepository = folioSequenceRepository;
    }

    @Transactional
    public VentaResponse registrar(VentaRequest request) {
        // 1. Validar que el cliente exista
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente", "id", request.getClienteId()));

        // 2. Crear la venta (cabecera)
        Venta venta = new Venta(cliente);

        // 3. Procesar cada línea: bloquear artículo, validar stock, descontar, armar detalle
        for (DetalleVentaRequest linea : request.getDetalles()) {
            // Bloqueo pesimista de la fila del artículo (SELECT ... FOR UPDATE)
            Articulo articulo = articuloRepository.findByIdForUpdate(linea.getArticuloId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Artículo", "id", linea.getArticuloId()));

            // Validar stock disponible
            if (articulo.getStock() < linea.getCantidad()) {
                throw new InsufficientStockException(
                        articulo.getNombre(), articulo.getStock(), linea.getCantidad());
            }

            // Descontar stock
            articulo.setStock(articulo.getStock() - linea.getCantidad());

            // Crear el detalle con snapshot del precio actual del artículo
            DetalleVenta detalle = new DetalleVenta(
                    articulo, linea.getCantidad(), articulo.getPrecio());

            // Agregar a la venta (fija relación bidireccional y recalcula total)
            venta.agregarDetalle(detalle);
        }

        // Generar el folio de forma segura (contador bloqueado con FOR UPDATE)
        venta.setFolio(siguienteFolio());


        // 4. Persistir el agregado completo (cascade guarda los detalles; el folio se genera aquí)
        Venta guardada = ventaRepository.save(venta);

        return VentaResponse.fromEntity(guardada);
    }

    @Transactional(readOnly = true)
    public PagedResponse<VentaResponse> listar(Pageable pageable) {
        Page<Venta> page = ventaRepository.findAll(pageable);
        List<VentaResponse> content = page.getContent().stream()
                .map(VentaResponse::fromEntityResumen)
                .collect(Collectors.toList());
        return PagedResponse.of(page, content);
    }

    @Transactional(readOnly = true)
    public PagedResponse<VentaResponse> buscar(Long clienteId, LocalDateTime desde,
        LocalDateTime hasta, Pageable pageable) {
        Page<Venta> page = ventaRepository.buscarConFiltros(clienteId, desde, hasta, pageable);
        List<VentaResponse> content = page.getContent().stream()
                .map(VentaResponse::fromEntityResumen)
                .collect(Collectors.toList());
        return PagedResponse.of(page, content);
    }

    @Transactional(readOnly = true)
    public VentaResponse obtenerDetalle(Long id) {
        Venta venta = ventaRepository.findByIdConDetalles(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta", "id", id));
        return VentaResponse.fromEntity(venta);
    }

    @Transactional
    public VentaResponse cancelar(Long id) {
        // Cargamos la venta con sus detalles para poder reponer el stock
        Venta venta = ventaRepository.findByIdConDetalles(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta", "id", id));

        // No se puede cancelar una venta ya cancelada
        if (venta.getEstado() == EstadoVenta.CANCELADA) {
            throw new BusinessException("La venta con folio " + venta.getFolio()
                    + " ya está cancelada");
        }

        // Reponer el stock de cada línea (con bloqueo pesimista por seguridad)
        for (DetalleVenta detalle : venta.getDetalles()) {
            Articulo articulo = articuloRepository.findByIdForUpdate(detalle.getArticulo().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Artículo", "id", detalle.getArticulo().getId()));
            articulo.setStock(articulo.getStock() + detalle.getCantidad());
        }

        // Cambiar estado (dirty checking persiste el cambio)
        venta.setEstado(EstadoVenta.CANCELADA);

        return VentaResponse.fromEntity(venta);
    }

    /**
     * Devuelve el siguiente folio incremental. Bloquea la fila del contador con
     * PESSIMISTIC_WRITE para que dos ventas concurrentes no repitan folio.
     * Corre dentro de la transacción de la venta (heredada de registrar()).
     */
    private Long siguienteFolio() {
        FolioSequence secuencia = folioSequenceRepository
                .findBySeqNameForUpdate(SECUENCIA_FOLIO)
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontró la secuencia de folios '" + SECUENCIA_FOLIO + "'. "
                        + "¿Se ejecutó el seeder?"));
        Long siguiente = secuencia.getSeqValue() + 1;
        secuencia.setSeqValue(siguiente);
        return siguiente;
    }
}