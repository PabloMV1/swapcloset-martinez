package es.safareyes.swapcloset.dto.request;

import es.safareyes.swapcloset.entity.EstadoPrenda;
import es.safareyes.swapcloset.entity.Modalidad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PrendaCrearRequest(
        @NotBlank @Size(max = 100) String titulo,
        @Size(max = 1000) String descripcion,
        @Size(max = 60) String marca,
        @NotNull Long categoriaId,
        @NotNull Long estiloId,
        @NotBlank @Size(max = 10) String talla,
        @NotNull EstadoPrenda estado,
        @NotNull Modalidad modalidad) {
}
