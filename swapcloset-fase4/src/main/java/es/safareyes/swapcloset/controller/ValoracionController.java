package es.safareyes.swapcloset.controller;

import es.safareyes.swapcloset.dto.request.ValoracionCrearRequest;
import es.safareyes.swapcloset.dto.response.ValoracionResponse;
import es.safareyes.swapcloset.security.Actor;
import es.safareyes.swapcloset.service.ValoracionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/solicitudes/{solicitudId}/valoraciones")
@RequiredArgsConstructor
@Tag(name = "Valoraciones", description = "Reputación entre personas tras un intercambio o préstamo")
public class ValoracionController {

    private final ValoracionService valoracionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Valorar a la otra persona (solicitud FINALIZADA, una vez por participante)")
    @PreAuthorize("@seguridad.esParticipante(#solicitudId, #actor)")
    public ValoracionResponse valorar(
            @PathVariable Long solicitudId,
            @AuthenticationPrincipal Actor actor,
            @Valid @RequestBody ValoracionCrearRequest request) {
        return valoracionService.valorar(actor, solicitudId, request);
    }
}
