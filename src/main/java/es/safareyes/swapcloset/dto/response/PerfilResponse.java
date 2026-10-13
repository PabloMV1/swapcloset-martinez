package es.safareyes.swapcloset.dto.response;

import es.safareyes.swapcloset.entity.Rol;
import es.safareyes.swapcloset.entity.TallaCamiseta;

import java.time.LocalDate;
import java.util.List;

/** Mi propio perfil (incluye el email; nunca la contraseña). */
public record PerfilResponse(
        Long id,
        String username,
        String email,
        LocalDate fechaNacimiento,
        String ciudad,
        String biografia,
        TallaCamiseta tallaCamiseta,
        Integer tallaPantalon,
        Integer tallaCalzado,
        List<String> estilos,
        Rol rol) {
}
