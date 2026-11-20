package cl.duoc.tiotec.dispositivos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.tiotec.dispositivos.model.Dispositivo;
import cl.duoc.tiotec.dispositivos.model.SistemaOperativo;

public interface DispositivoRepository extends JpaRepository<Dispositivo, Long> {

    List<Dispositivo> findAllByOrderByCreadoEnDescIdDesc();

    List<Dispositivo> findBySistemaOperativoOrderByCreadoEnDescIdDesc(SistemaOperativo sistemaOperativo);

    List<Dispositivo> findByArchivadoOrderByCreadoEnDescIdDesc(boolean archivado);

    List<Dispositivo> findBySistemaOperativoAndArchivadoOrderByCreadoEnDescIdDesc(
            SistemaOperativo sistemaOperativo, boolean archivado);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
