package es.safareyes.swapcloset.dto.response;

/** Nunca incluye la clave ni la ruta del fichero en disco. */
public record FotoResponse(Long id, Integer orden, String url) {
}
