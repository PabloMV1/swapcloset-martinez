package es.safareyes.swapcloset.dto.response;

import es.safareyes.swapcloset.entity.EstadoSolicitud;
import es.safareyes.swapcloset.entity.TipoSolicitud;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SolicitudResponse(
        Long id,
        TipoSolicitud tipo,
        EstadoSolicitud estado,
        PrendaBasicaResponse prenda,
        UsuarioResumenResponse solicitante,
        UsuarioResumenResponse propietaria,
        PrendaBasicaResponse prendaOfrecida,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        LocalDateTime creadaEn,
        LocalDateTime actualizadaEn) {
}
