package es.safareyes.swapcloset.service;

import es.safareyes.swapcloset.dto.request.AccionSolicitud;
import es.safareyes.swapcloset.dto.request.SolicitudCrearRequest;
import es.safareyes.swapcloset.dto.request.VistaSolicitudes;
import es.safareyes.swapcloset.dto.response.SolicitudResponse;
import es.safareyes.swapcloset.entity.*;
import es.safareyes.swapcloset.exception.ConflictoException;
import es.safareyes.swapcloset.exception.ProhibidoException;
import es.safareyes.swapcloset.exception.RecursoNoEncontradoException;
import es.safareyes.swapcloset.exception.ReglaNegocioException;
import es.safareyes.swapcloset.mapper.SolicitudMapper;
import es.safareyes.swapcloset.repository.PrendaRepository;
import es.safareyes.swapcloset.repository.SolicitudRepository;
import es.safareyes.swapcloset.repository.UsuarioRepository;
import es.safareyes.swapcloset.security.Actor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SolicitudServiceImpl implements SolicitudService {

    private static final int MAX_PRESTAMOS_ACTIVOS = 3;      // RN-05
    private static final long MAX_DIAS_PRESTAMO = 30;        // RN-05
    private static final String RECURSO = "SOLICITUD";

    private final SolicitudRepository solicitudes;
    private final PrendaRepository prendas;
    private final UsuarioRepository usuarios;
    private final SolicitudAcceso acceso;
    private final NotificacionService notificaciones;
    private final SolicitudMapper mapper;

    // ------------------------------------------------------------------ endpoint 10
    @Override
    @Transactional
    public SolicitudResponse crear(Actor actor, SolicitudCrearRequest r) {
        Usuario solicitante = usuarios.findById(actor.id())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id " + actor.id()));
        Prenda prenda = prendas.findById(r.prendaId())
                .filter(Prenda::isActiva)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la prenda con id " + r.prendaId()));

        // RN-04
        if (prenda.getPropietaria().getId().equals(actor.id())) {
            throw new ReglaNegocioException("No puedes solicitar una prenda tuya");
        }
        if (prenda.getDisponibilidad() != Disponibilidad.DISPONIBLE) {
            throw new ReglaNegocioException("La prenda no está disponible en este momento");
        }
        if (!admite(prenda, r.tipo())) {
            throw new ReglaNegocioException("La prenda no admite " + r.tipo().name().toLowerCase()
                    + " (modalidad " + prenda.getModalidad() + ")");
        }
        if (solicitudes.existsBySolicitanteIdAndPrendaIdAndEstado(actor.id(), prenda.getId(),
                EstadoSolicitud.PENDIENTE)) {
            throw new ConflictoException("Ya tienes una solicitud pendiente para esta prenda");
        }

        Solicitud nueva = Solicitud.builder()
                .tipo(r.tipo())
                .prenda(prenda)
                .solicitante(solicitante)
                .propietaria(prenda.getPropietaria())
                .build();

        if (r.tipo() == TipoSolicitud.INTERCAMBIO) {
            prepararIntercambio(nueva, actor, r);
        } else {
            prepararPrestamo(nueva, actor, r);
        }

        Solicitud guardada = solicitudes.save(nueva);
        notificaciones.notificar(prenda.getPropietaria().getId(), TipoNotificacion.SOLICITUD_RECIBIDA,
                solicitante.getUsername() + " ha solicitado tu prenda \"" + prenda.getTitulo() + "\"",
                RECURSO, guardada.getId());
        log.info("Solicitud {} ({}) creada por el usuario {} sobre la prenda {}",
                guardada.getId(), r.tipo(), actor.id(), prenda.getId());
        return mapper.toResponse(guardada);
    }

    private void prepararIntercambio(Solicitud nueva, Actor actor, SolicitudCrearRequest r) {
        if (r.fechaInicio() != null || r.fechaFin() != null) {
            throw new ReglaNegocioException("Un intercambio no lleva fechas");
        }
        if (r.prendaOfrecidaId() == null) {
            throw new ReglaNegocioException("En un intercambio debes indicar la prenda que ofreces");
        }
        Prenda ofrecida = prendas.findById(r.prendaOfrecidaId())
                .filter(Prenda::isActiva)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe la prenda ofrecida con id " + r.prendaOfrecidaId()));
        if (!ofrecida.getPropietaria().getId().equals(actor.id())) {
            throw new ReglaNegocioException("La prenda que ofreces debe ser tuya");
        }
        if (ofrecida.getDisponibilidad() != Disponibilidad.DISPONIBLE) {
            throw new ReglaNegocioException("La prenda que ofreces no está disponible");
        }
        if (!admite(ofrecida, TipoSolicitud.INTERCAMBIO)) {
            throw new ReglaNegocioException("La prenda que ofreces no admite intercambio");
        }
        nueva.setPrendaOfrecida(ofrecida);
    }

    private void prepararPrestamo(Solicitud nueva, Actor actor, SolicitudCrearRequest r) {
        if (r.prendaOfrecidaId() != null) {
            throw new ReglaNegocioException("Un préstamo no lleva prenda ofrecida");
        }
        if (r.fechaInicio() == null || r.fechaFin() == null) {
            throw new ReglaNegocioException("En un préstamo debes indicar fechaInicio y fechaFin");
        }
        if (r.fechaInicio().isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("La fecha de inicio no puede ser anterior a hoy");
        }
        if (!r.fechaFin().isAfter(r.fechaInicio())) {
            throw new ReglaNegocioException("La fecha de fin debe ser posterior a la de inicio");
        }
        if (ChronoUnit.DAYS.between(r.fechaInicio(), r.fechaFin()) > MAX_DIAS_PRESTAMO) {
            throw new ReglaNegocioException("Un préstamo dura como máximo " + MAX_DIAS_PRESTAMO + " días");
        }
        long activos = solicitudes.countBySolicitanteIdAndTipoAndEstadoIn(actor.id(), TipoSolicitud.PRESTAMO,
                Set.of(EstadoSolicitud.ACEPTADA, EstadoSolicitud.RETRASADA));
        if (activos >= MAX_PRESTAMOS_ACTIVOS) {
            throw new ReglaNegocioException("Ya tienes " + MAX_PRESTAMOS_ACTIVOS + " préstamos activos");
        }
        nueva.setFechaInicio(r.fechaInicio());
        nueva.setFechaFin(r.fechaFin());
    }

    private boolean admite(Prenda prenda, TipoSolicitud tipo) {
        return prenda.getModalidad() == Modalidad.AMBAS
                || prenda.getModalidad().name().equals(tipo.name());
    }

    // ------------------------------------------------------------------ endpoint 11
    @Override
    public Page<SolicitudResponse> listar(Actor actor, VistaSolicitudes vista, EstadoSolicitud estado,
                                          Pageable pageable) {
        Page<Solicitud> pagina;
        if (vista == VistaSolicitudes.RECIBIDAS) {
            pagina = estado == null
                    ? solicitudes.findByPropietariaId(actor.id(), pageable)
                    : solicitudes.findByPropietariaIdAndEstado(actor.id(), estado, pageable);
        } else {
            pagina = estado == null
                    ? solicitudes.findBySolicitanteId(actor.id(), pageable)
                    : solicitudes.findBySolicitanteIdAndEstado(actor.id(), estado, pageable);
        }
        return pagina.map(mapper::toResponse);
    }

    @Override
    public SolicitudResponse obtener(Actor actor, Long id) {
        return mapper.toResponse(acceso.cargarParaParticipante(id, actor));
    }

    // ------------------------------------------------------------------ endpoint 12
    @Override
    @Transactional
    public SolicitudResponse cambiarEstado(Actor actor, Long id, AccionSolicitud accion) {
        Solicitud s = acceso.cargarParaParticipante(id, actor);
        Solicitud resultado = switch (accion) {
            case ACEPTAR -> aceptar(s, actor);
            case RECHAZAR -> rechazar(s, actor);
            case CANCELAR -> cancelar(s, actor);
            case FINALIZAR -> finalizar(s, actor);
        };
        log.info("Solicitud {}: {} por el usuario {} -> {}", id, accion, actor.id(), resultado.getEstado());
        return mapper.toResponse(resultado);
    }

    private Solicitud aceptar(Solicitud s, Actor actor) {
        exigirPropietaria(s, actor, "Solo la propietaria puede aceptar la solicitud");
        exigirEstado(s, EstadoSolicitud.ACEPTADA, EstadoSolicitud.PENDIENTE);
        Prenda prenda = s.getPrenda();
        Prenda ofrecida = s.getPrendaOfrecida();
        if (!prenda.isActiva() || prenda.getDisponibilidad() != Disponibilidad.DISPONIBLE) {
            throw new ReglaNegocioException("La prenda ya no está disponible");
        }
        if (ofrecida != null && (!ofrecida.isActiva() || ofrecida.getDisponibilidad() != Disponibilidad.DISPONIBLE)) {
            throw new ReglaNegocioException("La prenda ofrecida ya no está disponible");
        }
        Long solicitudId = s.getId();
        Long prendaId = prenda.getId();
        Long ofrecidaId = ofrecida == null ? null : ofrecida.getId();

        // RN-07: el resto de solicitudes pendientes de las prendas implicadas pasan a RECHAZADA
        List<Solicitud> otras = solicitudes.findByPrendaIdAndEstado(prendaId, EstadoSolicitud.PENDIENTE).stream()
                .filter(o -> !o.getId().equals(solicitudId)).toList();
        List<Solicitud> otrasOfrecida = ofrecidaId == null ? List.of()
                : solicitudes.findByPrendaIdAndEstado(ofrecidaId, EstadoSolicitud.PENDIENTE);
        List<Long[]> avisos = new java.util.ArrayList<>();
        for (Solicitud o : otras) avisos.add(new Long[]{o.getSolicitante().getId(), o.getId()});
        for (Solicitud o : otrasOfrecida) avisos.add(new Long[]{o.getSolicitante().getId(), o.getId()});

        LocalDateTime ahora = LocalDateTime.now();
        solicitudes.cambiarEstadoPendientesDePrenda(prendaId, solicitudId,
                EstadoSolicitud.PENDIENTE, EstadoSolicitud.RECHAZADA, ahora);
        if (ofrecidaId != null) {
            solicitudes.cambiarEstadoPendientesDePrenda(ofrecidaId, solicitudId,
                    EstadoSolicitud.PENDIENTE, EstadoSolicitud.RECHAZADA, ahora);
        }
        // La actualización masiva vacía el contexto de persistencia: se vuelve a cargar la solicitud
        Solicitud actual = solicitudes.findConRelacionesById(solicitudId).orElseThrow();
        actual.setEstado(EstadoSolicitud.ACEPTADA);
        if (actual.getTipo() == TipoSolicitud.PRESTAMO) {
            actual.getPrenda().setDisponibilidad(Disponibilidad.PRESTADA);
        } else {
            actual.getPrenda().setDisponibilidad(Disponibilidad.RESERVADA);
            actual.getPrendaOfrecida().setDisponibilidad(Disponibilidad.RESERVADA);
        }

        notificaciones.notificar(actual.getSolicitante().getId(), TipoNotificacion.SOLICITUD_ACEPTADA,
                "Tu solicitud de \"" + actual.getPrenda().getTitulo() + "\" ha sido aceptada",
                RECURSO, solicitudId);
        for (Long[] aviso : avisos) {
            notificaciones.notificar(aviso[0], TipoNotificacion.SOLICITUD_RECHAZADA,
                    "Tu solicitud ha sido rechazada: la prenda ya no está disponible", RECURSO, aviso[1]);
        }
        return actual;
    }

    private Solicitud rechazar(Solicitud s, Actor actor) {
        exigirPropietaria(s, actor, "Solo la propietaria puede rechazar la solicitud");
        exigirEstado(s, EstadoSolicitud.RECHAZADA, EstadoSolicitud.PENDIENTE);
        s.setEstado(EstadoSolicitud.RECHAZADA);
        notificaciones.notificar(s.getSolicitante().getId(), TipoNotificacion.SOLICITUD_RECHAZADA,
                "Tu solicitud de \"" + s.getPrenda().getTitulo() + "\" ha sido rechazada", RECURSO, s.getId());
        return s;
    }

    private Solicitud cancelar(Solicitud s, Actor actor) {
        if (!acceso.esSolicitante(s, actor)) {
            throw new ProhibidoException("Solo quien hizo la solicitud puede cancelarla");
        }
        exigirEstado(s, EstadoSolicitud.CANCELADA, EstadoSolicitud.PENDIENTE);
        s.setEstado(EstadoSolicitud.CANCELADA);
        return s;
    }

    private Solicitud finalizar(Solicitud s, Actor actor) {
        // Préstamo: lo cierra la dueña al recibir la prenda. Intercambio: cualquiera de las dos al hacer la entrega.
        if (s.getTipo() == TipoSolicitud.PRESTAMO) {
            exigirPropietaria(s, actor, "Solo la propietaria puede confirmar la devolución");
        }
        exigirEstado(s, EstadoSolicitud.FINALIZADA, EstadoSolicitud.ACEPTADA, EstadoSolicitud.RETRASADA);
        s.setEstado(EstadoSolicitud.FINALIZADA);

        Prenda prenda = s.getPrenda();
        if (s.getTipo() == TipoSolicitud.PRESTAMO) {
            prenda.setDisponibilidad(Disponibilidad.DISPONIBLE);
        } else {
            // RN-07: las prendas cambian de dueña
            Prenda ofrecida = s.getPrendaOfrecida();
            prenda.setPropietaria(s.getSolicitante());
            ofrecida.setPropietaria(s.getPropietaria());
            prenda.setDisponibilidad(Disponibilidad.DISPONIBLE);
            ofrecida.setDisponibilidad(Disponibilidad.DISPONIBLE);
        }
        return s;
    }

    private void exigirPropietaria(Solicitud s, Actor actor, String mensaje) {
        if (!acceso.esPropietaria(s, actor)) {
            throw new ProhibidoException(mensaje);
        }
    }

    /** RN-06: máquina de estados. Solo se permite pasar desde los estados indicados. */
    private void exigirEstado(Solicitud s, EstadoSolicitud destino, EstadoSolicitud... permitidos) {
        for (EstadoSolicitud permitido : permitidos) {
            if (s.getEstado() == permitido) {
                return;
            }
        }
        throw new ReglaNegocioException("Una solicitud " + s.getEstado() + " no puede pasar a " + destino);
    }
}
