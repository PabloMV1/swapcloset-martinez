package es.safareyes.swapcloset.controller;

import es.safareyes.swapcloset.dto.request.MensajeCrearRequest;
import es.safareyes.swapcloset.dto.response.MensajeResponse;
import es.safareyes.swapcloset.security.Actor;
import es.safareyes.swapcloset.service.MensajeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/solicitudes/{solicitudId}/mensajes")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Mensajes entre las dos personas de una solicitud")
public class MensajeController {

    private final MensajeService mensajeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Enviar un mensaje (solo los dos participantes)")
    @PreAuthorize("@seguridad.esParticipante(#solicitudId, #actor)")
    public MensajeResponse enviar(
            @PathVariable Long solicitudId,
            @AuthenticationPrincipal Actor actor,
            @Valid @RequestBody MensajeCrearRequest request) {
        return mensajeService.enviar(actor, solicitudId, request);
    }

    @GetMapping
    @Operation(summary = "Conversación paginada, del mensaje más antiguo al más reciente")
    @PreAuthorize("@seguridad.esParticipante(#solicitudId, #actor)")
    public PagedModel<MensajeResponse> listar(
            @PathVariable Long solicitudId,
            @AuthenticationPrincipal Actor actor,
            @ParameterObject @PageableDefault(size = 20, sort = "enviadoEn", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return new PagedModel<>(mensajeService.listar(actor, solicitudId, pageable));
    }
}
