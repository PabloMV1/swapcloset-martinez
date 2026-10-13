package es.safareyes.swapcloset.dto.request;

import es.safareyes.swapcloset.entity.TipoSolicitud;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SolicitudCrearRequest(
        @NotNull(message = "El tipo es obligatorio (INTERCAMBIO o PRESTAMO)")
        TipoSolicitud tipo,

        @NotNull(message = "La prenda solicitada es obligatoria")
        Long prendaId,

        @Schema(description = "Solo en intercambios: la prenda que ofreces a cambio")
        Long prendaOfrecidaId,

        @Schema(description = "Solo en préstamos")
        LocalDate fechaInicio,

        @Schema(description = "Solo en préstamos (máximo 30 días desde el inicio)")
        LocalDate fechaFin) {
}
