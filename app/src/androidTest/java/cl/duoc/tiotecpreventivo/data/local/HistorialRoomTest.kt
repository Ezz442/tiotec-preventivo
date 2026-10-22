package cl.duoc.tiotecpreventivo.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.tiotecpreventivo.data.catalogo.CatalogoLocal
import cl.duoc.tiotecpreventivo.data.catalogo.CatalogoRepositoryLocal
import cl.duoc.tiotecpreventivo.data.catalogo.ValorRespuesta
import cl.duoc.tiotecpreventivo.data.local.entity.ExperienciaDigital
import cl.duoc.tiotecpreventivo.data.local.entity.NivelRiesgo
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo
import cl.duoc.tiotecpreventivo.domain.HistorialLogica
import cl.duoc.tiotecpreventivo.domain.ReglasRiesgo
import cl.duoc.tiotecpreventivo.domain.ResultadoEvaluacionMapper
import cl.duoc.tiotecpreventivo.domain.ResultadoReglas
import cl.duoc.tiotecpreventivo.repository.DispositivoRepository
import cl.duoc.tiotecpreventivo.repository.EvaluacionRepository
import cl.duoc.tiotecpreventivo.repository.PerfilRepository
import cl.duoc.tiotecpreventivo.repository.ResultadoEliminacion
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TT-14: historial con varias evaluaciones, sin sobrescribir lo guardado al editar o
 * archivar el dispositivo, vacío y después de cerrar y reabrir la base.
 * Ejecutar con un emulador encendido: clic derecho → Run 'HistorialRoomTest'.
 */
@RunWith(AndroidJUnit4::class)
class HistorialRoomTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val nombreBase = "tiotec_prueba_historial.db"
    private val catalogo = CatalogoRepositoryLocal()
    private val reglas = ReglasRiesgo(catalogo)
    private val mapper = ResultadoEvaluacionMapper(catalogo)

    private var ahora = 1_000_000L
    private lateinit var db: TioTecDatabase
    private lateinit var perfiles: PerfilRepository
    private lateinit var dispositivos: DispositivoRepository
    private lateinit var evaluaciones: EvaluacionRepository

    private fun abrir() {
        db = TioTecDatabase.construir(context, nombreBase)
        perfiles = PerfilRepository(db.perfilDao()) { ahora }
        dispositivos = DispositivoRepository(db.dispositivoDao()) { ahora }
        evaluaciones = EvaluacionRepository(db.evaluacionDao(), db.dispositivoDao(), catalogo) { ahora }
    }

    @Before
    fun crearBase() {
        context.deleteDatabase(nombreBase)
        abrir()
    }

    @After
    fun cerrarBase() {
        db.close()
        context.deleteDatabase(nombreBase)
    }

    private fun respuestas(base: ValorRespuesta, vararg cambios: Pair<String, ValorRespuesta>) =
        CatalogoLocal.preguntas.associate { it.id to base } + cambios

    /** Igual que EvaluacionViewModel.finalizarEvaluacion (TT-11). */
    private suspend fun evaluar(dispositivoId: Long, respuestas: Map<String, ValorRespuesta>): Long {
        ahora += 60_000
        val completa = reglas.evaluar(respuestas) as ResultadoReglas.Completa
        return evaluaciones.guardarEvaluacion(dispositivoId, respuestas, completa.resultados)
    }

    private suspend fun crearDispositivo(nombre: String, so: SistemaOperativo): Long {
        val perfilId = perfiles.guardar("Tío Prueba", ExperienciaDigital.PRINCIPIANTE)
        return dispositivos.crear(perfilId, nombre, so)
    }

    @Test
    fun historialVacio(): Unit = runBlocking {
        assertTrue(evaluaciones.observarHistorial().first().isEmpty())
    }

    @Test
    fun variasEvaluacionesPorFechaYDispositivo(): Unit = runBlocking {
        val telefono = crearDispositivo("Teléfono de prueba", SistemaOperativo.ANDROID)
        val computador = crearDispositivo("Computador de prueba", SistemaOperativo.WINDOWS)

        val primera = evaluar(telefono, respuestas(ValorRespuesta.NO))
        val segunda = evaluar(computador, respuestas(ValorRespuesta.SI))
        val tercera = evaluar(telefono, respuestas(ValorRespuesta.NO_SE))

        val historial = evaluaciones.observarHistorial().first()
        assertEquals(listOf(tercera, segunda, primera), historial.map { it.id })
        assertEquals(
            listOf(NivelRiesgo.RIESGO_MEDIO, NivelRiesgo.RIESGO_BAJO, NivelRiesgo.RIESGO_ALTO),
            historial.map { it.nivelGlobal }
        )

        assertEquals(listOf(tercera, primera), HistorialLogica.filtrar(historial, telefono).map { it.id })
        assertEquals(listOf(segunda), HistorialLogica.filtrar(historial, computador).map { it.id })
    }

    @Test
    fun editarElDispositivoNoSobrescribeElHistorial(): Unit = runBlocking {
        val id = crearDispositivo("Teléfono de la abuela", SistemaOperativo.ANDROID)
        val antigua = evaluar(id, respuestas(ValorRespuesta.SI, "AUT-01" to ValorRespuesta.NO))
        val detalleAntes = mapper.construir(evaluaciones.obtenerDetalle(antigua)!!)

        dispositivos.actualizar(id, "Tablet de la abuela", SistemaOperativo.IOS)
        val nueva = evaluar(id, respuestas(ValorRespuesta.SI))

        val detalleDespues = mapper.construir(evaluaciones.obtenerDetalle(antigua)!!)
        assertEquals(detalleAntes, detalleDespues)
        assertEquals("Teléfono de la abuela", detalleDespues.nombreDispositivo)
        assertEquals(SistemaOperativo.ANDROID, detalleDespues.sistemaOperativo)
        assertEquals(ValorRespuesta.NO, detalleDespues.respuestas.single { it.preguntaId == "AUT-01" }.respuesta)

        val historial = evaluaciones.observarHistorial().first()
        assertEquals(listOf("Tablet de la abuela", "Teléfono de la abuela"), historial.map { it.nombreDispositivo })
        assertEquals(nueva, historial.first().id)
        // El filtro muestra el nombre actual del dispositivo.
        assertEquals("Tablet de la abuela", HistorialLogica.filtros(historial).single().nombre)
    }

    @Test
    fun renombrarSinVolverAEvaluarCambiaElFiltroYNoLasEvaluaciones(): Unit = runBlocking {
        // Caso de la revisión de Gerard en TT-14.
        val id = crearDispositivo("iaintegradajorge", SistemaOperativo.ANDROID)
        evaluar(id, respuestas(ValorRespuesta.NO))
        evaluar(id, respuestas(ValorRespuesta.SI))

        dispositivos.actualizar(id, "superiajorge", SistemaOperativo.ANDROID)

        val historial = evaluaciones.observarHistorial().first()
        val nombres = dispositivos.observarTodos().first().associate { it.id to it.nombre }
        assertEquals("superiajorge", HistorialLogica.filtros(historial, nombres).single().nombre)
        assertTrue(historial.all { it.nombreDispositivo == "iaintegradajorge" })
    }

    @Test
    fun eliminarDispositivoConEvaluacionesConservaElHistorial(): Unit = runBlocking {
        val id = crearDispositivo("Computador antiguo", SistemaOperativo.WINDOWS)
        val evaluacionId = evaluar(id, respuestas(ValorRespuesta.NO))

        assertEquals(ResultadoEliminacion.Archivado, dispositivos.eliminar(id))

        assertTrue(dispositivos.observarActivos().first().none { it.id == id })
        // Archivado sigue existiendo, así que el filtro conserva su nombre.
        assertEquals("Computador antiguo", dispositivos.observarTodos().first().single { it.id == id }.nombre)
        val historial = evaluaciones.observarHistorial().first()
        assertEquals(listOf(evaluacionId), historial.map { it.id })
        val detalle = mapper.construir(evaluaciones.obtenerDetalle(evaluacionId)!!)
        assertEquals(CatalogoLocal.preguntas.size, detalle.respuestas.size)
        assertEquals(NivelRiesgo.RIESGO_ALTO, detalle.nivelGlobal)
    }

    @Test
    fun historialSobreviveAlCerrarYReabrir(): Unit = runBlocking {
        val id = crearDispositivo("Teléfono de prueba", SistemaOperativo.ANDROID)
        evaluar(id, respuestas(ValorRespuesta.NO))
        evaluar(id, respuestas(ValorRespuesta.SI))
        val antes = evaluaciones.observarHistorial().first()
        val detallesAntes = antes.map { mapper.construir(evaluaciones.obtenerDetalle(it.id)!!) }

        db.close()
        abrir()

        val despues = evaluaciones.observarHistorial().first()
        assertEquals(antes, despues)
        assertEquals(detallesAntes, despues.map { mapper.construir(evaluaciones.obtenerDetalle(it.id)!!) })
    }
}
