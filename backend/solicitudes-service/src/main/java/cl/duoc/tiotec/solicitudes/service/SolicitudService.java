package cl.duoc.tiotec.solicitudes.service;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.tiotec.solicitudes.dto.CambioEstadoRequest;
import cl.duoc.tiotec.solicitudes.dto.HistorialResponse;
import cl.duoc.tiotec.solicitudes.dto.SolicitudRequest;
import cl.duoc.tiotec.solicitudes.dto.SolicitudResponse;
import cl.duoc.tiotec.solicitudes.exception.RecursoNoEncontradoException;
import cl.duoc.tiotec.solicitudes.exception.ReglaNegocioException;
import cl.duoc.tiotec.solicitudes.model.Categoria;
import cl.duoc.tiotec.solicitudes.model.EstadoSolicitud;
import cl.duoc.tiotec.solicitudes.model.Solicitud;
import cl.duoc.tiotec.solicitudes.repository.SolicitudRepository;

/** Logica de negocio de las solicitudes: el controlador solo traduce HTTP. */
@Service
@Transactional
public class SolicitudService {

    private final SolicitudRepository repositorio;
    private final Clock reloj;

    public SolicitudService(SolicitudRepository repositorio, Clock reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    public SolicitudResponse crear(SolicitudRequest datos) {
        long ahora = reloj.millis();
        Solicitud solicitud = new Solicitud(datos.alias(), datos.categoria(), EstadoSolicitud.PENDIENTE, ahora);
        solicitud.agregarHistorial(EstadoSolicitud.PENDIENTE, ahora, "Solicitud creada");
        solicitud = repositorio.save(solicitud);
        solicitud.setCodigo(String.format("DEMO-%03d", solicitud.getId()));
        return SolicitudResponse.desde(solicitud, true);
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponse> listar(Categoria categoria, EstadoSolicitud estado) {
        List<Solicitud> resultado;
        if (categoria != null && estado != null) {
            resultado = repositorio.findByCategoriaAndEstadoOrderByCreadaEnDescIdDesc(categoria, estado);
        } else if (categoria != null) {
            resultado = repositorio.findByCategoriaOrderByCreadaEnDescIdDesc(categoria);
        } else if (estado != null) {
            resultado = repositorio.findByEstadoOrderByCreadaEnDescIdDesc(estado);
        } else {
            resultado = repositorio.findAllByOrderByCreadaEnDescIdDesc();
        }
        return resultado.stream().map(s -> SolicitudResponse.desde(s, false)).toList();
    }

    @Transactional(readOnly = true)
    public SolicitudResponse obtener(Long id) {
        return SolicitudResponse.desde(buscar(id), true);
    }

    @Transactional(readOnly = true)
    public List<HistorialResponse> historial(Long id) {
        return buscar(id).getHistorial().stream().map(HistorialResponse::desde).toList();
    }

    /** Solo se pueden editar los datos mientras la solicitud sigue pendiente. */
    public SolicitudResponse actualizar(Long id, SolicitudRequest datos) {
        Solicitud solicitud = buscar(id);
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ReglaNegocioException("Solo se puede editar una solicitud en estado PENDIENTE.");
        }
        solicitud.setAlias(datos.alias());
        solicitud.setCategoria(datos.categoria());
        solicitud.setActualizadaEn(reloj.millis());
        return SolicitudResponse.desde(solicitud, true);
    }

    public SolicitudResponse cambiarEstado(Long id, CambioEstadoRequest cambio) {
        Solicitud solicitud = buscar(id);
        EstadoSolicitud actual = solicitud.getEstado();
        EstadoSolicitud destino = cambio.estado();
        if (!actual.puedePasarA(destino)) {
            throw new ReglaNegocioException("No se puede pasar de " + actual + " a " + destino
                    + ". Permitidos: " + actual.siguientesPermitidos() + ".");
        }
        long ahora = reloj.millis();
        String nota = cambio.nota() == null || cambio.nota().isBlank()
                ? "Estado cambiado a " + destino
                : cambio.nota().trim();
        solicitud.setEstado(destino);
        solicitud.setActualizadaEn(ahora);
        solicitud.agregarHistorial(destino, ahora, nota);
        return SolicitudResponse.desde(solicitud, true);
    }

    public void eliminar(Long id) {
        repositorio.delete(buscar(id));
    }

    private Solicitud buscar(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la solicitud con id " + id + "."));
    }
}
