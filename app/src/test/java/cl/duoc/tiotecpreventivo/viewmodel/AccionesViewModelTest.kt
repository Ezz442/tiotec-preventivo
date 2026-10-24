package cl.duoc.tiotecpreventivo.viewmodel

import cl.duoc.tiotecpreventivo.data.catalogo.Prioridad
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoAccion
import cl.duoc.tiotecpreventivo.repository.AccionDaoFalso
import cl.duoc.tiotecpreventivo.repository.AccionDaoFalso.Companion.accion
import cl.duoc.tiotecpreventivo.repository.AccionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/** TT-21 (TT-13): transiciones pendiente ↔ completada, filtros y contador con StateFlow. */
@OptIn(ExperimentalCoroutinesApi::class)
class AccionesViewModelTest {

    private val dao = AccionDaoFalso()
    private lateinit var viewModel: AccionesViewModel

    @Before
    fun crear() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        dao.filas.value = listOf(
            accion(1, Prioridad.BAJA, revision = 300),
            accion(2, Prioridad.ALTA, revision = 200),
            accion(3, Prioridad.MEDIA, revision = 100),
            accion(4, Prioridad.ALTA, revision = 100)
        )
        viewModel = AccionesViewModel(AccionRepository(dao) { 9_000L })
    }

    @After
    fun restaurar() = Dispatchers.resetMain()

    @Test
    fun pendientesOrdenadasPorPrioridadYFechaConContador() {
        val estado = viewModel.uiState.value

        assertEquals(EstadoAccion.PENDIENTE, estado.filtro)
        assertEquals(listOf(4L, 2L, 3L, 1L), estado.acciones.map { it.id })
        assertEquals(4, estado.pendientes)
    }

    @Test
    fun completarLaQuitaDePendientesYBajaElContador() {
        viewModel.cambiarEstado(2)

        assertEquals(listOf(4L, 3L, 1L), viewModel.uiState.value.acciones.map { it.id })
        assertEquals(3, viewModel.uiState.value.pendientes)
        val completada = dao.filas.value.single { it.id == 2L }
        assertEquals(EstadoAccion.COMPLETADA, completada.estado)
        assertEquals(9_000L, completada.completadaEn)
    }

    @Test
    fun filtroCompletadasYVolverAPendiente() {
        viewModel.cambiarEstado(2)
        viewModel.cambiarFiltro(EstadoAccion.COMPLETADA)
        assertEquals(listOf(2L), viewModel.uiState.value.acciones.map { it.id })

        viewModel.cambiarEstado(2)

        assertEquals(emptyList<Long>(), viewModel.uiState.value.acciones.map { it.id })
        assertEquals(4, viewModel.uiState.value.pendientes)
        assertNull(dao.filas.value.single { it.id == 2L }.completadaEn)
    }

    @Test
    fun accionInexistenteNoHaceNada() {
        viewModel.cambiarEstado(99)

        assertEquals(4, viewModel.uiState.value.pendientes)
        assertNull(viewModel.uiState.value.errorOperacion)
    }

    @Test
    fun errorAlGuardarMantieneLaAccionYMuestraMensaje() {
        dao.falla = true

        viewModel.cambiarEstado(2)

        assertNotNull(viewModel.uiState.value.errorOperacion)
        assertEquals(EstadoAccion.PENDIENTE, dao.filas.value.single { it.id == 2L }.estado)
        assertNull(viewModel.uiState.value.actualizandoId)
    }
}
