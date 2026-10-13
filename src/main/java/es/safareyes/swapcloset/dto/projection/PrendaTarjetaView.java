package es.safareyes.swapcloset.dto.projection;

import es.safareyes.swapcloset.entity.EstadoPrenda;
import es.safareyes.swapcloset.entity.Modalidad;

import java.time.LocalDateTime;

/** Q5 (interfaz): tarjeta ligera para listados, sin cargar la entidad completa. */
public interface PrendaTarjetaView {
    Long getId();
    String getTitulo();
    String getTalla();
    EstadoPrenda getEstado();
    Modalidad getModalidad();
    String getCategoria();
    String getPropietaria();
    String getCiudad();
    LocalDateTime getCreadaEn();
}
