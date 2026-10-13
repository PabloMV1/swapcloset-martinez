package es.safareyes.swapcloset.repository;

import es.safareyes.swapcloset.dto.projection.PrendaTarjetaView;
import es.safareyes.swapcloset.entity.Disponibilidad;
import es.safareyes.swapcloset.entity.Prenda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PrendaRepository extends JpaRepository<Prenda, Long>, JpaSpecificationExecutor<Prenda> {

    /** Q1 */
    List<Prenda> findByPropietariaIdAndActivaTrueOrderByCreadaEnDesc(Long propietariaId);

    /** Q1 */
    long countByPropietariaIdAndDisponibilidadAndActivaTrue(Long propietariaId, Disponibilidad disponibilidad);

    /** Q1 */
    boolean existsByIdAndPropietariaId(Long id, Long propietariaId);

    /** Q1 (Top / Containing / IgnoreCase / OrderBy) */
    List<Prenda> findTop5ByTituloContainingIgnoreCaseAndActivaTrueAndDisponibilidadOrderByCreadaEnDesc(
            String texto, Disponibilidad disponibilidad);

    /** Q1 (Between) */
    List<Prenda> findByCreadaEnBetweenAndActivaTrue(java.time.LocalDateTime desde, java.time.LocalDateTime hasta);

    /**
     * Q7 (Specification): buscador con filtros combinables. Endpoint 1.
     * El @EntityGraph carga categoría, propietaria y estilo con el listado (evita N+1);
     * son relaciones a uno, así que la paginación no se ve afectada.
     */
    @Override
    @EntityGraph(attributePaths = {"categoria", "propietaria", "estilo"})
    Page<Prenda> findAll(Specification<Prenda> spec, Pageable pageable);

    /** Q8 (@EntityGraph): prenda con fotos, propietaria, categoría y estilo en una consulta. Endpoint 2. */
    @EntityGraph(attributePaths = {"fotos", "propietaria", "categoria", "estilo"})
    @Query("select p from Prenda p where p.id = :id")
    Optional<Prenda> findDetalleById(@Param("id") Long id);

    /** Q5 (interfaz) + Q6 (Pageable): prendas visibles (no borrador) de una persona. Endpoint 7. */
    @Query(value = """
            select p.id as id, p.titulo as titulo, p.talla as talla, p.estado as estado,
                   p.modalidad as modalidad, c.nombre as categoria, u.username as propietaria,
                   u.ciudad as ciudad, p.creadaEn as creadaEn
            from Prenda p
            join p.categoria c
            join p.propietaria u
            where u.id = :propietariaId and p.activa = true
              and p.disponibilidad <> es.safareyes.swapcloset.entity.Disponibilidad.BORRADOR
            """,
            countQuery = "select count(p) from Prenda p where p.propietaria.id = :propietariaId and p.activa = true "
                    + "and p.disponibilidad <> es.safareyes.swapcloset.entity.Disponibilidad.BORRADOR")
    Page<PrendaTarjetaView> listarTarjetasDePropietaria(@Param("propietariaId") Long propietariaId, Pageable pageable);
}
