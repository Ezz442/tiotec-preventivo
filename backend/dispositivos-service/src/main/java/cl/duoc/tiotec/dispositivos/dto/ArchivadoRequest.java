package cl.duoc.tiotec.dispositivos.dto;

import jakarta.validation.constraints.NotNull;

/** Cuerpo de PATCH /api/dispositivos/{id}/archivado. */
public record ArchivadoRequest(
        @NotNull(message = "Indica si el dispositivo queda archivado (true) o activo (false).")
        Boolean archivado) {
}
