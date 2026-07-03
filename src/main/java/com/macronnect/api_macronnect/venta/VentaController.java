package com.macronnect.api_macronnect.venta;

import com.macronnect.api_macronnect.common.dto.PagedResponse;
import com.macronnect.api_macronnect.venta.dto.VentaRequest;
import com.macronnect.api_macronnect.venta.dto.VentaResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @PostMapping
    public ResponseEntity<VentaResponse> registrar(@Valid @RequestBody VentaRequest request) {
        VentaResponse creada = ventaService.registrar(request);
        URI location = URI.create("/ventas/" + creada.getId());
        return ResponseEntity.created(location).body(creada);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<VentaResponse>> listar(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable) {
        // Si no mandan ningún filtro, buscarConFiltros los ignora (todos null) y lista todo
        return ResponseEntity.ok(ventaService.buscar(clienteId, desde, hasta, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponse> obtenerDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.obtenerDetalle(id));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<VentaResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.cancelar(id));
    }
}