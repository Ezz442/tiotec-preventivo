package cl.duoc.tiotecpreventivo.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import cl.duoc.tiotecpreventivo.data.preferences.Tema
import cl.duoc.tiotecpreventivo.repository.PreferenciasRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * DataStore en memoria que puede fallar a propósito al guardar, como si el
 * almacenamiento del teléfono estuviera lleno o no disponible.
 */
private class DataStoreFalso : DataStore<Preferences> {
    private val estado = MutableStateFlow(emptyPreferences())
    var fallarAlGuardar = false

    override val data: Flow<Preferences> = estado

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        if (fallarAlGuardar) throw IOException("Almacenamiento no disponible (simulado)")
        return transform(estado.value).also { estado.value = it }
    }
}

/**
 * TT-08: si guardar una preferencia falla, se muestra un mensaje y se mantiene
 * la configuración anterior sin cerrar la app.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PreferenciasViewModelTest {

    private val dataStore = DataStoreFalso()
    private lateinit var viewModel: PreferenciasViewModel

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = PreferenciasViewModel(PreferenciasRepository(dataStore))
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    /** `stateIn(WhileSubscribed)` solo trabaja si alguien observa, como lo hace la pantalla. */
    private fun kotlinx.coroutines.test.TestScope.observar() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
    }

    @Test
    fun alGuardarBien_seActualizaSinMensaje() = runTest {
        observar()

        viewModel.onTemaChange(Tema.OSCURO)
        viewModel.onAvisosChange(false)
        viewModel.onBienvenidaVista()

        val estado = viewModel.uiState.value
        assertFalse(estado.cargando)
        assertEquals(Tema.OSCURO, estado.tema)
        assertFalse(estado.avisosActivos)
        assertFalse(estado.primeraEjecucion)
        assertNull(estado.mensaje)
    }

    @Test
    fun siFallaGuardarElTema_mantieneElAnteriorYMuestraMensaje() = runTest {
        observar()
        viewModel.onTemaChange(Tema.OSCURO)

        dataStore.fallarAlGuardar = true
        viewModel.onTemaChange(Tema.CLARO)

        val estado = viewModel.uiState.value
        assertEquals("Se mantiene el tema anterior", Tema.OSCURO, estado.tema)
        assertEquals(PreferenciasViewModel.ERROR_TEMA, estado.mensaje)
    }

    @Test
    fun siFallaGuardarLosAvisos_mantieneElAnteriorYMuestraMensaje() = runTest {
        observar()
        dataStore.fallarAlGuardar = true

        viewModel.onAvisosChange(false)

        val estado = viewModel.uiState.value
        assertTrue("Se mantiene el valor anterior", estado.avisosActivos)
        assertEquals(PreferenciasViewModel.ERROR_AVISOS, estado.mensaje)
    }

    @Test
    fun siFallaGuardarLaPrimeraEjecucion_mantieneElAnteriorYMuestraMensaje() = runTest {
        observar()
        dataStore.fallarAlGuardar = true

        viewModel.onBienvenidaVista()

        val estado = viewModel.uiState.value
        assertTrue("Se mantiene el valor anterior", estado.primeraEjecucion)
        assertEquals(PreferenciasViewModel.ERROR_BIENVENIDA, estado.mensaje)
    }

    @Test
    fun trasMostrarElMensaje_seLimpiaYLaAppSigueFuncionando() = runTest {
        observar()
        dataStore.fallarAlGuardar = true
        viewModel.onTemaChange(Tema.OSCURO)

        viewModel.onMensajeMostrado()
        assertNull(viewModel.uiState.value.mensaje)

        // El almacenamiento vuelve a estar disponible: el siguiente cambio funciona.
        dataStore.fallarAlGuardar = false
        viewModel.onTemaChange(Tema.OSCURO)
        assertEquals(Tema.OSCURO, viewModel.uiState.value.tema)
        assertNull(viewModel.uiState.value.mensaje)
    }
}
