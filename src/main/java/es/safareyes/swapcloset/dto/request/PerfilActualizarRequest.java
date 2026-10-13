package es.safareyes.swapcloset.dto.request;

import es.safareyes.swapcloset.entity.TallaCamiseta;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.Set;

/** Campos opcionales: solo se cambian los informados. Tallas válidas según RN-02. */
public record PerfilActualizarRequest(
        @Size(max = 80) String ciudad,
        @Size(max = 500) String biografia,
        TallaCamiseta tallaCamiseta,
        @Min(34) @Max(50) Integer tallaPantalon,
        @Min(35) @Max(46) Integer tallaCalzado,
        @Size(min = 1, max = 5, message = "Debes indicar entre 1 y 5 estilos") Set<Long> estilosIds) {
}
