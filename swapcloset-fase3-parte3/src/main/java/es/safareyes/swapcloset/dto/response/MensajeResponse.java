package es.safareyes.swapcloset.dto.response;

import java.time.LocalDateTime;

public record MensajeResponse(Long id, Long solicitudId, UsuarioResumenResponse autor, String texto,
                              LocalDateTime enviadoEn) {
}
