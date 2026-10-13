package es.safareyes.swapcloset.repository;

import es.safareyes.swapcloset.dto.projection.ActividadMensualView;
import es.safareyes.swapcloset.dto.projection.RankingUsuarioView;
import es.safareyes.swapcloset.dto.response.ActividadCategoriaDto;
import es.safareyes.swapcloset.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    // ---------------- Q1: métodos derivados ----------------

    /** RN-04: no dos solicitudes pendientes de la misma prenda. */
    boolean existsBySolicitanteIdAndPrendaIdAndEstado(Long solicitanteId, Long prendaId, EstadoSolicitud estado);

    /** RN-05: préstamos activos como solicitante (ACEPTADA y RETRASADA). */
    long countBySolicitanteIdAndTipoAndEstadoIn(Long solicitanteId, TipoSolicitud tipo, Collection<EstadoSolicitud> estados);

    List<Solicitud> findByPrendaIdAndEstado(Long prendaId, EstadoSolicitud estado);

    /** Tarea programada: préstamos que vencen en una fecha (aviso del día antes). */
    List<Solicitud> findByTipoAndEstadoAndFechaFin(TipoSolicitud tipo, EstadoSolicitud estado, LocalDate fechaFin);

    /** Tarea programada: préstamos vencidos y sin devolver. */
    List<Solicitud> findByTipoAndEstadoAndFechaFinLessThan(TipoSolicitud tipo, EstadoSolicitud estado, LocalDate hoy);

    // ---------------- Q8: una solicitud con todas sus relaciones en una sola consulta ----------------

    @EntityGraph(attributePaths = {"prenda", "solicitante", "propietaria", "prendaOfrecida"})
    Optional<Solicitud> findConRelacionesById(Long id);

    // ---------------- Q6 + Q8: paginación con EntityGraph. Endpoint 11 ----------------

    @EntityGraph(attributePaths = {"prenda", "solicitante", "propietaria", "prendaOfrecida"})
    Page<Solicitud> findBySolicitanteId(Long solicitanteId, Pageable pageable);

    @EntityGraph(attributePaths = {"prenda", "solicitante", "propietaria", "prendaOfrecida"})
    Page<Solicitud> findBySolicitanteIdAndEstado(Long solicitanteId, EstadoSolicitud estado, Pageable pageable);

    @EntityGraph(attributePaths = {"prenda", "solicitante", "propietaria", "prendaOfrecida"})
    Page<Solicitud> findByPropietariaId(Long propietariaId, Pageable pageable);

    @EntityGraph(attributePaths = {"prenda", "solicitante", "propietaria", "prendaOfrecida"})
    Page<Solicitud> findByPropietariaIdAndEstado(Long propietariaId, EstadoSolicitud estado, Pageable pageable);

    // ---------------- Q2: JPQL con JOIN ----------------

    /** Solicitudes recibidas por la dueña actual de la prenda (JOIN con prenda). */
    @Query("""
            select s from Solicitud s
            join s.prenda p
            where p.propietaria.id = :propietariaId and s.estado = :estado
            order by s.creadaEn desc
            """)
    List<Solicitud> findRecibidasPorDuenaDePrenda(@Param("propietariaId") Long propietariaId,
                                                  @Param("estado") EstadoSolicitud estado);

    // ---------------- Q4: modificaciones masivas ----------------

    /** RN-07: al aceptar una solicitud, las demás pendientes de esa prenda pasan a RECHAZADA. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Solicitud s
               set s.estado = :nuevo, s.actualizadaEn = :ahora
             where s.prenda.id = :prendaId and s.estado = :actual and s.id <> :excepto
            """)
    int cambiarEstadoPendientesDePrenda(@Param("prendaId") Long prendaId,
                                        @Param("excepto") Long excepto,
                                        @Param("actual") EstadoSolicitud actual,
                                        @Param("nuevo") EstadoSolicitud nuevo,
                                        @Param("ahora") LocalDateTime ahora);

    /** Tarea programada: caducar solicitudes pendientes de más de 7 días. Idempotente. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Solicitud s
               set s.estado = :nuevo, s.actualizadaEn = :ahora
             where s.estado = :actual and s.creadaEn < :limite
            """)
    int caducarPendientesAnterioresA(@Param("limite") LocalDateTime limite,
                                     @Param("actual") EstadoSolicitud actual,
                                     @Param("nuevo") EstadoSolicitud nuevo,
                                     @Param("ahora") LocalDateTime ahora);

    /** Tarea programada: préstamos aceptados con la fecha de fin pasada pasan a RETRASADA. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Solicitud s
               set s.estado = :nuevo, s.actualizadaEn = :ahora
             where s.tipo = :tipo and s.estado = :actual and s.fechaFin < :hoy
            """)
    int marcarPrestamosRetrasados(@Param("tipo") TipoSolicitud tipo,
                                  @Param("actual") EstadoSolicitud actual,
                                  @Param("nuevo") EstadoSolicitud nuevo,
                                  @Param("hoy") LocalDate hoy,
                                  @Param("ahora") LocalDateTime ahora);

    // ---------------- Q5: proyección por record (SELECT new) ----------------

    @Query("""
            select new es.safareyes.swapcloset.dto.response.ActividadCategoriaDto(c.nombre, count(s))
            from Solicitud s
            join s.prenda p
            join p.categoria c
            where s.creadaEn >= :desde and s.creadaEn < :hasta
            group by c.nombre
            order by count(s) desc, c.nombre
            """)
    List<ActividadCategoriaDto> resumenPorCategoria(@Param("desde") LocalDateTime desde,
                                                    @Param("hasta") LocalDateTime hasta);

    // ---------------- Q3: SQL nativo de PostgreSQL ----------------

    /** Endpoint 16: intercambios y préstamos por categoría y mes (DATE_TRUNC + FILTER). */
    @Query(value = """
            select to_char(date_trunc('month', s.creada_en), 'YYYY-MM') as mes,
                   c.nombre as categoria,
                   count(*) filter (where s.tipo = 'INTERCAMBIO') as intercambios,
                   count(*) filter (where s.tipo = 'PRESTAMO') as prestamos
            from solicitudes s
            join prendas p on p.id = s.prenda_id
            join categorias c on c.id = p.categoria_id
            where s.creada_en >= :desde and s.creada_en < :hasta
            group by date_trunc('month', s.creada_en), c.nombre
            order by mes, categoria
            """, nativeQuery = true)
    List<ActividadMensualView> actividadPorCategoriaYMes(@Param("desde") LocalDateTime desde,
                                                         @Param("hasta") LocalDateTime hasta);

    /** Ranking de las personas más activas: solicitudes enviadas y recibidas. */
    @Query(value = """
            select u.id as id, u.username as username, count(s.id) as solicitudes
            from usuarios u
            join solicitudes s on s.solicitante_id = u.id or s.propietaria_id = u.id
            group by u.id, u.username
            order by count(s.id) desc, u.username
            limit :limite
            """, nativeQuery = true)
    List<RankingUsuarioView> rankingUsuariasActivas(@Param("limite") int limite);
}
