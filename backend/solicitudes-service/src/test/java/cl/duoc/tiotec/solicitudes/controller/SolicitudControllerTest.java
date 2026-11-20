package cl.duoc.tiotec.solicitudes.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Pruebas de punta a punta de los endpoints (controlador + servicio + H2 en memoria). */
@SpringBootTest
@AutoConfigureMockMvc
class SolicitudControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    private long crear(String alias, String categoria) throws Exception {
        String respuesta = mvc.perform(post("/api/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alias\":\"" + alias + "\",\"categoria\":\"" + categoria + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(respuesta).get("id").asLong();
    }

    private void cambiarEstado(long id, String estado, int httpEsperado) throws Exception {
        mvc.perform(patch("/api/solicitudes/" + id + "/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"" + estado + "\"}"))
                .andExpect(status().is(httpEsperado));
    }

    @Test
    void crearDevuelve201ConCodigoEstadoPendienteEHistorialInicial() throws Exception {
        mvc.perform(post("/api/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alias\":\"  Tio   Pepe \",\"categoria\":\"RESPALDOS\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.codigo", matchesPattern("DEMO-\\d{3,}")))
                .andExpect(jsonPath("$.alias").value("Tio Pepe"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.historial", hasSize(1)));
    }

    @Test
    void crearConDatosInvalidosDevuelve400ConErroresPorCampo() throws Exception {
        mvc.perform(post("/api/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alias\":\"a\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.alias").exists())
                .andExpect(jsonPath("$.campos.categoria").exists());
    }

    @Test
    void crearRechazaAliasConCorreoOTelefono() throws Exception {
        for (String alias : new String[] {"pepe@correo.cl", "mi fono 912345678"}) {
            mvc.perform(post("/api/solicitudes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"alias\":\"" + alias + "\",\"categoria\":\"AUTENTICACION\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campos.alias").exists());
        }
    }

    @Test
    void crearConCategoriaDesconocidaDevuelve400() throws Exception {
        mvc.perform(post("/api/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alias\":\"Ana\",\"categoria\":\"OTRA\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/solicitudes/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void listarPermiteFiltrarPorCategoriaYEstado() throws Exception {
        long id = crear("Filtro", "ACTUALIZACIONES");
        cambiarEstado(id, "EN_REVISION", 200);

        mvc.perform(get("/api/solicitudes").param("categoria", "ACTUALIZACIONES").param("estado", "EN_REVISION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].historial").doesNotExist());

        mvc.perform(get("/api/solicitudes").param("estado", "INVALIDO"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cambiarEstadoRespetaLasTransicionesYRegistraHistorial() throws Exception {
        long id = crear("Flujo", "RECUPERACION");

        cambiarEstado(id, "RESUELTA", 409);   // PENDIENTE no puede ir directo a RESUELTA
        cambiarEstado(id, "EN_REVISION", 200);
        cambiarEstado(id, "RESUELTA", 200);
        cambiarEstado(id, "CANCELADA", 409);  // RESUELTA es final

        mvc.perform(get("/api/solicitudes/" + id + "/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[2].estado").value("RESUELTA"));
    }

    @Test
    void actualizarSoloFuncionaMientrasEstaPendiente() throws Exception {
        long id = crear("Editable", "RESPALDOS");

        mvc.perform(put("/api/solicitudes/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alias\":\"Editado\",\"categoria\":\"AUTENTICACION\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alias").value("Editado"))
                .andExpect(jsonPath("$.categoria").value("AUTENTICACION"));

        cambiarEstado(id, "EN_REVISION", 200);

        mvc.perform(put("/api/solicitudes/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alias\":\"Otro\",\"categoria\":\"RESPALDOS\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void eliminarDevuelve204YLuegoLaSolicitudYaNoExiste() throws Exception {
        long id = crear("Borrar", "RESPALDOS");

        mvc.perform(delete("/api/solicitudes/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/solicitudes/" + id)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/solicitudes/" + id)).andExpect(status().isNotFound());
    }
}
