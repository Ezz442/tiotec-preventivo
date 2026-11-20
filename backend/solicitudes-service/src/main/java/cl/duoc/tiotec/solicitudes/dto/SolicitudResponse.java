package cl.duoc.tiotec.solicitudes.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import cl.duoc.tiotec.solicitudes.model.Categoria;
import cl.duoc.tiotec.solicitudes.model.EstadoSolicitud;
import cl.duoc.tiotec.solicitudes.model.Solicitud;

/** Respuesta de la API. En los listados `historial` se omite (null) para no cargarlo. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SolicitudResponse(
        Long id,
        String codigo,
        String alias,
        Categoria categoria,
        EstadoSolicitud estado,
        long creadaEn,
        long actualizadaEn,
        List<HistorialResponse> historial) {

    public static SolicitudResponse desde(Solicitud s, boolean incluirHistorial) {
        List<HistorialResponse> registros = incluirHistorial
                ? s.getHistorial().stream().map(HistorialResponse::desde).toList()
                : null;
        return new SolicitudResponse(s.getId(), s.getCodigo(), s.getAlias(), s.getCategoria(),
                s.getEstado(), s.getCreadaEn(), s.getActualizadaEn(), registros);
    }
}
