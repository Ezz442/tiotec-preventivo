package cl.duoc.tiotec.dispositivos.exception;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ApiExceptionHandler {

    private final Clock reloj;

    public ApiExceptionHandler(Clock reloj) {
        this.reloj = reloj;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return responder(HttpStatus.BAD_REQUEST, "Hay campos con errores.", req, campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> cuerpoInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST,
                "Cuerpo invalido o con valores no permitidos (revisa los valores de sistemaOperativo y archivado).", req, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> parametroInvalido(MethodArgumentTypeMismatchException ex,
                                                           HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST,
                "Valor no permitido para el parametro '" + ex.getName() + "'.", req, null);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest req) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> reglaNegocio(ReglaNegocioException ex, HttpServletRequest req) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus status, String mensaje, HttpServletRequest req,
                                                    Map<String, String> campos) {
        ErrorResponse cuerpo = new ErrorResponse(reloj.millis(), status.value(), status.getReasonPhrase(),
                mensaje, req.getRequestURI(), campos);
        return ResponseEntity.status(status).body(cuerpo);
    }
}
