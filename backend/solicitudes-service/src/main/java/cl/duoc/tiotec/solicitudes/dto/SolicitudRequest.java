package cl.duoc.tiotec.solicitudes.dto;

import cl.duoc.tiotec.solicitudes.model.Categoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo para crear (POST) o editar (PUT) una solicitud.
 * Mismas reglas que el formulario de la app: alias ficticio de 2 a 30 caracteres,
 * sin correos ni numeros largos (telefonos, RUT).
 */
public record SolicitudRequest(
        @NotBlank(message = "Ingresa un alias ficticio.")
        @Size(min = 2, max = 30, message = "El alias debe tener entre 2 y 30 caracteres.")
        @Pattern(regexp = "^(?!.*@)(?!(?:\\D*\\d){7}).*$",
                message = "Usa un alias ficticio: no ingreses correos, telefonos ni RUT.")
        String alias,

        @NotNull(message = "Selecciona una categoria.")
        Categoria categoria) {

    public SolicitudRequest {
        // Se normaliza antes de validar: sin espacios en los bordes y uno solo entre palabras.
        alias = alias == null ? null : alias.trim().replaceAll("\\s+", " ");
    }
}
