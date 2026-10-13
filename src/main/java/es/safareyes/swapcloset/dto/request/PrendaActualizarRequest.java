package es.safareyes.swapcloset.dto.request;

import es.safareyes.swapcloset.entity.EstadoPrenda;
import es.safareyes.swapcloset.entity.Modalidad;
import jakarta.validation.constraints.Size;

/** Todos los campos son opcionales: solo se modifican los que llegan informados. */
public record PrendaActualizarRequest(
        @Size(min = 1, max = 100) String titulo,
        @Size(max = 1000) String descripcion,
        @Size(max = 60) String marca,
        Long categoriaId,
        Long estiloId,
        @Size(min = 1, max = 10) String talla,
        EstadoPrenda estado,
        Modalidad modalidad) {
}
