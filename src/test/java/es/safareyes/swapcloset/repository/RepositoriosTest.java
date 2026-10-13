package es.safareyes.swapcloset.repository;

import es.safareyes.swapcloset.dto.projection.*;
import es.safareyes.swapcloset.dto.response.ActividadCategoriaDto;
import es.safareyes.swapcloset.entity.*;
import es.safareyes.swapcloset.repository.spec.PrendaSpecs;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Prueba cada consulta contra la base de datos real (docker compose up -d) con los datos de data.sql.
 * Cada test se revierte al terminar, así que no ensucia los datos.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RepositoriosTest {

    @Autowired UsuarioRepository usuarios;
    @Autowired PrendaRepository prendas;
    @Autowired CategoriaRepository categorias;
    @Autowired SolicitudRepository solicitudes;
    @Autowired MensajeRepository mensajes;
    @Autowired ValoracionRepository valoraciones;
    @Autowired NotificacionRepository notificaciones;
    @Autowired FotoPrendaRepository fotos;

    // ---------- Q1 ----------
    @Test
    void q1_derivados() {
        assertThat(usuarios.existsByEmailIgnoreCase("MARIANA@swapcloset.es")).isTrue();
        assertThat(usuarios.existsByUsernameIgnoreCase("nadie")).isFalse();
        assertThat(usuarios.findByUsernameIgnoreCase("LUCIA_S")).isPresent();
        assertThat(prendas.findByPropietariaIdAndActivaTrueOrderByCreadaEnDesc(5L)).extracting(Prenda::getId)
                .doesNotContain(19L);
        assertThat(prendas.findTop5ByTituloContainingIgnoreCaseAndActivaTrueAndDisponibilidadOrderByCreadaEnDesc(
                "vestido", Disponibilidad.DISPONIBLE)).isNotEmpty();
        assertThat(fotos.countByPrendaId(1L)).isEqualTo(5);
        assertThat(fotos.findFirstByPrendaIdOrderByOrdenDesc(1L)).get().extracting(FotoPrenda::getOrden).isEqualTo(5);
        assertThat(valoraciones.existsBySolicitudIdAndAutorId(7L, 10L)).isTrue();
        assertThat(mensajes.findBySolicitudIdOrderByEnviadoEnAsc(3L)).hasSize(3);
        assertThat(notificaciones.existsByUsuarioIdAndTipoAndRecursoId(
                6L, TipoNotificacion.PRESTAMO_VENCE_MANANA, 4L)).isTrue();
    }

    @Test
    void q1_tresPrestamosActivosDeSofia() {
        long activos = solicitudes.countBySolicitanteIdAndTipoAndEstadoIn(
                6L, TipoSolicitud.PRESTAMO, List.of(EstadoSolicitud.ACEPTADA, EstadoSolicitud.RETRASADA));
        assertThat(activos).isEqualTo(3);
    }

    // ---------- Q2 ----------
    @Test
    void q2_jpql() {
        List<PerfilAfinView> afines = usuarios.buscarAfines(4L, PageRequest.of(0, 5));
        assertThat(afines).isNotEmpty();
        assertThat(afines.get(0).getEstilosComunes()).isGreaterThanOrEqualTo(1);

        assertThat(valoraciones.reputacionMedia(4L)).isCloseTo(5.0, within(0.001));
        assertThat(valoraciones.reputacionMedia(1L)).isNull();

        List<CategoriaConteoView> cats = categorias.contarPrendasPorCategoria(Disponibilidad.DISPONIBLE);
        assertThat(cats).hasSize(6);

        assertThat(solicitudes.findRecibidasPorDuenaDePrenda(7L, EstadoSolicitud.PENDIENTE)).isNotEmpty();
    }

    // ---------- Q3 ----------
    @Test
    void q3_nativas() {
        LocalDateTime desde = LocalDateTime.now().minusYears(1);
        LocalDateTime hasta = LocalDateTime.now().plusDays(1);
        List<ActividadMensualView> filas = solicitudes.actividadPorCategoriaYMes(desde, hasta);
        assertThat(filas).isNotEmpty();
        assertThat(filas.get(0).getMes()).matches("\\d{4}-\\d{2}");

        assertThat(solicitudes.rankingUsuariasActivas(3)).hasSize(3);
    }

    // ---------- Q4 ----------
    @Test
    void q4_modificacionesMasivas() {
        LocalDateTime ahora = LocalDateTime.now();
        int caducadas = solicitudes.caducarPendientesAnterioresA(
                ahora.minusDays(7), EstadoSolicitud.PENDIENTE, EstadoSolicitud.CADUCADA, ahora);
        assertThat(caducadas).isEqualTo(1);
        // idempotente: una segunda ejecución no cambia nada
        assertThat(solicitudes.caducarPendientesAnterioresA(
                ahora.minusDays(7), EstadoSolicitud.PENDIENTE, EstadoSolicitud.CADUCADA, ahora)).isZero();

        int retrasadas = solicitudes.marcarPrestamosRetrasados(
                TipoSolicitud.PRESTAMO, EstadoSolicitud.ACEPTADA, EstadoSolicitud.RETRASADA, LocalDate.now(), ahora);
        assertThat(retrasadas).isEqualTo(1); // la solicitud 5
    }

    // ---------- Q5 ----------
    @Test
    void q5_proyecciones() {
        Page<PrendaTarjetaView> tarjetas = prendas.listarTarjetasDePropietaria(4L, PageRequest.of(0, 10));
        assertThat(tarjetas.getContent()).isNotEmpty();
        assertThat(tarjetas.getContent().get(0).getPropietaria()).isEqualTo("lucia_s");

        List<ActividadCategoriaDto> resumen = solicitudes.resumenPorCategoria(
                LocalDateTime.now().minusYears(1), LocalDateTime.now().plusDays(1));
        assertThat(resumen).isNotEmpty();
    }

    // ---------- Q6 ----------
    @Test
    void q6_paginacionYOrdenacion() {
        Page<Mensaje> pagina = mensajes.findBySolicitudId(3L,
                PageRequest.of(0, 2, Sort.by("enviadoEn").ascending()));
        assertThat(pagina.getTotalElements()).isEqualTo(3);
        assertThat(pagina.getContent()).hasSize(2);

        Page<Notificacion> noLeidas = notificaciones.findByUsuarioIdAndLeida(
                6L, false, PageRequest.of(0, 10, Sort.by("creadaEn").descending()));
        assertThat(noLeidas.getContent()).allMatch(n -> !n.isLeida());
    }

    // ---------- Q7 ----------
    @Test
    void q7_specification() {
        Page<Prenda> todas = prendas.findAll(PrendaSpecs.filtrar(null, null, null, null, null, null),
                PageRequest.of(0, 50, Sort.by("creadaEn").descending()));
        // excluye el borrador (20), la retirada (19), las reservadas y las prestadas
        assertThat(todas.getContent()).extracting(Prenda::getId).doesNotContain(19L, 20L, 3L, 4L);
        assertThat(todas.getContent()).allMatch(p -> p.getDisponibilidad() == Disponibilidad.DISPONIBLE);

        Page<Prenda> sevillaPrestamo = prendas.findAll(
                PrendaSpecs.filtrar(null, null, null, null, "sevilla", Modalidad.PRESTAMO), PageRequest.of(0, 50));
        assertThat(sevillaPrestamo.getContent()).isNotEmpty()
                .allMatch(p -> p.getModalidad() != Modalidad.INTERCAMBIO);
    }

    // ---------- Q8 ----------
    @Test
    void q8_entityGraph() {
        Prenda p = prendas.findDetalleById(1L).orElseThrow();
        assertThat(Hibernate.isInitialized(p.getFotos())).isTrue();
        assertThat(p.getFotos()).hasSize(5);
        assertThat(Hibernate.isInitialized(p.getPropietaria())).isTrue();

        Usuario u = usuarios.findConEstilosById(7L).orElseThrow();
        assertThat(Hibernate.isInitialized(u.getEstilos())).isTrue();
        assertThat(u.getEstilos()).hasSize(5);
    }
}
