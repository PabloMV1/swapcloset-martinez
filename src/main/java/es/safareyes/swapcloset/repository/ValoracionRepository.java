package es.safareyes.swapcloset.repository;

import es.safareyes.swapcloset.entity.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {

    /** Q1 (RN-09: una valoración por participante y solicitud) */
    boolean existsBySolicitudIdAndAutorId(Long solicitudId, Long autorId);

    /** Q1 */
    long countByValoradoId(Long valoradoId);

    /** Q1 */
    List<Valoracion> findByValoradoIdOrderByCreadaEnDesc(Long valoradoId);

    /** Q2 (JPQL con función de agregado AVG). Devuelve null si aún no tiene valoraciones. */
    @Query("select avg(v.puntuacion) from Valoracion v where v.valorado.id = :usuarioId")
    Double reputacionMedia(@Param("usuarioId") Long usuarioId);
}
