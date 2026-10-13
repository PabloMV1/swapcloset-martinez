package es.safareyes.swapcloset.repository;

import es.safareyes.swapcloset.entity.Estilo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstiloRepository extends JpaRepository<Estilo, Long> {

    /** Q1 */
    Optional<Estilo> findByNombreIgnoreCase(String nombre);
}
