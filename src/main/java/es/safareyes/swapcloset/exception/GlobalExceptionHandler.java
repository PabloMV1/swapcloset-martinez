package es.safareyes.swapcloset.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

/** Único punto donde se generan los errores: todos salen como ProblemDetail (RFC 9457). */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public record CampoError(String campo, String mensaje) {
    }

    private ResponseEntity<ProblemDetail> responder(HttpStatus estado, String titulo, String detalle,
                                                    HttpServletRequest request) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setTitle(titulo);
        problema.setInstance(URI.create(request.getRequestURI()));
        problema.setProperty("timestamp", LocalDateTime.now().withNano(0));
        return ResponseEntity.status(estado).body(problema);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ProblemDetail> noEncontrado(RecursoNoEncontradoException e, HttpServletRequest r) {
        return responder(HttpStatus.NOT_FOUND, "Recurso no encontrado", e.getMessage(), r);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ProblemDetail> reglaNegocio(ReglaNegocioException e, HttpServletRequest r) {
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, "Regla de negocio incumplida", e.getMessage(), r);
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ProblemDetail> conflicto(ConflictoException e, HttpServletRequest r) {
        return responder(HttpStatus.CONFLICT, "Conflicto", e.getMessage(), r);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> noAutenticado(AuthenticationException e, HttpServletRequest r) {
        return responder(HttpStatus.UNAUTHORIZED, "No autenticado", e.getMessage(), r);
    }

    @ExceptionHandler({ProhibidoException.class, AccessDeniedException.class})
    public ResponseEntity<ProblemDetail> prohibido(RuntimeException e, HttpServletRequest r) {
        return responder(HttpStatus.FORBIDDEN, "Acceso denegado", e.getMessage(), r);
    }

    @ExceptionHandler(FicheroNoValidoException.class)
    public ResponseEntity<ProblemDetail> ficheroNoValido(FicheroNoValidoException e, HttpServletRequest r) {
        return responder(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Fichero no válido", e.getMessage(), r);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> ficheroGrande(MaxUploadSizeExceededException e, HttpServletRequest r) {
        return responder(HttpStatus.PAYLOAD_TOO_LARGE, "Fichero demasiado grande",
                "El fichero supera el tamaño máximo de 5 MB", r);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> validacion(MethodArgumentNotValidException e, HttpServletRequest r) {
        List<CampoError> errores = e.getBindingResult().getFieldErrors().stream()
                .map(f -> new CampoError(f.getField(), f.getDefaultMessage()))
                .toList();
        ResponseEntity<ProblemDetail> respuesta = responder(HttpStatus.BAD_REQUEST, "Datos no válidos",
                "Hay " + errores.size() + " campo(s) con errores", r);
        respuesta.getBody().setProperty("errores", errores);
        return respuesta;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> restricciones(ConstraintViolationException e, HttpServletRequest r) {
        List<CampoError> errores = e.getConstraintViolations().stream()
                .map(v -> new CampoError(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        ResponseEntity<ProblemDetail> respuesta = responder(HttpStatus.BAD_REQUEST, "Datos no válidos",
                "Hay " + errores.size() + " parámetro(s) con errores", r);
        respuesta.getBody().setProperty("errores", errores);
        return respuesta;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            ServletRequestBindingException.class, PropertyReferenceException.class})
    public ResponseEntity<ProblemDetail> peticionMalFormada(Exception e, HttpServletRequest r) {
        log.debug("Petición mal formada: {}", e.getMessage());
        return responder(HttpStatus.BAD_REQUEST, "Petición mal formada",
                "Revisa el cuerpo, los parámetros o las cabeceras de la petición", r);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetail> tipoNoSoportado(HttpMediaTypeNotSupportedException e, HttpServletRequest r) {
        return responder(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de contenido no soportado", e.getMessage(), r);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> metodoNoPermitido(HttpRequestMethodNotSupportedException e,
                                                           HttpServletRequest r) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, "Método no permitido", e.getMessage(), r);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> rutaInexistente(NoResourceFoundException e, HttpServletRequest r) {
        return responder(HttpStatus.NOT_FOUND, "Recurso no encontrado", "La ruta no existe", r);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> integridad(DataIntegrityViolationException e, HttpServletRequest r) {
        log.warn("Violación de integridad: {}", e.getMostSpecificCause().getMessage());
        return responder(HttpStatus.CONFLICT, "Conflicto de datos",
                "La operación viola una restricción de la base de datos (duplicado o dato relacionado)", r);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> inesperado(Exception e, HttpServletRequest r) {
        log.error("Error inesperado en {}", r.getRequestURI(), e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ha ocurrido un error inesperado", r);
    }
}
