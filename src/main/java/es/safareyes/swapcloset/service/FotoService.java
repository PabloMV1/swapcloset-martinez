package es.safareyes.swapcloset.service;

import es.safareyes.swapcloset.dto.response.FotoResponse;
import es.safareyes.swapcloset.security.Actor;
import org.springframework.web.multipart.MultipartFile;

public interface FotoService {

    FotoResponse subir(Actor actor, Long prendaId, MultipartFile fichero);

    /** actor puede ser null: las fotos de prendas públicas se ven sin cuenta. */
    FotoDescarga descargar(Actor actor, Long fotoId);
}
