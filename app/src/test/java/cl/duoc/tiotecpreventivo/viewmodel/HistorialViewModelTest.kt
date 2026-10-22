package cl.duoc.tiotecpreventivo.viewmodel

import cl.duoc.tiotecpreventivo.data.local.entity.EvaluacionEntity
import cl.duoc.tiotecpreventivo.data.local.entity.NivelRiesgo
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** TT-14: el historial se observa desde Room y se filtra en el ViewModel. */
@OptIn(ExperimentalCoroutinesApi::class)
class HistorialViewModelTest {

    @Before
    fun usarDispatcherDePrueba() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun restaurarDispatcher() = Dispatchers.resetMain()

    private fun evaluacion(id: Long, dispositivoId: Long, nombre: String, fecha: Long) =
        EvaluacionEntity(id, dispositivoId, nombre, SistemaOperativo.IOS, fecha, NivelRiesgo.RIESGO_BAJO)

    @Test
    fun historialVacio() {
        val viewModel = HistorialViewModel { MutableStateFlow(emptyList()) }

        val estado = viewModel.uiState.value
        assertFalse(estado.cargando)
        assertTrue(estado.todas.isEmpty())
        assertTrue(estado.visibles.isEmpty())
    }

    @Test
    fun nuevaEvaluacionApareceSinRecargar() {
        val room = MutableStateFlow(listOf(evaluacion(1, 10, "Tablet de prueba", 100)))
        val viewModel = HistorialViewModel { room }
        assertEquals(1, viewModel.uiState.value.visibles.size)

        room.value = room.value + evaluacion(2, 10, "Tablet de prueba", 200)

        assertEquals(listOf(2L, 1L), viewModel.uiState.value.visibles.map { it.id })
    }

    @Test
    fun filtraPorDispositivoYVuelveATodos() {
        val room = MutableStateFlow(
            listOf(evaluacion(1, 10, "Tablet de prueba", 100), evaluacion(2, 20, "Teléfono de prueba", 200))
        )
        val viewModel = HistorialViewModel { room }

        viewModel.seleccionarDispositivo(10)
        assertEquals(listOf(1L), viewModel.uiState.value.visibles.map { it.id })

        viewModel.seleccionarDispositivo(null)
        assertEquals(listOf(2L, 1L), viewModel.uiState.value.visibles.map { it.id })
    }

    @Test
    fun dispositivoSinEvaluacionesNoSePuedeSeleccionar() {
        val viewModel = HistorialViewModel { MutableStateFlow(listOf(evaluacion(1, 10, "Tablet", 100))) }

        viewModel.seleccionarDispositivo(99)

        assertNull(viewModel.uiState.value.dispositivoSeleccionado)
    }

    @Test
    fun siDesapareceElDispositivoFiltradoVuelveATodos() {
        val room = MutableStateFlow(
            listOf(evaluacion(1, 10, "Tablet", 100), evaluacion(2, 20, "Teléfono", 200))
        )
        val viewModel = HistorialViewModel { room }
        viewModel.seleccionarDispositivo(20)

        room.value = listOf(evaluacion(1, 10, "Tablet", 100))

        assertNull(viewModel.uiState.value.dispositivoSeleccionado)
        assertEquals(listOf(1L), viewModel.uiState.value.visibles.map { it.id })
    }

    @Test
    fun renombrarElDispositivoActualizaElFiltroPeroNoLasEvaluaciones() {
        val room = MutableStateFlow(listOf(evaluacion(1, 10, "iaintegradajorge", 100)))
        val nombres = MutableStateFlow(mapOf(10L to "iaintegradajorge"))
        val viewModel = HistorialViewModel(observarNombresActuales = { nombres }, observarHistorial = { room })
        assertEquals("iaintegradajorge", viewModel.uiState.value.filtros.single().nombre)

        nombres.value = mapOf(10L to "superiajorge")

        assertEquals("superiajorge", viewModel.uiState.value.filtros.single().nombre)
        assertEquals("iaintegradajorge", viewModel.uiState.value.visibles.single().nombreDispositivo)
    }

    @Test
    fun errorAlLeerPermiteReintentar() {
        var falla = true
        val viewModel = HistorialViewModel {
            flow {
                if (falla) error("Base de datos no disponible")
                emit(listOf(evaluacion(1, 10, "Tablet", 100)))
            }
        }
        assertEquals("No pudimos cargar el historial.", viewModel.uiState.value.error)

        falla = false
        viewModel.reintentar()

        assertNull(viewModel.uiState.value.error)
        assertEquals(1, viewModel.uiState.value.visibles.size)
    }
}
