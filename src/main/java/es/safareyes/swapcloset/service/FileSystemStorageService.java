package es.safareyes.swapcloset.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
public class FileSystemStorageService implements StorageService {

    /** Única forma de clave que se acepta: así es imposible escapar de la carpeta (../, rutas absolutas...). */
    private static final Pattern CLAVE_VALIDA =
            Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png|webp)$");

    private final Path carpeta;

    public FileSystemStorageService(@Value("${app.storage.dir}") String directorio) {
        this.carpeta = Path.of(directorio).toAbsolutePath().normalize();
        try {
            Files.createDirectories(carpeta);
        } catch (IOException e) {
            throw new UncheckedIOException("No se puede crear la carpeta de ficheros " + carpeta, e);
        }
    }

    @Override
    public String guardar(MultipartFile fichero, String extension) {
        String clave = UUID.randomUUID() + "." + extension;
        try (InputStream entrada = fichero.getInputStream()) {
            Files.copy(entrada, carpeta.resolve(clave), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el fichero", e);
        }
        return clave;
    }

    @Override
    public Optional<Resource> cargar(String clave) {
        if (clave == null || !CLAVE_VALIDA.matcher(clave).matches()) {
            return Optional.empty();
        }
        Path ruta = carpeta.resolve(clave).normalize();
        if (!ruta.startsWith(carpeta) || !Files.isReadable(ruta)) {
            return Optional.empty();
        }
        return Optional.of(new FileSystemResource(ruta));
    }

    @Override
    public void borrar(String clave) {
        if (clave == null || !CLAVE_VALIDA.matcher(clave).matches()) {
            return;
        }
        try {
            Files.deleteIfExists(carpeta.resolve(clave).normalize());
        } catch (IOException e) {
            log.warn("No se pudo borrar el fichero {}: {}", clave, e.getMessage());
        }
    }
}
