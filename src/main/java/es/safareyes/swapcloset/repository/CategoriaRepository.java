package es.safareyes.swapcloset.repository;

import es.safareyes.swapcloset.dto.projection.CategoriaConteoView;
import es.safareyes.swapcloset.entity.Categoria;
import es.safareyes.swapcloset.entity.Disponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /** Q2 (JPQL con JOIN, COUNT y GROUP BY) + Q5 (proyección por interfaz). Endpoint 6. */
    @Query("""
            select c.id as id, c.nombre as nombre, count(p.id) as disponibles
            from Categoria c
            left join Prenda p on p.categoria.id = c.id
                              and p.activa = true
                              and p.disponibilidad = :disponibilidad
            group by c.id, c.nombre
            order by c.nombre
            """)
    List<CategoriaConteoView> contarPrendasPorCategoria(@Param("disponibilidad") Disponibilidad disponibilidad);
}
