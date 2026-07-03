package com.macronnect.api_macronnect.articulo;

import com.macronnect.api_macronnect.articulo.dto.ArticuloRequest;
import com.macronnect.api_macronnect.articulo.dto.ArticuloResponse;
import com.macronnect.api_macronnect.categoria.Categoria;
import com.macronnect.api_macronnect.categoria.CategoriaRepository;
import com.macronnect.api_macronnect.common.dto.PagedResponse;
import com.macronnect.api_macronnect.common.exception.DuplicateResourceException;
import com.macronnect.api_macronnect.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticuloService {

    private final ArticuloRepository articuloRepository;
    private final CategoriaRepository categoriaRepository;

    public ArticuloService(ArticuloRepository articuloRepository,
                           CategoriaRepository categoriaRepository) {
        this.articuloRepository = articuloRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<ArticuloResponse> listar(Pageable pageable) {
        Page<Articulo> page = articuloRepository.findAll(pageable);
        List<ArticuloResponse> content = page.getContent().stream()
                .map(ArticuloResponse::fromEntity)
                .collect(Collectors.toList());
        return PagedResponse.of(page, content);
    }

    @Transactional(readOnly = true)
    public ArticuloResponse obtenerPorId(Long id) {
        return ArticuloResponse.fromEntity(buscarOFallar(id));
    }

    @Transactional
    public ArticuloResponse crear(ArticuloRequest request) {
        if (articuloRepository.existsByCodigo(request.getCodigo())) {
            throw new DuplicateResourceException("Artículo", "código", request.getCodigo());
        }
        Categoria categoria = buscarCategoria(request.getCategoriaId());

        Articulo articulo = new Articulo(
                request.getCodigo(),
                request.getNombre(),
                request.getDescripcion(),
                request.getPrecio(),
                request.getStock(),
                categoria);

        Articulo guardado = articuloRepository.save(articulo);
        return ArticuloResponse.fromEntity(guardado);
    }

    @Transactional
    public ArticuloResponse actualizar(Long id, ArticuloRequest request) {
        Articulo articulo = buscarOFallar(id);

        if (articuloRepository.existsByCodigoAndIdNot(request.getCodigo(), id)) {
            throw new DuplicateResourceException("Artículo", "código", request.getCodigo());
        }
        Categoria categoria = buscarCategoria(request.getCategoriaId());

        articulo.setCodigo(request.getCodigo());
        articulo.setNombre(request.getNombre());
        articulo.setDescripcion(request.getDescripcion());
        articulo.setPrecio(request.getPrecio());
        articulo.setStock(request.getStock());
        articulo.setCategoria(categoria);
        // sin save(): dirty checking dentro de la transacción
        return ArticuloResponse.fromEntity(articulo);
    }

    @Transactional
    public void eliminar(Long id) {
        buscarOFallar(id).setActivo(false); // soft delete
    }

    private Articulo buscarOFallar(Long id) {
        return articuloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo", "id", id));
    }

    private Categoria buscarCategoria(Long categoriaId) {
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", categoriaId));
    }
}