package es.safareyes.swapcloset.dto.request;

import es.safareyes.swapcloset.dto.validation.EdadMinima;
import es.safareyes.swapcloset.entity.TallaCamiseta;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.Set;

public record RegistroRequest(
        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Pattern(regexp = "^[A-Za-z0-9_.]{3,30}$",
                message = "El nombre de usuario tiene entre 3 y 30 caracteres: letras, números, '_' o '.'")
        String username,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 120, message = "El email admite como máximo 120 caracteres")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 72, message = "La contraseña tiene entre 8 y 72 caracteres")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).*$", message = "La contraseña debe incluir al menos una letra y un número")
        String password,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser pasada")
        @EdadMinima(anios = 16) // RN-01
        LocalDate fechaNacimiento,

        @Size(max = 80, message = "La ciudad admite como máximo 80 caracteres")
        String ciudad,

        TallaCamiseta tallaCamiseta,

        @Min(value = 34, message = "La talla de pantalón va de 34 a 50")
        @Max(value = 50, message = "La talla de pantalón va de 34 a 50")
        Integer tallaPantalon,

        @Min(value = 35, message = "La talla de calzado va de 35 a 46")
        @Max(value = 46, message = "La talla de calzado va de 35 a 46")
        Integer tallaCalzado,

        @NotEmpty(message = "Elige entre 1 y 5 estilos")
        @Size(max = 5, message = "Elige entre 1 y 5 estilos") // RN-02
        Set<Long> estilosIds) {
}
