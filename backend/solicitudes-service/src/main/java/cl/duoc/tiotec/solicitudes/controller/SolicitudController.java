package cl.duoc.tiotec.solicitudes.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.tiotec.solicitudes.dto.CambioEstadoRequest;
import cl.duoc.tiotec.solicitudes.dto.HistorialResponse;
import cl.duoc.tiotec.solicitudes.dto.SolicitudRequest;
import cl.duoc.tiotec.solicitudes.dto.SolicitudResponse;
import cl.duoc.tiotec.solicitudes.model.Categoria;
import cl.duoc.tiotec.solicitudes.model.EstadoSolicitud;
import cl.duoc.tiotec.solicitudes.service.SolicitudService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

    private final SolicitudService servicio;

    public SolicitudController(SolicitudService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<SolicitudResponse> crear(@Valid @RequestBody SolicitudRequest datos) {
        SolicitudResponse creada = servicio.crear(datos);
        return ResponseEntity.created(URI.create("/api/solicitudes/" + creada.id())).body(creada);
    }

    @GetMapping
    public List<SolicitudResponse> listar(@RequestParam(required = false) Categoria categoria,
                                          @RequestParam(required = false) EstadoSolicitud estado) {
        return servicio.listar(categoria, estado);
    }

    @GetMapping("/{id}")
    public SolicitudResponse obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @GetMapping("/{id}/historial")
    public List<HistorialResponse> historial(@PathVariable Long id) {
        return servicio.historial(id);
    }

    @PutMapping("/{id}")
    public SolicitudResponse actualizar(@PathVariable Long id, @Valid @RequestBody SolicitudRequest datos) {
        return servicio.actualizar(id, datos);
    }

    @PatchMapping("/{id}/estado")
    public SolicitudResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoRequest cambio) {
        return servicio.cambiarEstado(id, cambio);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
