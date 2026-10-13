package es.safareyes.swapcloset.dto.request;

import jakarta.validation.constraints.NotNull;

public record SolicitudAccionRequest(
        @NotNull(message = "La acción es obligatoria (ACEPTAR, RECHAZAR, CANCELAR o FINALIZAR)")
        AccionSolicitud accion) {
}
