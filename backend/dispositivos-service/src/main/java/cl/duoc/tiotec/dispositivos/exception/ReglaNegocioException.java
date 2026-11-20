package cl.duoc.tiotec.dispositivos.exception;

/** Se traduce a HTTP 409: la peticion es valida pero el estado actual no la permite. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
