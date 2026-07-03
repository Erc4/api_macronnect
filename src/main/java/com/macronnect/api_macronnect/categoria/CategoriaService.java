package com.macronnect.api_macronnect.categoria;

import com.macronnect.api_macronnect.categoria.dto.CategoriaRequest;
import com.macronnect.api_macronnect.categoria.dto.CategoriaResponse;
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
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<CategoriaResponse> listar(Pageable pageable) {
        Page<Categoria> page = categoriaRepository.findAll(pageable);
        List<CategoriaResponse> content = page.getContent().stream()
                .map(CategoriaResponse::fromEntity)
                .collect(Collectors.toList());
        return PagedResponse.of(page, content);
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtenerPorId(Long id) {
        Categoria categoria = buscarOFallar(id);
        return CategoriaResponse.fromEntity(categoria);
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        if (categoriaRepository.existsByNombre(request.getNombre())) {
            throw new DuplicateResourceException("Categoría", "nombre", request.getNombre());
        }
        Categoria categoria = new Categoria(request.getNombre(), request.getDescripcion());
        Categoria guardada = categoriaRepository.save(categoria);
        return CategoriaResponse.fromEntity(guardada);
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria categoria = buscarOFallar(id);

        if (categoriaRepository.existsByNombreAndIdNot(request.getNombre(), id)) {
            throw new DuplicateResourceException("Categoría", "nombre", request.getNombre());
        }

        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());
        // No hace falta save(): la entidad está "managed" dentro de la transacción (dirty checking)
        return CategoriaResponse.fromEntity(categoria);
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = buscarOFallar(id);
        categoria.setActivo(false); // soft delete
    }

    private Categoria buscarOFallar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", id));
    }
}