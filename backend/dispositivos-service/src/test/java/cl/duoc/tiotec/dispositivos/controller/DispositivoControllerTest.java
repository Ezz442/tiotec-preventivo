package cl.duoc.tiotec.dispositivos.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

// #pruebas de los endpoints de dispositivos (base H2 en memoria)
/** Pruebas de punta a punta de los endpoints (controlador + servicio + H2 en memoria). */
@SpringBootTest
@AutoConfigureMockMvc
class DispositivoControllerTest {

    private static final AtomicInteger CONTADOR = new AtomicInteger();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    /** Nombre distinto en cada llamada: el nombre es unico y la base se comparte entre pruebas. */
    private static String nombreUnico(String base) {
        return base + " " + CONTADOR.incrementAndGet();
    }

    private static String cuerpo(String nombre, String sistema) {
        return "{\"nombre\":\"" + nombre + "\",\"sistemaOperativo\":\"" + sistema + "\"}";
    }

    private long crear(String nombre, String sistema) throws Exception {
        String respuesta = mvc.perform(post("/api/dispositivos")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo(nombre, sistema)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(respuesta).get("id").asLong();
    }

    private void archivar(long id, boolean archivado, int httpEsperado) throws Exception {
        mvc.perform(patch("/api/dispositivos/" + id + "/archivado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"archivado\":" + archivado + "}"))
                .andExpect(status().is(httpEsperado));
    }

    @Test
    void crearDevuelve201ConNombreNormalizadoYActivo() throws Exception {
        mvc.perform(post("/api/dispositivos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("  Telefono   de   Ana " + CONTADOR.incrementAndGet(), "ANDROID")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.nombre").value(org.hamcrest.Matchers.startsWith("Telefono de Ana")))
                .andExpect(jsonPath("$.sistemaOperativo").value("ANDROID"))
                .andExpect(jsonPath("$.archivado").value(false));
    }

    @Test
    void crearConDatosInvalidosDevuelve400ConErroresPorCampo() throws Exception {
        mvc.perform(post("/api/dispositivos")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"a\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nombre").exists())
                .andExpect(jsonPath("$.campos.sistemaOperativo").exists());
    }

    @Test
    void crearRechazaNombreConCorreoOTelefono() throws Exception {
        for (String nombre : new String[] {"pepe@correo.cl", "fono 912345678"}) {
            mvc.perform(post("/api/dispositivos")
                            .contentType(MediaType.APPLICATION_JSON).content(cuerpo(nombre, "IOS")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campos.nombre").exists());
        }
    }

    @Test
    void crearConSistemaDesconocidoDevuelve400() throws Exception {
        mvc.perform(post("/api/dispositivos")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo("Equipo", "LINUX")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nombreRepetidoDevuelve409SinImportarMayusculas() throws Exception {
        String nombre = nombreUnico("Repetido");
        crear(nombre, "WINDOWS");

        mvc.perform(post("/api/dispositivos")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo(nombre.toUpperCase(), "MACOS")))
                .andExpect(status().isConflict());
    }

    @Test
    void obtenerInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/dispositivos/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void listarPermiteFiltrarPorSistemaYArchivado() throws Exception {
        long id = crear(nombreUnico("Filtro"), "OTRO");
        archivar(id, true, 200);

        mvc.perform(get("/api/dispositivos").param("sistemaOperativo", "OTRO").param("archivado", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));

        mvc.perform(get("/api/dispositivos").param("sistemaOperativo", "LINUX"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarCambiaLosDatosPeroNoSePuedeSiEstaArchivado() throws Exception {
        long id = crear(nombreUnico("Editable"), "ANDROID");
        String nuevo = nombreUnico("Editado");

        mvc.perform(put("/api/dispositivos/" + id)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo(nuevo, "IOS")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value(nuevo))
                .andExpect(jsonPath("$.sistemaOperativo").value("IOS"));

        archivar(id, true, 200);

        mvc.perform(put("/api/dispositivos/" + id)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo(nombreUnico("Otro"), "IOS")))
                .andExpect(status().isConflict());
    }

    @Test
    void archivarYDesarchivarFuncionaYRepetirElMismoEstadoDevuelve409() throws Exception {
        long id = crear(nombreUnico("Archivo"), "MACOS");

        archivar(id, false, 409);  // ya estaba activo
        archivar(id, true, 200);
        archivar(id, true, 409);   // ya estaba archivado
        archivar(id, false, 200);
    }

    @Test
    void eliminarDevuelve204YLuegoElDispositivoYaNoExiste() throws Exception {
        long id = crear(nombreUnico("Borrar"), "WINDOWS");

        mvc.perform(delete("/api/dispositivos/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/dispositivos/" + id)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/dispositivos/" + id)).andExpect(status().isNotFound());
    }
}
