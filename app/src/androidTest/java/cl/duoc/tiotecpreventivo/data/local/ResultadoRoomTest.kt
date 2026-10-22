package cl.duoc.tiotecpreventivo.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.tiotecpreventivo.data.catalogo.CatalogoLocal
import cl.duoc.tiotecpreventivo.data.catalogo.CatalogoRepositoryLocal
import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.data.catalogo.ValorRespuesta
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoAccion
import cl.duoc.tiotecpreventivo.data.local.entity.ExperienciaDigital
import cl.duoc.tiotecpreventivo.data.local.entity.NivelRiesgo
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo
import cl.duoc.tiotecpreventivo.domain.ReglasRiesgo
import cl.duoc.tiotecpreventivo.domain.ResultadoEvaluacionMapper
import cl.duoc.tiotecpreventivo.domain.ResultadoReglas
import cl.duoc.tiotecpreventivo.repository.AccionRepository
import cl.duoc.tiotecpreventivo.repository.DispositivoRepository
import cl.duoc.tiotecpreventivo.repository.EvaluacionRepository
import cl.duoc.tiotecpreventivo.repository.PerfilRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TT-12: el resultado queda guardado junto a su evaluación y consultarlo
 * (pantalla de resultados o historial) no crea registros duplicados.
 * Ejecutar con un emulador encendido: clic derecho → Run 'ResultadoRoomTest'.
 */
@RunWith(AndroidJUnit4::class)
class ResultadoRoomTest {

    private lateinit var db: TioTecDatabase
    private val catalogo = CatalogoRepositoryLocal()
    private val reglas = ReglasRiesgo(catalogo)
    private val mapper = ResultadoEvaluacionMapper(catalogo)

    private lateinit var perfiles: PerfilRepository
    private lateinit var dispositivos: DispositivoRepository
    private lateinit var evaluaciones: EvaluacionRepository
    private lateinit var acciones: AccionRepository

    @Before
    fun crearBase() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TioTecDatabase::class.java
        ).build()
        perfiles = PerfilRepository(db.perfilDao())
        dispositivos = DispositivoRepository(db.dispositivoDao())
        evaluaciones = EvaluacionRepository(db.evaluacionDao(), db.dispositivoDao(), catalogo)
        acciones = AccionRepository(db.accionDao())
    }

    @After
    fun cerrarBase() = db.close()

    private val respuestasMixtas: Map<String, ValorRespuesta> =
        CatalogoLocal.preguntas.associate { it.id to ValorRespuesta.SI } + mapOf(
            "RES-02" to ValorRespuesta.NO, "RES-03" to ValorRespuesta.NO,
            "ACT-03" to ValorRespuesta.NO_SE,
            "AUT-02" to ValorRespuesta.NO, "AUT-03" to ValorRespuesta.NO,
            "RCU-01" to ValorRespuesta.NO, "RCU-02" to ValorRespuesta.NO
        )

    /** Lo mismo que hace EvaluacionViewModel.finalizarEvaluacion (TT-11). */
    private suspend fun guardar(respuestas: Map<String, ValorRespuesta>): Pair<Long, Long> {
        val perfilId = perfiles.guardar("Tío Prueba", ExperienciaDigital.PRINCIPIANTE)
        val dispositivoId = dispositivos.crear(perfilId, "Teléfono de prueba", SistemaOperativo.ANDROID)
        val completa = reglas.evaluar(respuestas) as ResultadoReglas.Completa
        val evaluacionId = evaluaciones.guardarEvaluacion(dispositivoId, respuestas, completa.resultados)
        return dispositivoId to evaluacionId
    }

    @Test
    fun resultadoQuedaGuardadoJuntoASuEvaluacion(): Unit = runBlocking {
        val (_, evaluacionId) = guardar(respuestasMixtas)

        val detalle = evaluaciones.obtenerDetalle(evaluacionId)!!
        assertEquals(NivelRiesgo.RIESGO_ALTO, detalle.evaluacion.nivelGlobal)
        assertEquals(Categoria.entries.size, detalle.resultados.size)
        assertEquals(CatalogoLocal.preguntas.size, detalle.respuestas.size)
        assertEquals(7, detalle.acciones.size)

        val resultado = mapper.construir(detalle)
        assertEquals(evaluacionId, resultado.evaluacionId)
        assertEquals(
            listOf(NivelRiesgo.RIESGO_MEDIO, NivelRiesgo.RIESGO_BAJO, NivelRiesgo.RIESGO_MEDIO, NivelRiesgo.RIESGO_ALTO),
            resultado.categorias.map { it.nivel }
        )
        // Cada recomendación queda enlazada a una acción real del checklist (TT-13).
        resultado.recomendaciones.forEach { vista ->
            val accion = acciones.obtener(vista.accionId!!)!!
            assertEquals(vista.recomendacionId, accion.recomendacionId)
            assertEquals(EstadoAccion.PENDIENTE, accion.estado)
        }
    }

    @Test
    fun consultarElResultadoVariasVecesNoCreaDuplicados(): Unit = runBlocking {
        val (dispositivoId, evaluacionId) = guardar(respuestasMixtas)
        val primera = mapper.construir(evaluaciones.obtenerDetalle(evaluacionId)!!)

        // Abrir resultados y luego el historial varias veces.
        repeat(5) {
            val otra = mapper.construir(evaluaciones.obtenerDetalle(evaluacionId)!!)
            assertEquals(primera, otra)
            evaluaciones.observarHistorial().first()
        }

        assertEquals(1, evaluaciones.observarHistorial().first().size)
        assertEquals(1, db.evaluacionDao().contarPorDispositivo(dispositivoId))
        assertEquals(7, acciones.observarPorEstado(EstadoAccion.PENDIENTE).first().size)
        val detalle = evaluaciones.obtenerDetalle(evaluacionId)!!
        assertEquals(Categoria.entries.size, detalle.resultados.size)
        assertEquals(CatalogoLocal.preguntas.size, detalle.respuestas.size)
    }

    @Test
    fun resultadoInexistenteDevuelveNull(): Unit = runBlocking {
        assertNull(evaluaciones.obtenerDetalle(999))
    }
}
