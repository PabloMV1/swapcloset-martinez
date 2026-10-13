package es.safareyes.swapcloset.dto.projection;

/** Q5 (interfaz): perfil sugerido por estilos en común. */
public interface PerfilAfinView {
    Long getId();
    String getUsername();
    String getCiudad();
    Long getEstilosComunes();
}
