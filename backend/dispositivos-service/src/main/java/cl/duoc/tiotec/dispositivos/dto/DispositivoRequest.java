package cl.duoc.tiotec.dispositivos.dto;

import cl.duoc.tiotec.dispositivos.model.SistemaOperativo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo para crear (POST) o editar (PUT) un dispositivo.
 * Mismas reglas que el formulario de la app: nombre de 2 a 30 caracteres,
 * sin correos ni numeros largos (telefonos, RUT).
 */
public record DispositivoRequest(
        // #validacion del nombre del dispositivo
        @NotBlank(message = "Ingresa un nombre ficticio.")
        @Size(min = 2, max = 30, message = "El nombre debe tener entre 2 y 30 caracteres.")
        @Pattern(regexp = "^(?!.*@)(?!(?:\\D*\\d){7}).*$",
                message = "Usa un nombre de prueba: no ingreses correos, telefonos ni RUT.")
        String nombre,

        @NotNull(message = "Selecciona un sistema operativo.")
        SistemaOperativo sistemaOperativo) {

    public DispositivoRequest {
        // Se normaliza antes de validar: sin espacios en los bordes y uno solo entre palabras.
        nombre = nombre == null ? null : nombre.trim().replaceAll("\\s+", " ");
    }
}
