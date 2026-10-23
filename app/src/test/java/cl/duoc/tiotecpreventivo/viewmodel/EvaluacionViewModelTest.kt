package cl.duoc.tiotecpreventivo.viewmodel

import cl.duoc.tiotecpreventivo.data.catalogo.CatalogoLocal
import cl.duoc.tiotecpreventivo.data.catalogo.CatalogoRepositoryLocal
import cl.duoc.tiotecpreventivo.data.catalogo.ValorRespuesta
import cl.duoc.tiotecpreventivo.data.local.entity.NivelRiesgo
import cl.duoc.tiotecpreventivo.repository.DispositivoDaoFalso
import cl.duoc.tiotecpreventivo.repository.DispositivoRepository
import cl.duoc.tiotecpreventivo.repository.EvaluacionDaoFalso
import cl.duoc.tiotecpreventivo.repository.EvaluacionRepository
import cl.duoc.tiotecpreventivo.repository.dispositivoDePrueba
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** TT-21 (TT-11): evaluación guiada, validación por pregunta y guardado sin duplicados. */
@OptIn(ExperimentalCoroutinesApi::class)
class EvaluacionViewModelTest {

    private val catalogo = CatalogoRepositoryLocal()
    private val dispositivos = DispositivoDaoFalso()
    private val evaluaciones = EvaluacionDaoFalso()

    @Before
    fun crear() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        dispositivos.filas.value = listOf(
            dispositivoDePrueba(1, "Teléfono de prueba"),
            dispositivoDePrueba(2, "Computador antiguo", archivado = true)
        )
    }

    @After
    fun restaurar() = Dispatchers.resetMain()

    private fun viewModel(dispositivoId: Long = 1) = EvaluacionViewModel(
        dispositivoId = dispositivoId,
        catalogoRepository = catalogo,
        dispositivoRepository = DispositivoRepository(dispositivos),
        evaluacionRepository = EvaluacionRepository(evaluaciones, dispositivos, catalogo) { 5_000L }
    )

    /** Responde todas las preguntas avanzando con "Siguiente". */
    private fun EvaluacionViewModel.responderTodo(valor: ValorRespuesta) {
        repeat(catalogo.preguntas().size) {
            onRespuestaChange(valor)
            siguiente()
        }
    }

    @Test
    fun cargaLasDocePreguntasDelDispositivo() {
        val estado = viewModel().uiState.value

        assertFalse(estado.cargando)
        assertEquals("Teléfono de prueba", estado.dispositivo!!.nombre)
        assertEquals(CatalogoLocal.preguntas.size, estado.preguntas.size)
        assertEquals(0, estado.indiceActual)
    }

    @Test
    fun dispositivoArchivadoOInexistenteNoSePuedeEvaluar() {
        assertEquals("El dispositivo no está disponible para evaluar.", viewModel(2).uiState.value.errorCarga)
        assertEquals("El dispositivo no está disponible para evaluar.", viewModel(99).uiState.value.errorCarga)
    }

    @Test
    fun siguienteSinResponderMuestraErrorYNoAvanza() {
        val vm = viewModel()

        vm.siguiente()

        assertEquals("Selecciona una respuesta para continuar.", vm.uiState.value.errorPregunta)
        assertEquals(0, vm.uiState.value.indiceActual)
    }

    @Test
    fun responderBorraElErrorYLasRespuestasSeConservanAlRetroceder() {
        val vm = viewModel()
        vm.siguiente()
        vm.onRespuestaChange(ValorRespuesta.NO)
        assertNull(vm.uiState.value.errorPregunta)

        vm.siguiente()
        vm.onRespuestaChange(ValorRespuesta.NO_SE)
        vm.anterior()

        val primera = vm.uiState.value.preguntas[0].id
        assertEquals(0, vm.uiState.value.indiceActual)
        assertEquals(ValorRespuesta.NO, vm.uiState.value.respuestas[primera])
        assertEquals(2, vm.uiState.value.respuestas.size)
    }

    @Test
    fun revisarConPreguntasPendientesLlevaALaPrimeraSinResponder() {
        val vm = viewModel()
        vm.onRespuestaChange(ValorRespuesta.SI)

        assertFalse(vm.prepararRevision())

        assertEquals(1, vm.uiState.value.indiceActual)
        assertEquals("Responde esta pregunta antes de revisar.", vm.uiState.value.errorPregunta)
    }

    @Test
    fun finalizarGuardaUnaEvaluacionCompletaConSusResultados() {
        val vm = viewModel()
        vm.responderTodo(ValorRespuesta.NO)

        assertTrue(vm.prepararRevision())
        vm.finalizarEvaluacion()

        val id = vm.uiState.value.evaluacionGuardadaId
        assertNotNull(id)
        val guardada = evaluaciones.evaluaciones.value.single()
        assertEquals(NivelRiesgo.RIESGO_ALTO, guardada.nivelGlobal)
        assertEquals("Teléfono de prueba", guardada.nombreDispositivo)
        assertEquals(CatalogoLocal.preguntas.size, evaluaciones.respuestas.size)
        assertEquals(4, evaluaciones.resultados.size)
        assertEquals(CatalogoLocal.recomendaciones.size, evaluaciones.acciones.size)
    }

    @Test
    fun finalizarDosVecesNoDuplica() {
        val vm = viewModel()
        vm.responderTodo(ValorRespuesta.SI)

        vm.finalizarEvaluacion()
        vm.finalizarEvaluacion()

        assertEquals(1, evaluaciones.evaluaciones.value.size)
        // Después de guardar ya no se puede cambiar ninguna respuesta.
        vm.irAPregunta(0)
        vm.onRespuestaChange(ValorRespuesta.NO)
        assertEquals(ValorRespuesta.SI, vm.uiState.value.respuestas.values.first())
    }

    @Test
    fun errorAlGuardarPermiteReintentarConLasMismasRespuestas() {
        val vm = viewModel()
        vm.responderTodo(ValorRespuesta.SI)
        evaluaciones.falla = true

        vm.finalizarEvaluacion()
        assertEquals("No pudimos guardar la evaluación. Puedes reintentar.", vm.uiState.value.errorGuardado)
        assertTrue(evaluaciones.evaluaciones.value.isEmpty())

        evaluaciones.falla = false
        vm.finalizarEvaluacion()
        assertEquals(1, evaluaciones.evaluaciones.value.size)
    }

    @Test
    fun salirConRespuestasPideConfirmacionYSinRespuestasNo() {
        val vacia = viewModel()
        assertTrue(vacia.solicitarSalida())

        val conRespuestas = viewModel()
        conRespuestas.onRespuestaChange(ValorRespuesta.SI)
        assertFalse(conRespuestas.solicitarSalida())
        assertTrue(conRespuestas.uiState.value.mostrarConfirmarSalida)

        conRespuestas.cancelarSalida()
        assertFalse(conRespuestas.uiState.value.mostrarConfirmarSalida)
        assertEquals(1, conRespuestas.uiState.value.respuestas.size)
    }
}
