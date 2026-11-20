package cl.duoc.tiotec.solicitudes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.tiotec.solicitudes.model.Categoria;
import cl.duoc.tiotec.solicitudes.model.EstadoSolicitud;
import cl.duoc.tiotec.solicitudes.model.Solicitud;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findAllByOrderByCreadaEnDescIdDesc();

    List<Solicitud> findByCategoriaOrderByCreadaEnDescIdDesc(Categoria categoria);

    List<Solicitud> findByEstadoOrderByCreadaEnDescIdDesc(EstadoSolicitud estado);

    List<Solicitud> findByCategoriaAndEstadoOrderByCreadaEnDescIdDesc(Categoria categoria, EstadoSolicitud estado);
}
