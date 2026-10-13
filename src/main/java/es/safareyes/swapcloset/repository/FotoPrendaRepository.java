package es.safareyes.swapcloset.repository;

import es.safareyes.swapcloset.dto.projection.FotoPortadaView;
import es.safareyes.swapcloset.entity.FotoPrenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FotoPrendaRepository extends JpaRepository<FotoPrenda, Long> {

    /** Q1 (RN-03: máximo 5 fotos) */
    long countByPrendaId(Long prendaId);

    /** Q1 */
    List<FotoPrenda> findByPrendaIdOrderByOrdenAsc(Long prendaId);

    /** Q1 (findFirst / Top): la última foto, para calcular el siguiente orden. */
    Optional<FotoPrenda> findFirstByPrendaIdOrderByOrdenDesc(Long prendaId);

    /** Q5 (interfaz): la foto de portada de varias prendas en UNA consulta (evita N+1 en el listado). */
    @Query("select f.prenda.id as prendaId, f.id as id from FotoPrenda f where f.prenda.id in :ids and f.orden = 1")
    List<FotoPortadaView> findPortadas(@Param("ids") Collection<Long> ids);
}
