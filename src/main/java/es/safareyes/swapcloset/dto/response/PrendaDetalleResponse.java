package es.safareyes.swapcloset.dto.response;

import es.safareyes.swapcloset.entity.Disponibilidad;
import es.safareyes.swapcloset.entity.EstadoPrenda;
import es.safareyes.swapcloset.entity.Modalidad;

import java.time.LocalDateTime;
import java.util.List;

public record PrendaDetalleResponse(
        Long id,
        String titulo,
        String descripcion,
        String marca,
        String talla,
        EstadoPrenda estado,
        Modalidad modalidad,
        Disponibilidad disponibilidad,
        Long categoriaId,
        String categoria,
        Long estiloId,
        String estilo,
        PropietariaResponse propietaria,
        List<FotoResponse> fotos,
        LocalDateTime creadaEn) {
}
