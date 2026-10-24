package cl.duoc.tiotecpreventivo.viewmodel

import cl.duoc.tiotecpreventivo.domain.ValidadorRecordatorio
import cl.duoc.tiotecpreventivo.repository.GestorRecordatorios
import cl.duoc.tiotecpreventivo.repository.ProgramadorFalso
import cl.duoc.tiotecpreventivo.repository.RecordatorioDaoFalso
import cl.duoc.tiotecpreventivo.repository.RecordatorioRepository
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

/** TT-16: crear, editar y cancelar recordatorios desde el ViewModel, con errores por campo. */
@OptIn(ExperimentalCoroutinesApi::class)
class RecordatorioViewModelTest {

    private val ahora = System.currentTimeMillis()
    private val dao = RecordatorioDaoFalso()
    private val programador = ProgramadorFalso()
    private lateinit var viewModel: RecordatorioViewModel

    /** Mañana a las 10:00 (hora local), separada como la entregan los selectores. */
    private val manana = ValidadorRecordatorio.separar(ahora + 24 * 60 * 60_000L)

    @Before
    fun crear() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = RecordatorioViewModel(
            GestorRecordatorios(RecordatorioRepository(dao) { ahora }, programador)
        ) { ahora }
    }

    @After
    fun restaurar() = Dispatchers.resetMain()

    private fun llenarYGuardar(puedeNotificar: Boolean = true) {
        viewModel.onFechaChange(manana.first)
        viewModel.onHoraChange(10, 0)
        viewModel.guardar(puedeNotificar)
    }

    @Test
    fun guardarSinFechaNiHoraMuestraErroresPorCampoYNoGuarda() {
        viewModel.abrirNuevo(accionId = 3, tituloAccion = "Crear una copia de seguridad")

        viewModel.guardar(puedeNotificar = true)

        val formulario = viewModel.uiState.value.formulario!!
        assertEquals("Elige una fecha.", formulario.errorFecha)
        assertEquals("Elige una hora.", formulario.errorHora)
        assertTrue(dao.filas.value.isEmpty())
    }

    @Test
    fun corregirUnCampoBorraSuError() {
        viewModel.abrirNuevo(3, "Crear una copia de seguridad")
        viewModel.guardar(true)

        viewModel.onFechaChange(manana.first)

        assertNull(viewModel.uiState.value.formulario!!.errorFecha)
        assertNotNull(viewModel.uiState.value.formulario!!.errorHora)
    }

    @Test
    fun guardarValidoProgramaYApareceEnLaAccion() {
        viewModel.abrirNuevo(3, "Crear una copia de seguridad")

        llenarYGuardar()

        val estado = viewModel.uiState.value
        assertNull(estado.formulario)
        assertEquals("Recordatorio programado.", estado.mensaje)
        val recordatorio = estado.porAccion.getValue(3)
        assertEquals("Crear una copia de seguridad", recordatorio.titulo)
        assertEquals(mapOf(recordatorio.id to recordatorio.fechaHora), programador.programados)
    }

    @Test
    fun sinPermisoSeGuardaIgualYExplicaComoActivarlo() {
        viewModel.abrirNuevo(3, "Crear una copia de seguridad")

        llenarYGuardar(puedeNotificar = false)

        assertEquals(1, viewModel.uiState.value.porAccion.size)
        assertTrue(viewModel.uiState.value.mensaje!!.contains("Activa las notificaciones"))
    }

    @Test
    fun abrirUnaAccionConRecordatorioLoEditaYReprograma() {
        viewModel.abrirNuevo(3, "Crear una copia de seguridad")
        llenarYGuardar()
        val id = viewModel.uiState.value.porAccion.getValue(3).id

        viewModel.abrirNuevo(3, "Crear una copia de seguridad")
        val formulario = viewModel.uiState.value.formulario!!
        assertEquals(id, formulario.recordatorioId)
        assertEquals(10, formulario.hora)

        viewModel.onHoraChange(18, 30)
        viewModel.guardar(true)

        assertEquals("Recordatorio reprogramado.", viewModel.uiState.value.mensaje)
        assertEquals(1, dao.filas.value.size)
        val (_, hora, minuto) = ValidadorRecordatorio.separar(programador.programados.getValue(id))
        assertEquals(18, hora)
        assertEquals(30, minuto)
    }

    @Test
    fun cancelarQuitaElRecordatorioDeLaAccion() {
        viewModel.abrirNuevo(3, "Crear una copia de seguridad")
        llenarYGuardar()
        val id = viewModel.uiState.value.porAccion.getValue(3).id

        viewModel.cancelarRecordatorio(id)

        assertTrue(viewModel.uiState.value.porAccion.isEmpty())
        assertTrue(programador.programados.isEmpty())
        assertEquals("Recordatorio cancelado.", viewModel.uiState.value.mensaje)
    }

    @Test
    fun cancelarPideConfirmacionYVolverNoBorraNada() {
        viewModel.abrirNuevo(3, "Crear una copia de seguridad")
        llenarYGuardar()
        val id = viewModel.uiState.value.porAccion.getValue(3).id
        viewModel.abrirNuevo(3, "Crear una copia de seguridad")

        // TT-26: primero el AlertDialog de confirmación.
        viewModel.pedirCancelacion()
        assertTrue(viewModel.uiState.value.formulario!!.confirmandoCancelacion)

        viewModel.volverDeCancelacion()
        assertFalse(viewModel.uiState.value.formulario!!.confirmandoCancelacion)
        assertEquals(id, viewModel.uiState.value.porAccion.getValue(3).id)
        assertEquals(1, programador.programados.size)

        viewModel.pedirCancelacion()
        viewModel.cancelarRecordatorio(id)
        assertNull(viewModel.uiState.value.formulario)
        assertTrue(viewModel.uiState.value.porAccion.isEmpty())
    }

    @Test
    fun unRecordatorioNuevoNoPideConfirmacionDeCancelar() {
        viewModel.abrirNuevo(3, "Crear una copia de seguridad")

        viewModel.pedirCancelacion()

        assertFalse(viewModel.uiState.value.formulario!!.confirmandoCancelacion)
    }

    @Test
    fun cerrarElFormularioNoGuardaNada() {
        viewModel.abrirNuevo(3, "Crear una copia de seguridad")
        viewModel.onFechaChange(manana.first)

        viewModel.cerrarFormulario()

        assertNull(viewModel.uiState.value.formulario)
        assertTrue(dao.filas.value.isEmpty())
        assertFalse(viewModel.uiState.value.guardando)
    }
}
