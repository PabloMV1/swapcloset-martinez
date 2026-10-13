package es.safareyes.swapcloset.service;

import es.safareyes.swapcloset.dto.projection.FotoPortadaView;
import es.safareyes.swapcloset.dto.request.PrendaActualizarRequest;
import es.safareyes.swapcloset.dto.request.PrendaCrearRequest;
import es.safareyes.swapcloset.dto.response.CategoriaResponse;
import es.safareyes.swapcloset.dto.response.PrendaDetalleResponse;
import es.safareyes.swapcloset.dto.response.PrendaResumenResponse;
import es.safareyes.swapcloset.dto.response.PropietariaResponse;
import es.safareyes.swapcloset.entity.*;
import es.safareyes.swapcloset.exception.ProhibidoException;
import es.safareyes.swapcloset.exception.RecursoNoEncontradoException;
import es.safareyes.swapcloset.exception.ReglaNegocioException;
import es.safareyes.swapcloset.mapper.CategoriaMapper;
import es.safareyes.swapcloset.mapper.PrendaMapper;
import es.safareyes.swapcloset.repository.*;
import es.safareyes.swapcloset.repository.spec.PrendaSpecs;
import es.safareyes.swapcloset.security.Actor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrendaServiceImpl implements PrendaService {

    private final PrendaRepository prendas;
    private final CategoriaRepository categorias;
    private final EstiloRepository estilos;
    private final UsuarioRepository usuarios;
    private final FotoPrendaRepository fotos;
    private final ValoracionRepository valoraciones;
    private final SolicitudRepository solicitudes;
    private final PrendaMapper prendaMapper;
    private final CategoriaMapper categoriaMapper;

    // ------------------------------------------------------------------ endpoint 1
    @Override
    public Page<PrendaResumenResponse> buscar(Long categoriaId, String talla, EstadoPrenda estado, Long estiloId,
                                              String ciudad, Modalidad modalidad, Pageable pageable) {
        Page<Prenda> pagina = prendas.findAll(
                PrendaSpecs.filtrar(categoriaId, talla, estado, estiloId, ciudad, modalidad), pageable);

        // Una sola consulta para las portadas de toda la página (evita N+1)
        List<Long> ids = pagina.getContent().stream().map(Prenda::getId).toList();
        Map<Long, Long> portadas = ids.isEmpty() ? Map.of()
                : fotos.findPortadas(ids).stream()
                        .collect(Collectors.toMap(FotoPortadaView::getPrendaId, FotoPortadaView::getId));

        return pagina.map(p -> prendaMapper.toResumen(p, portadas.get(p.getId())));
    }

    // ------------------------------------------------------------------ endpoint 2
    @Override
    public PrendaDetalleResponse obtener(Long id, Actor actor) {
        Prenda prenda = prendas.findDetalleById(id)
                .orElseThrow(() -> noExiste(id));
        // Un borrador o una prenda retirada solo la ve su dueña o el personal de moderación
        if (!esVisiblePara(prenda, actor)) {
            throw noExiste(id);
        }
        return aDetalle(prenda);
    }

    // ------------------------------------------------------------------ endpoint 3
    @Override
    @Transactional
    public PrendaDetalleResponse crear(Actor actor, PrendaCrearRequest request) {
        Usuario propietaria = usuarios.findById(actor.id())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id " + actor.id()));

        Prenda prenda = prendaMapper.toEntity(request);
        prenda.setPropietaria(propietaria);
        prenda.setCategoria(buscarCategoria(request.categoriaId()));
        prenda.setEstilo(buscarEstilo(request.estiloId()));
        prenda.setDisponibilidad(Disponibilidad.BORRADOR); // RN-03: pasa a DISPONIBLE con la primera foto
        prenda.setActiva(true);

        Prenda guardada = prendas.save(prenda);
        log.info("Prenda {} creada en BORRADOR por el usuario {}", guardada.getId(), actor.id());
        return aDetalle(guardada);
    }

    // ------------------------------------------------------------------ endpoint 4
    @Override
    @Transactional
    public PrendaDetalleResponse actualizar(Actor actor, Long id, PrendaActualizarRequest request) {
        Prenda prenda = prendas.findById(id).orElseThrow(() -> noExiste(id));
        if (!prenda.isActiva()) {
            throw noExiste(id);
        }
        if (!esPropietaria(prenda, actor)) {
            throw new ProhibidoException("Solo la propietaria puede modificar la prenda");
        }
        if (prenda.getDisponibilidad() == Disponibilidad.RESERVADA
                || prenda.getDisponibilidad() == Disponibilidad.PRESTADA) {
            throw new ReglaNegocioException("No se puede modificar una prenda reservada o prestada");
        }

        prendaMapper.actualizar(request, prenda);
        if (request.categoriaId() != null) {
            prenda.setCategoria(buscarCategoria(request.categoriaId()));
        }
        if (request.estiloId() != null) {
            prenda.setEstilo(buscarEstilo(request.estiloId()));
        }
        log.info("Prenda {} modificada por el usuario {}", id, actor.id());
        return aDetalle(prenda);
    }

    // ------------------------------------------------------------------ endpoint 5
    @Override
    @Transactional
    public void retirar(Actor actor, Long id) {
        Prenda prenda = prendas.findById(id).orElseThrow(() -> noExiste(id));
        if (!esPropietaria(prenda, actor) && !actor.esStaff()) {
            throw new ProhibidoException("Solo la propietaria o la moderación pueden retirar la prenda");
        }
        if (!prenda.isActiva()) {
            return; // ya estaba retirada: la operación es idempotente
        }
        if (prenda.getDisponibilidad() == Disponibilidad.RESERVADA
                || prenda.getDisponibilidad() == Disponibilidad.PRESTADA) {
            throw new ReglaNegocioException("No se puede retirar una prenda con un intercambio o préstamo en curso");
        }

        prenda.setActiva(false); // baja lógica (RN-10)
        // RN-10: cancela las solicitudes pendientes de esa prenda (el flush previo guarda la baja)
        int canceladas = solicitudes.cambiarEstadoPendientesDePrenda(
                id, -1L, EstadoSolicitud.PENDIENTE, EstadoSolicitud.CANCELADA, LocalDateTime.now());
        log.info("Prenda {} retirada por el usuario {} ({}); {} solicitud(es) pendiente(s) canceladas",
                id, actor.id(), actor.rol(), canceladas);
    }

    // ------------------------------------------------------------------ endpoint 6
    @Override
    public List<CategoriaResponse> listarCategorias() {
        return categoriaMapper.toResponseList(categorias.contarPrendasPorCategoria(Disponibilidad.DISPONIBLE));
    }

    // ------------------------------------------------------------------ auxiliares
    private PrendaDetalleResponse aDetalle(Prenda prenda) {
        Usuario duena = prenda.getPropietaria();
        Double media = valoraciones.reputacionMedia(duena.getId());
        Double reputacion = media == null ? null : Math.round(media * 10) / 10.0; // un decimal (RN-09)
        long total = valoraciones.countByValoradoId(duena.getId());
        PropietariaResponse propietaria = prendaMapper.toPropietaria(duena, reputacion, total);
        return prendaMapper.toDetalle(prenda, propietaria);
    }

    private boolean esPropietaria(Prenda prenda, Actor actor) {
        return actor != null && prenda.getPropietaria().getId().equals(actor.id());
    }

    private boolean esVisiblePara(Prenda prenda, Actor actor) {
        boolean publica = prenda.isActiva() && prenda.getDisponibilidad() != Disponibilidad.BORRADOR;
        return publica || (actor != null && (esPropietaria(prenda, actor) || actor.esStaff()));
    }

    private Categoria buscarCategoria(Long id) {
        return categorias.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la categoría con id " + id));
    }

    private Estilo buscarEstilo(Long id) {
        return estilos.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el estilo con id " + id));
    }

    private RecursoNoEncontradoException noExiste(Long id) {
        return new RecursoNoEncontradoException("No existe la prenda con id " + id);
    }
}
