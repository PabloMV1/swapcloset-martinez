package es.safareyes.swapcloset.service;

import es.safareyes.swapcloset.dto.response.FotoResponse;
import es.safareyes.swapcloset.entity.Disponibilidad;
import es.safareyes.swapcloset.entity.FotoPrenda;
import es.safareyes.swapcloset.entity.Prenda;
import es.safareyes.swapcloset.exception.FicheroNoValidoException;
import es.safareyes.swapcloset.exception.ProhibidoException;
import es.safareyes.swapcloset.exception.RecursoNoEncontradoException;
import es.safareyes.swapcloset.exception.ReglaNegocioException;
import es.safareyes.swapcloset.mapper.FotoMapper;
import es.safareyes.swapcloset.repository.FotoPrendaRepository;
import es.safareyes.swapcloset.repository.PrendaRepository;
import es.safareyes.swapcloset.security.Actor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FotoServiceImpl implements FotoService {

    static final long MAX_BYTES = 5L * 1024 * 1024;   // 5 MB
    static final int MAX_FOTOS = 5;                    // RN-03

    private final PrendaRepository prendas;
    private final FotoPrendaRepository fotos;
    private final StorageService almacenamiento;
    private final FotoMapper mapper;

    // ------------------------------------------------------------------ endpoint 17
    @Override
    @Transactional
    public FotoResponse subir(Actor actor, Long prendaId, MultipartFile fichero) {
        Prenda prenda = prendas.findById(prendaId)
                .filter(Prenda::isActiva)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la prenda con id " + prendaId));
        if (!prenda.getPropietaria().getId().equals(actor.id())) {
            throw new ProhibidoException("Solo la propietaria puede subir fotos de su prenda");
        }

        String tipo = validar(fichero);
        if (fotos.countByPrendaId(prendaId) >= MAX_FOTOS) {
            throw new ReglaNegocioException("Una prenda admite como máximo " + MAX_FOTOS + " fotos");
        }
        int orden = fotos.findFirstByPrendaIdOrderByOrdenDesc(prendaId).map(f -> f.getOrden() + 1).orElse(1);

        String clave = almacenamiento.guardar(fichero, extensionDe(tipo));
        // Si la transacción no llega a confirmarse, el fichero que acabamos de escribir se borra
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int estado) {
                if (estado != STATUS_COMMITTED) {
                    almacenamiento.borrar(clave);
                }
            }
        });

        FotoPrenda foto = fotos.save(FotoPrenda.builder()
                .prenda(prenda)
                .nombreOriginal(nombreSeguro(fichero.getOriginalFilename()))
                .claveDisco(clave)
                .tipoContenido(tipo)
                .tamanoBytes(fichero.getSize())
                .orden(orden)
                .build());

        if (prenda.getDisponibilidad() == Disponibilidad.BORRADOR) {
            prenda.setDisponibilidad(Disponibilidad.DISPONIBLE); // RN-03: con la primera foto se publica
        }
        log.info("Foto {} (orden {}) subida a la prenda {} por el usuario {}", foto.getId(), orden, prendaId, actor.id());
        return mapper.toResponse(foto);
    }

    // ------------------------------------------------------------------ endpoint 18
    @Override
    public FotoDescarga descargar(Actor actor, Long fotoId) {
        FotoPrenda foto = fotos.findConPrendaById(fotoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la foto con id " + fotoId));
        Prenda prenda = foto.getPrenda();
        boolean publica = prenda.isActiva() && prenda.getDisponibilidad() != Disponibilidad.BORRADOR;
        boolean autorizada = actor != null
                && (actor.esStaff() || prenda.getPropietaria().getId().equals(actor.id()));
        if (!publica && !autorizada) {
            throw new RecursoNoEncontradoException("No existe la foto con id " + fotoId); // no se revela que existe
        }
        var recurso = almacenamiento.cargar(foto.getClaveDisco())
                .orElseThrow(() -> new RecursoNoEncontradoException("El fichero de la foto " + fotoId + " no está disponible"));
        return new FotoDescarga(recurso, foto.getTipoContenido(), foto.getNombreOriginal());
    }

    // ------------------------------------------------------------------ validación del fichero

    /** Devuelve el tipo real del fichero (image/jpeg, image/png o image/webp) o lanza el error que corresponda. */
    private String validar(MultipartFile fichero) {
        if (fichero == null || fichero.isEmpty()) {
            throw new ReglaNegocioException("El fichero está vacío");
        }
        if (fichero.getSize() > MAX_BYTES) {
            throw new MaxUploadSizeExceededException(MAX_BYTES); // 413
        }
        String declarado = fichero.getContentType();
        if (declarado == null || !(declarado.equals("image/jpeg") || declarado.equals("image/png")
                || declarado.equals("image/webp"))) {
            throw new FicheroNoValidoException("Solo se admiten imágenes JPEG, PNG o WebP");
        }
        // No nos fiamos de lo que diga el cliente: se mira la firma real de los primeros bytes del fichero
        String real = detectarTipo(fichero);
        if (!declarado.equals(real)) {
            throw new FicheroNoValidoException("El contenido del fichero no es una imagen " + declarado);
        }
        return real;
    }

    private String detectarTipo(MultipartFile fichero) {
        byte[] b;
        try (InputStream in = fichero.getInputStream()) {
            b = in.readNBytes(12);
        } catch (IOException e) {
            throw new FicheroNoValidoException("No se puede leer el fichero");
        }
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return "image/png";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "image/webp";
        }
        return "desconocido";
    }

    private String extensionDe(String tipo) {
        return switch (tipo) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            default -> "webp";
        };
    }

    /** El nombre original es solo informativo: se queda con el último tramo y se acota a 255 caracteres. */
    private String nombreSeguro(String original) {
        if (original == null || original.isBlank()) {
            return "foto";
        }
        String nombre = original.replace('\\', '/');
        nombre = nombre.substring(nombre.lastIndexOf('/') + 1).trim();
        if (nombre.isEmpty()) {
            return "foto";
        }
        return nombre.length() > 255 ? nombre.substring(0, 255) : nombre;
    }
}
