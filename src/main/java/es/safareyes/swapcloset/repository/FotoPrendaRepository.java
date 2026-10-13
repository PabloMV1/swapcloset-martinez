package es.safareyes.swapcloset.repository;

import es.safareyes.swapcloset.entity.FotoPrenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FotoPrendaRepository extends JpaRepository<FotoPrenda, Long> {

    /** Q1 (RN-03: máximo 5 fotos) */
    long countByPrendaId(Long prendaId);

    /** Q1 */
    List<FotoPrenda> findByPrendaIdOrderByOrdenAsc(Long prendaId);

    /** Q1 (findFirst / Top): la última foto, para calcular el siguiente orden. */
    Optional<FotoPrenda> findFirstByPrendaIdOrderByOrdenDesc(Long prendaId);
}
