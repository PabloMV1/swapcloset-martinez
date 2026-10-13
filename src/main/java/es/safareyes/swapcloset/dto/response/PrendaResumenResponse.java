package es.safareyes.swapcloset.dto.response;

import es.safareyes.swapcloset.entity.EstadoPrenda;
import es.safareyes.swapcloset.entity.Modalidad;

import java.time.LocalDateTime;

public record PrendaResumenResponse(
        Long id,
        String titulo,
        String talla,
        EstadoPrenda estado,
        Modalidad modalidad,
        String categoria,
        String propietaria,
        String ciudad,
        Long fotoPortadaId,
        LocalDateTime creadaEn) {
}
