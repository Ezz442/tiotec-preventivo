package cl.duoc.tiotec.dispositivos.dto;

import cl.duoc.tiotec.dispositivos.model.Dispositivo;
import cl.duoc.tiotec.dispositivos.model.SistemaOperativo;

public record DispositivoResponse(
        Long id,
        String nombre,
        SistemaOperativo sistemaOperativo,
        boolean archivado,
        long creadoEn,
        long actualizadoEn) {

    public static DispositivoResponse desde(Dispositivo d) {
        return new DispositivoResponse(d.getId(), d.getNombre(), d.getSistemaOperativo(),
                d.isArchivado(), d.getCreadoEn(), d.getActualizadoEn());
    }
}
