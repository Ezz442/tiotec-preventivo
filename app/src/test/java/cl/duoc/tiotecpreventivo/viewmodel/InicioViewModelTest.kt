package cl.duoc.tiotecpreventivo.viewmodel

import cl.duoc.tiotecpreventivo.repository.InicioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
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

/** TT-31: InicioViewModel combina el alias y las acciones pendientes, y muestra un error si falla. */
@OptIn(ExperimentalCoroutinesApi::class)
class InicioViewModelTest {

    @Before
    fun dispatcher() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun restaurar() = Dispatchers.resetMain()

    private class RepositorioFalso(
        val alias: Flow<String?>,
        val pendientes: Flow<Int>
    ) : InicioRepository {
        override fun observarAlias() = alias
        override fun observarAccionesPendientes() = pendientes
    }

    @Test
    fun estadoInicialEstaCargando() {
        val estado = InicioUiState()

        assertTrue(estado.cargando)
        assertNull(estado.alias)
        assertEquals(0, estado.accionesPendientes)
        assertNull(estado.error)
    }

    @Test
    fun combinaAliasYAccionesPendientes() = runTest(UnconfinedTestDispatcher()) {
        val alias = MutableStateFlow<String?>("Ana")
        val pendientes = MutableStateFlow(2)
        val vm = InicioViewModel(RepositorioFalso(alias, pendientes))
        backgroundScope.launch { vm.uiState.collect { } }

        assertEquals("Ana", vm.uiState.value.alias)
        assertEquals(2, vm.uiState.value.accionesPendientes)
        assertFalse(vm.uiState.value.cargando)
        assertNull(vm.uiState.value.error)

        pendientes.value = 5
        alias.value = null

        assertEquals(5, vm.uiState.value.accionesPendientes)
        assertNull(vm.uiState.value.alias)
    }

    @Test
    fun siElRepositorioFallaMuestraUnError() = runTest(UnconfinedTestDispatcher()) {
        val roto = flow<String?> { throw IllegalStateException("sin base de datos") }
        val vm = InicioViewModel(RepositorioFalso(roto, MutableStateFlow(0)))
        backgroundScope.launch { vm.uiState.collect { } }

        assertEquals("No pudimos cargar la información de inicio.", vm.uiState.value.error)
        assertFalse(vm.uiState.value.cargando)
    }
}
