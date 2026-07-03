package com.macronnect.api_macronnect.articulo;

import com.macronnect.api_macronnect.articulo.dto.ArticuloRequest;
import com.macronnect.api_macronnect.articulo.dto.ArticuloResponse;
import com.macronnect.api_macronnect.common.dto.PagedResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/articulos")
public class ArticuloController {

    private final ArticuloService articuloService;

    public ArticuloController(ArticuloService articuloService) {
        this.articuloService = articuloService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ArticuloResponse>> listar(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return ResponseEntity.ok(articuloService.listar(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticuloResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(articuloService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<ArticuloResponse> crear(@Valid @RequestBody ArticuloRequest request) {
        ArticuloResponse creado = articuloService.crear(request);
        URI location = URI.create("/articulos/" + creado.getId());
        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArticuloResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ArticuloRequest request) {
        return ResponseEntity.ok(articuloService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        articuloService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}