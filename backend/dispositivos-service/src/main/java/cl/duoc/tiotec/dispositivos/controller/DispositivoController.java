package cl.duoc.tiotec.dispositivos.controller;

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

import cl.duoc.tiotec.dispositivos.dto.ArchivadoRequest;
import cl.duoc.tiotec.dispositivos.dto.DispositivoRequest;
import cl.duoc.tiotec.dispositivos.dto.DispositivoResponse;
import cl.duoc.tiotec.dispositivos.model.SistemaOperativo;
import cl.duoc.tiotec.dispositivos.service.DispositivoService;
import jakarta.validation.Valid;

// #controlador REST de dispositivos
@RestController
@RequestMapping("/api/dispositivos")
public class DispositivoController {

    private final DispositivoService servicio;

    public DispositivoController(DispositivoService servicio) {
        this.servicio = servicio;
    }

    // #endpoint crear dispositivo (POST)
    @PostMapping
    public ResponseEntity<DispositivoResponse> crear(@Valid @RequestBody DispositivoRequest datos) {
        DispositivoResponse creado = servicio.crear(datos);
        return ResponseEntity.created(URI.create("/api/dispositivos/" + creado.id())).body(creado);
    }

    // #endpoint listar dispositivos (GET)
    @GetMapping
    public List<DispositivoResponse> listar(@RequestParam(required = false) SistemaOperativo sistemaOperativo,
                                            @RequestParam(required = false) Boolean archivado) {
        return servicio.listar(sistemaOperativo, archivado);
    }

    // #endpoint obtener dispositivo por id (GET)
    @GetMapping("/{id}")
    public DispositivoResponse obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    // #endpoint editar dispositivo (PUT)
    @PutMapping("/{id}")
    public DispositivoResponse actualizar(@PathVariable Long id, @Valid @RequestBody DispositivoRequest datos) {
        return servicio.actualizar(id, datos);
    }

    // #endpoint archivar dispositivo (PATCH)
    @PatchMapping("/{id}/archivado")
    public DispositivoResponse cambiarArchivado(@PathVariable Long id, @Valid @RequestBody ArchivadoRequest cuerpo) {
        return servicio.cambiarArchivado(id, cuerpo.archivado());
    }

    // #endpoint eliminar dispositivo (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
