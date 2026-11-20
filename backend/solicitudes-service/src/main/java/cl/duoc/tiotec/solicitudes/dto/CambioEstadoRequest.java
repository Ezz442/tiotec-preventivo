package cl.duoc.tiotec.solicitudes.dto;

import cl.duoc.tiotec.solicitudes.model.EstadoSolicitud;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Cuerpo de PATCH /api/solicitudes/{id}/estado. */
public record CambioEstadoRequest(
        @NotNull(message = "Indica el nuevo estado.")
        EstadoSolicitud estado,

        @Size(max = 100, message = "La nota admite hasta 100 caracteres.")
        String nota) {
}
