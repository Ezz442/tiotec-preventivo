package cl.duoc.tiotec.dispositivos.exception;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Formato unico de error para toda la API. `campos` solo aparece en errores de validacion. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        long timestamp,
        int status,
        String error,
        String mensaje,
        String ruta,
        Map<String, String> campos) {
}
