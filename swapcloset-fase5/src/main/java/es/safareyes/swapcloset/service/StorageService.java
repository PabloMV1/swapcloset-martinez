package es.safareyes.swapcloset.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/** Guarda y recupera ficheros en disco. Los ficheros se identifican por una clave aleatoria, nunca por su nombre original. */
public interface StorageService {

    /** Guarda el fichero con un nombre aleatorio (UUID + extensión) y devuelve esa clave. */
    String guardar(MultipartFile fichero, String extension);

    /** Devuelve el fichero, o vacío si la clave no es válida o el fichero no existe. */
    Optional<Resource> cargar(String clave);

    /** Borra el fichero si existe (se usa para deshacer una subida cuando la base de datos falla). */
    void borrar(String clave);
}
