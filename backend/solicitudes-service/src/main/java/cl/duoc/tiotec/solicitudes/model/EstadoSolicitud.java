package cl.duoc.tiotec.solicitudes.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Estados de una solicitud (mismos nombres que EstadoSolicitud en la app).
 * Transiciones: PENDIENTE -> EN_REVISION | CANCELADA, EN_REVISION -> RESUELTA | CANCELADA.
 * RESUELTA y CANCELADA son estados finales.
 */
public enum EstadoSolicitud {
    PENDIENTE,
    EN_REVISION,
    RESUELTA,
    CANCELADA;

    public Set<EstadoSolicitud> siguientesPermitidos() {
        return switch (this) {
            case PENDIENTE -> EnumSet.of(EN_REVISION, CANCELADA);
            case EN_REVISION -> EnumSet.of(RESUELTA, CANCELADA);
            case RESUELTA, CANCELADA -> EnumSet.noneOf(EstadoSolicitud.class);
        };
    }

    public boolean puedePasarA(EstadoSolicitud destino) {
        return siguientesPermitidos().contains(destino);
    }
}
