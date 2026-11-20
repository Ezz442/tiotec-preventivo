package cl.duoc.tiotec.solicitudes.dto;

import cl.duoc.tiotec.solicitudes.model.EstadoSolicitud;
import cl.duoc.tiotec.solicitudes.model.HistorialSolicitud;

public record HistorialResponse(Long id, EstadoSolicitud estado, long fecha, String nota) {

    public static HistorialResponse desde(HistorialSolicitud h) {
        return new HistorialResponse(h.getId(), h.getEstado(), h.getFecha(), h.getNota());
    }
}
