package es.safareyes.swapcloset.dto.response;

import es.safareyes.swapcloset.entity.TallaCamiseta;

import java.util.List;

/** Perfil visible para el resto: sin email ni fecha de nacimiento. */
public record PerfilPublicoResponse(
        Long id,
        String username,
        String ciudad,
        String biografia,
        TallaCamiseta tallaCamiseta,
        Integer tallaPantalon,
        Integer tallaCalzado,
        List<String> estilos,
        Double reputacion,
        Long valoraciones,
        List<PrendaResumenResponse> prendas) {
}
