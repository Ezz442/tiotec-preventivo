package cl.duoc.tiotec.dispositivos.service;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.tiotec.dispositivos.dto.DispositivoRequest;
import cl.duoc.tiotec.dispositivos.dto.DispositivoResponse;
import cl.duoc.tiotec.dispositivos.exception.RecursoNoEncontradoException;
import cl.duoc.tiotec.dispositivos.exception.ReglaNegocioException;
import cl.duoc.tiotec.dispositivos.model.Dispositivo;
import cl.duoc.tiotec.dispositivos.model.SistemaOperativo;
import cl.duoc.tiotec.dispositivos.repository.DispositivoRepository;

/** Logica de negocio de los dispositivos: el controlador solo traduce HTTP. */
@Service
@Transactional
public class DispositivoService {

    static final String NOMBRE_REPETIDO = "Ya existe un dispositivo con ese nombre.";

    private final DispositivoRepository repositorio;
    private final Clock reloj;

    public DispositivoService(DispositivoRepository repositorio, Clock reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    public DispositivoResponse crear(DispositivoRequest datos) {
        if (repositorio.existsByNombreIgnoreCase(datos.nombre())) {
            throw new ReglaNegocioException(NOMBRE_REPETIDO);
        }
        Dispositivo dispositivo = new Dispositivo(datos.nombre(), datos.sistemaOperativo(), reloj.millis());
        return DispositivoResponse.desde(repositorio.save(dispositivo));
    }

    @Transactional(readOnly = true)
    public List<DispositivoResponse> listar(SistemaOperativo sistemaOperativo, Boolean archivado) {
        List<Dispositivo> resultado;
        if (sistemaOperativo != null && archivado != null) {
            resultado = repositorio.findBySistemaOperativoAndArchivadoOrderByCreadoEnDescIdDesc(
                    sistemaOperativo, archivado);
        } else if (sistemaOperativo != null) {
            resultado = repositorio.findBySistemaOperativoOrderByCreadoEnDescIdDesc(sistemaOperativo);
        } else if (archivado != null) {
            resultado = repositorio.findByArchivadoOrderByCreadoEnDescIdDesc(archivado);
        } else {
            resultado = repositorio.findAllByOrderByCreadoEnDescIdDesc();
        }
        return resultado.stream().map(DispositivoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public DispositivoResponse obtener(Long id) {
        return DispositivoResponse.desde(buscar(id));
    }

    /** Un dispositivo archivado no se edita: primero hay que desarchivarlo. */
    public DispositivoResponse actualizar(Long id, DispositivoRequest datos) {
        Dispositivo dispositivo = buscar(id);
        if (dispositivo.isArchivado()) {
            throw new ReglaNegocioException("No se puede editar un dispositivo archivado.");
        }
        if (repositorio.existsByNombreIgnoreCaseAndIdNot(datos.nombre(), id)) {
            throw new ReglaNegocioException(NOMBRE_REPETIDO);
        }
        dispositivo.setNombre(datos.nombre());
        dispositivo.setSistemaOperativo(datos.sistemaOperativo());
        dispositivo.setActualizadoEn(reloj.millis());
        return DispositivoResponse.desde(dispositivo);
    }

    public DispositivoResponse cambiarArchivado(Long id, boolean archivado) {
        Dispositivo dispositivo = buscar(id);
        if (dispositivo.isArchivado() == archivado) {
            throw new ReglaNegocioException(archivado
                    ? "El dispositivo ya esta archivado."
                    : "El dispositivo ya esta activo.");
        }
        dispositivo.setArchivado(archivado);
        dispositivo.setActualizadoEn(reloj.millis());
        return DispositivoResponse.desde(dispositivo);
    }

    public void eliminar(Long id) {
        repositorio.delete(buscar(id));
    }

    private Dispositivo buscar(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el dispositivo con id " + id + "."));
    }
}
