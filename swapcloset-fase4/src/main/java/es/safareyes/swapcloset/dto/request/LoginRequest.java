package es.safareyes.swapcloset.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "Nombre de usuario o email", example = "lucia_s")
        @NotBlank(message = "El usuario es obligatorio")
        String username,

        @Schema(example = "Password1!")
        @NotBlank(message = "La contraseña es obligatoria")
        String password) {
}
