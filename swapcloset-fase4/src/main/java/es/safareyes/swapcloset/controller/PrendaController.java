package es.safareyes.swapcloset.controller;

import es.safareyes.swapcloset.dto.request.PrendaActualizarRequest;
import es.safareyes.swapcloset.dto.request.PrendaCrearRequest;
import es.safareyes.swapcloset.dto.response.PrendaDetalleResponse;
import es.safareyes.swapcloset.dto.response.PrendaResumenResponse;
import es.safareyes.swapcloset.entity.EstadoPrenda;
import es.safareyes.swapcloset.entity.Modalidad;
import es.safareyes.swapcloset.security.Actor;
import es.safareyes.swapcloset.service.PrendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/prendas")
@RequiredArgsConstructor
@Tag(name = "Prendas", description = "Publicación, búsqueda y retirada de prendas")
public class PrendaController {

    private final PrendaService prendaService;

    @GetMapping
    @Operation(summary = "Búsqueda paginada con filtros",
            description = "Filtros opcionales y combinables. Orden por defecto: lo último publicado primero.")
    public PagedModel<PrendaResumenResponse> buscar(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String talla,
            @RequestParam(required = false) EstadoPrenda estado,
            @RequestParam(required = false) Long estiloId,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) Modalidad modalidad,
            @ParameterObject @PageableDefault(size = 20, sort = "creadaEn", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return new PagedModel<>(prendaService.buscar(categoriaId, talla, estado, estiloId, ciudad, modalidad, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una prenda con sus fotos y la reputación de su propietaria")
    public PrendaDetalleResponse obtener(
            @PathVariable Long id,
            @AuthenticationPrincipal Actor actor) {
        // sin token (catálogo público) el actor es null
        return prendaService.obtener(id, actor);
    }

    @PostMapping
    @Operation(summary = "Publicar una prenda (queda en BORRADOR hasta que se suba la primera foto)")
    public ResponseEntity<PrendaDetalleResponse> crear(
            @AuthenticationPrincipal Actor actor,
            @Valid @RequestBody PrendaCrearRequest request) {
        PrendaDetalleResponse creada = prendaService.crear(actor, request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(creada.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar una prenda propia (solo cambian los campos informados)")
    public PrendaDetalleResponse actualizar(
            @PathVariable Long id,
            @AuthenticationPrincipal Actor actor,
            @Valid @RequestBody PrendaActualizarRequest request) {
        return prendaService.actualizar(actor, id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Retirar una prenda (baja lógica)")
    public ResponseEntity<Void> retirar(
            @PathVariable Long id,
            @AuthenticationPrincipal Actor actor) {
        prendaService.retirar(actor, id);
        return ResponseEntity.noContent().build();
    }
}
