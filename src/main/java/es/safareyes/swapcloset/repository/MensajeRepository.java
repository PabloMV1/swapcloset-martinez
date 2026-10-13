package es.safareyes.swapcloset.repository;

import es.safareyes.swapcloset.entity.Mensaje;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    /** Q1 */
    List<Mensaje> findBySolicitudIdOrderByEnviadoEnAsc(Long solicitudId);

    /** Q6 + Q8: conversación paginada, con el autor cargado de una vez. Endpoint 14. */
    @EntityGraph(attributePaths = "autor")
    Page<Mensaje> findBySolicitudId(Long solicitudId, Pageable pageable);
}
