package es.safareyes.swapcloset.exception;

/** 422: la petición es correcta pero viola una regla de negocio. */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
