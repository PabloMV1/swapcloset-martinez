package es.safareyes.swapcloset.controller;

import es.safareyes.swapcloset.dto.request.MensajeCrearRequest;
import es.safareyes.swapcloset.dto.response.MensajeResponse;
import es.safareyes.swapcloset.entity.Rol;
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
import org.springframework.web.bind.annotation.*;

/** TEMPORAL (Fase 3): el actor llega por cabeceras; en la Fase 4 saldrá del token JWT. */
@RestController
@RequestMapping("/api/v1/solicitudes/{solicitudId}/mensajes")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Mensajes entre las dos personas de una solicitud")
public class MensajeController {

    private final MensajeService mensajeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Enviar un mensaje (solo los dos participantes)")
    public MensajeResponse enviar(
            @PathVariable Long solicitudId,
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestHeader(value = "X-Rol", defaultValue = "USUARIO") Rol rol,
            @Valid @RequestBody MensajeCrearRequest request) {
        return mensajeService.enviar(new Actor(usuarioId, rol), solicitudId, request);
    }

    @GetMapping
    @Operation(summary = "Conversación paginada, del mensaje más antiguo al más reciente")
    public PagedModel<MensajeResponse> listar(
            @PathVariable Long solicitudId,
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestHeader(value = "X-Rol", defaultValue = "USUARIO") Rol rol,
            @ParameterObject @PageableDefault(size = 20, sort = "enviadoEn", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return new PagedModel<>(mensajeService.listar(new Actor(usuarioId, rol), solicitudId, pageable));
    }
}
