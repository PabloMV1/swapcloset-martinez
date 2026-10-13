package es.safareyes.swapcloset.controller;

import es.safareyes.swapcloset.dto.request.ValoracionCrearRequest;
import es.safareyes.swapcloset.dto.response.ValoracionResponse;
import es.safareyes.swapcloset.entity.Rol;
import es.safareyes.swapcloset.security.Actor;
import es.safareyes.swapcloset.service.ValoracionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** TEMPORAL (Fase 3): el actor llega por cabeceras; en la Fase 4 saldrá del token JWT. */
@RestController
@RequestMapping("/api/v1/solicitudes/{solicitudId}/valoraciones")
@RequiredArgsConstructor
@Tag(name = "Valoraciones", description = "Reputación entre personas tras un intercambio o préstamo")
public class ValoracionController {

    private final ValoracionService valoracionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Valorar a la otra persona (solicitud FINALIZADA, una vez por participante)")
    public ValoracionResponse valorar(
            @PathVariable Long solicitudId,
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestHeader(value = "X-Rol", defaultValue = "USUARIO") Rol rol,
            @Valid @RequestBody ValoracionCrearRequest request) {
        return valoracionService.valorar(new Actor(usuarioId, rol), solicitudId, request);
    }
}
