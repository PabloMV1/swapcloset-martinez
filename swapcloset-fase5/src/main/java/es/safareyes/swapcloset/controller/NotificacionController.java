package es.safareyes.swapcloset.controller;

import es.safareyes.swapcloset.dto.response.NotificacionResponse;
import es.safareyes.swapcloset.security.Actor;
import es.safareyes.swapcloset.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Avisos de solicitudes, mensajes, valoraciones y vencimientos")
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    @Operation(summary = "Mis notificaciones, de la más reciente a la más antigua",
            description = "Filtro opcional ?leida=true|false.")
    public PagedModel<NotificacionResponse> listar(
            @AuthenticationPrincipal Actor actor,
            @RequestParam(required = false) Boolean leida,
            @ParameterObject @PageableDefault(size = 20, sort = "creadaEn", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return new PagedModel<>(notificacionService.listar(actor, leida, pageable));
    }

    @PatchMapping("/{id}/leida")
    @Operation(summary = "Marcar una notificación propia como leída")
    public NotificacionResponse marcarLeida(@PathVariable Long id, @AuthenticationPrincipal Actor actor) {
        return notificacionService.marcarLeida(actor, id);
    }
}
