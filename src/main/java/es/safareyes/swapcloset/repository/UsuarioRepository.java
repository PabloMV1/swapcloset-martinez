package es.safareyes.swapcloset.repository;

import es.safareyes.swapcloset.dto.projection.PerfilAfinView;
import es.safareyes.swapcloset.entity.Usuario;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Q1 */
    boolean existsByEmailIgnoreCase(String email);

    /** Q1 */
    boolean existsByUsernameIgnoreCase(String username);

    /** Q1 */
    Optional<Usuario> findByUsernameIgnoreCase(String username);

    /** Q1 */
    Optional<Usuario> findByEmailIgnoreCase(String email);

    /** Q8 (@EntityGraph): usuario con sus estilos en una sola consulta. */
    @EntityGraph(attributePaths = "estilos")
    @Query("select u from Usuario u where u.id = :id")
    Optional<Usuario> findConEstilosById(@Param("id") Long id);

    /**
     * Q2 (JPQL con JOIN, COUNT, GROUP BY y ORDER BY) + Q5 (interfaz). Endpoint 9.
     * Personas con más estilos en común con la usuaria {@code yo}.
     */
    @Query("""
            select u.id as id, u.username as username, u.ciudad as ciudad, count(e.id) as estilosComunes
            from Usuario u
            join u.estilos e
            where u.id <> :yo
              and u.rol = es.safareyes.swapcloset.entity.Rol.USUARIO
              and e.id in (select e2.id from Usuario y join y.estilos e2 where y.id = :yo)
            group by u.id, u.username, u.ciudad
            order by count(e.id) desc, u.username asc
            """)
    List<PerfilAfinView> buscarAfines(@Param("yo") Long yo, Pageable pageable);
}
