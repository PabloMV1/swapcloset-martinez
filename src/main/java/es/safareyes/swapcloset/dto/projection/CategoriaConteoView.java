package es.safareyes.swapcloset.dto.projection;

/** Q5 (interfaz): categoría con su número de prendas disponibles. */
public interface CategoriaConteoView {
    Long getId();
    String getNombre();
    Long getDisponibles();
}
