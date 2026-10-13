package es.safareyes.swapcloset.dto.response;

import es.safareyes.swapcloset.entity.TipoNotificacion;

import java.time.LocalDateTime;

public record NotificacionResponse(Long id, TipoNotificacion tipo, String mensaje, String recurso, Long recursoId,
                                   boolean leida, LocalDateTime creadaEn) {
}
