package es.safareyes.swapcloset.dto.projection;

/** Resultado de la consulta nativa de actividad por mes y categoría. */
public interface ActividadMensualView {
    String getMes();
    String getCategoria();
    Long getIntercambios();
    Long getPrestamos();
}
