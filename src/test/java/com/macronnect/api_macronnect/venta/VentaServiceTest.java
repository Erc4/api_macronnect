package com.macronnect.api_macronnect.venta;

import com.macronnect.api_macronnect.articulo.Articulo;
import com.macronnect.api_macronnect.articulo.ArticuloRepository;
import com.macronnect.api_macronnect.categoria.Categoria;
import com.macronnect.api_macronnect.cliente.Cliente;
import com.macronnect.api_macronnect.cliente.ClienteRepository;
import com.macronnect.api_macronnect.common.exception.BusinessException;
import com.macronnect.api_macronnect.common.exception.InsufficientStockException;
import com.macronnect.api_macronnect.common.exception.ResourceNotFoundException;
import com.macronnect.api_macronnect.venta.dto.DetalleVentaRequest;
import com.macronnect.api_macronnect.venta.dto.VentaRequest;
import com.macronnect.api_macronnect.venta.dto.VentaResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ArticuloRepository articuloRepository;

    @Mock
    private FolioSequenceRepository folioSequenceRepository;

    @InjectMocks
    private VentaService ventaService;

    private static final String SECUENCIA_FOLIO = "venta_folio";

    // ---------- Helpers para construir datos de prueba ----------

    private Cliente cliente(Long id, String nombre) {
        Cliente c = new Cliente(nombre, nombre + "@correo.com", null, null);
        c.setId(id);
        return c;
    }

    private Articulo articulo(Long id, String codigo, String nombre,
        BigDecimal precio, int stock) {
        Categoria cat = new Categoria("Categoría", "desc");
        cat.setId(1L);
        Articulo a = new Articulo(codigo, nombre, "desc", precio, stock, cat);
        a.setId(id);
        return a;
    }

    private DetalleVentaRequest lineaRequest(Long articuloId, int cantidad) {
        DetalleVentaRequest d = new DetalleVentaRequest();
        d.setArticuloId(articuloId);
        d.setCantidad(cantidad);
        return d;
    }

    // ---------- Tests de registrar() ----------

    @Test
    @DisplayName("registrar: calcula el total sumando los subtotales de las líneas")
    void registrar_calculaTotalCorrectamente() {
        Cliente cliente = cliente(1L, "Juan Pérez");
        Articulo art1 = articulo(10L, "A-1", "Artículo 1", new BigDecimal("100.00"), 5);
        Articulo art2 = articulo(20L, "A-2", "Artículo 2", new BigDecimal("50.50"), 3);

        VentaRequest request = new VentaRequest();
        request.setClienteId(1L);
        request.setDetalles(Arrays.asList(
                lineaRequest(10L, 2),   // 100.00 * 2 = 200.00
                lineaRequest(20L, 1)));  // 50.50 * 1 =  50.50  -> total 250.50

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(articuloRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(art1));
        when(articuloRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(art2));
        when(folioSequenceRepository.findBySeqNameForUpdate(SECUENCIA_FOLIO))
                .thenReturn(Optional.of(new FolioSequence(SECUENCIA_FOLIO, 1000L)));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        VentaResponse response = ventaService.registrar(request);

        // compareTo == 0 evita fallos por escala (250.50 vs 250.5)
        assertEquals(0, response.getTotal().compareTo(new BigDecimal("250.50")));
        assertEquals(2, response.getDetalles().size());
        assertEquals("ACTIVA", response.getEstado());
    }

    @Test
    @DisplayName("registrar: descuenta el stock de cada artículo vendido")
    void registrar_descuentaStock() {
        Cliente cliente = cliente(1L, "Juan");
        Articulo art = articulo(10L, "A-1", "Artículo 1", new BigDecimal("100.00"), 5);

        VentaRequest request = new VentaRequest();
        request.setClienteId(1L);
        request.setDetalles(Collections.singletonList(lineaRequest(10L, 3)));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(articuloRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(art));
        when(folioSequenceRepository.findBySeqNameForUpdate(SECUENCIA_FOLIO))
                .thenReturn(Optional.of(new FolioSequence(SECUENCIA_FOLIO, 1000L)));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        ventaService.registrar(request);

        assertEquals(2, art.getStock()); // 5 - 3 = 2
    }

    @Test
    @DisplayName("registrar: asigna el folio incremental (contador + 1)")
    void registrar_asignaFolioIncremental() {
        Cliente cliente = cliente(1L, "Juan");
        Articulo art = articulo(10L, "A-1", "Artículo 1", new BigDecimal("10.00"), 5);

        VentaRequest request = new VentaRequest();
        request.setClienteId(1L);
        request.setDetalles(Collections.singletonList(lineaRequest(10L, 1)));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(articuloRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(art));
        when(folioSequenceRepository.findBySeqNameForUpdate(SECUENCIA_FOLIO))
                .thenReturn(Optional.of(new FolioSequence(SECUENCIA_FOLIO, 1000L)));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        VentaResponse response = ventaService.registrar(request);

        assertEquals(1001L, response.getFolio()); // 1000 + 1
    }

    @Test
    @DisplayName("registrar: lanza InsufficientStockException si no hay stock suficiente")
    void registrar_stockInsuficiente_lanzaExcepcion() {
        Cliente cliente = cliente(1L, "Juan");
        Articulo art = articulo(10L, "A-1", "Artículo 1", new BigDecimal("100.00"), 1);

        VentaRequest request = new VentaRequest();
        request.setClienteId(1L);
        request.setDetalles(Collections.singletonList(lineaRequest(10L, 5))); // pide 5, hay 1

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(articuloRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(art));

        assertThrows(InsufficientStockException.class,
                () -> ventaService.registrar(request));

        // El stock NO debe haberse tocado, y la venta NO debe guardarse
        assertEquals(1, art.getStock());
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    @DisplayName("registrar: lanza ResourceNotFoundException si el cliente no existe")
    void registrar_clienteInexistente_lanzaExcepcion() {
        VentaRequest request = new VentaRequest();
        request.setClienteId(99L);
        request.setDetalles(Collections.singletonList(lineaRequest(10L, 1)));

        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> ventaService.registrar(request));

        verify(articuloRepository, never()).findByIdForUpdate(anyLong());
    }

    @Test
    @DisplayName("registrar: lanza ResourceNotFoundException si un artículo no existe")
    void registrar_articuloInexistente_lanzaExcepcion() {
        Cliente cliente = cliente(1L, "Juan");

        VentaRequest request = new VentaRequest();
        request.setClienteId(1L);
        request.setDetalles(Collections.singletonList(lineaRequest(999L, 1)));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(articuloRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> ventaService.registrar(request));
    }

    // ---------- Tests de cancelar() ----------

    @Test
    @DisplayName("cancelar: repone el stock y cambia el estado a CANCELADA")
    void cancelar_reponeStockYCambiaEstado() {
        Cliente cliente = cliente(1L, "Juan");
        // stock 3 representa el stock que quedó tras vender 2
        Articulo art = articulo(10L, "A-1", "Artículo 1", new BigDecimal("100.00"), 3);

        Venta venta = new Venta(cliente);
        venta.setId(1L);
        venta.setFolio(1001L);
        venta.agregarDetalle(new DetalleVenta(art, 2, art.getPrecio()));

        when(ventaRepository.findByIdConDetalles(1L)).thenReturn(Optional.of(venta));
        when(articuloRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(art));

        VentaResponse response = ventaService.cancelar(1L);

        assertEquals("CANCELADA", response.getEstado());
        assertEquals(5, art.getStock()); // 3 + 2 repuestos
    }

    @Test
    @DisplayName("cancelar: lanza BusinessException si la venta ya está cancelada")
    void cancelar_ventaYaCancelada_lanzaExcepcion() {
        Cliente cliente = cliente(1L, "Juan");
        Venta venta = new Venta(cliente);
        venta.setId(2L);
        venta.setFolio(1002L);
        venta.setEstado(EstadoVenta.CANCELADA);

        when(ventaRepository.findByIdConDetalles(2L)).thenReturn(Optional.of(venta));

        assertThrows(BusinessException.class, () -> ventaService.cancelar(2L));

        // No debe intentar reponer stock de una venta ya cancelada
        verify(articuloRepository, never()).findByIdForUpdate(anyLong());
    }
}