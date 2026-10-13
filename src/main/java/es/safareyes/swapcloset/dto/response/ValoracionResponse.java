package es.safareyes.swapcloset.dto.response;

import java.time.LocalDateTime;

public record ValoracionResponse(Long id, Long solicitudId, UsuarioResumenResponse autor,
                                 UsuarioResumenResponse valorado, Integer puntuacion, String comentario,
                                 LocalDateTime creadaEn) {
}
