package es.safareyes.swapcloset.controller;

import es.safareyes.swapcloset.dto.request.SolicitudAccionRequest;
import es.safareyes.swapcloset.dto.request.SolicitudCrearRequest;
import es.safareyes.swapcloset.dto.request.VistaSolicitudes;
import es.safareyes.swapcloset.dto.response.SolicitudResponse;
import es.safareyes.swapcloset.entity.EstadoSolicitud;
import es.safareyes.swapcloset.security.Actor;
import es.safareyes.swapcloset.service.SolicitudService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/solicitudes")
@RequiredArgsConstructor
@Tag(name = "Solicitudes", description = "Intercambios y préstamos entre personas")
public class SolicitudController {

    private final SolicitudService solicitudService;

    @PostMapping
    @Operation(summary = "Solicitar una prenda en intercambio o préstamo",
            description = "Intercambio: prendaOfrecidaId obligatoria. Préstamo: fechaInicio y fechaFin (máximo 30 días).")
    public ResponseEntity<SolicitudResponse> crear(
            @AuthenticationPrincipal Actor actor,
            @Valid @RequestBody SolicitudCrearRequest request) {
        SolicitudResponse creada = solicitudService.crear(actor, request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(creada.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @GetMapping
    @Operation(summary = "Mis solicitudes enviadas o recibidas, filtrables por estado y paginadas")
    public PagedModel<SolicitudResponse> listar(
            @AuthenticationPrincipal Actor actor,
            @RequestParam(defaultValue = "ENVIADAS") VistaSolicitudes vista,
            @RequestParam(required = false) EstadoSolicitud estado,
            @ParameterObject @PageableDefault(size = 20, sort = "creadaEn", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return new PagedModel<>(solicitudService.listar(actor, vista, estado, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una solicitud (solo sus dos participantes)")
    @PreAuthorize("@seguridad.esParticipante(#id, #actor)")
    public SolicitudResponse obtener(
            @PathVariable Long id,
            @AuthenticationPrincipal Actor actor) {
        return solicitudService.obtener(actor, id);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Aceptar, rechazar, cancelar o finalizar una solicitud",
            description = "ACEPTAR y RECHAZAR: la propietaria, desde PENDIENTE. CANCELAR: quien solicitó, desde PENDIENTE. "
                    + "FINALIZAR: desde ACEPTADA o RETRASADA (préstamo: la propietaria; intercambio: cualquiera de las dos).")
    @PreAuthorize("@seguridad.esParticipante(#id, #actor)")
    public SolicitudResponse cambiarEstado(
            @PathVariable Long id,
            @AuthenticationPrincipal Actor actor,
            @Valid @RequestBody SolicitudAccionRequest request) {
        return solicitudService.cambiarEstado(actor, id, request.accion());
    }
}
