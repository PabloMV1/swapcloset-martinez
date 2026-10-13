package es.safareyes.swapcloset.exception;

/** 403: recurso ajeno o rol insuficiente. */
public class ProhibidoException extends RuntimeException {
    public ProhibidoException(String mensaje) {
        super(mensaje);
    }
}
