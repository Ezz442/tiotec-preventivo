package cl.duoc.tiotecpreventivo.viewmodel

import cl.duoc.tiotecpreventivo.data.local.entity.ExperienciaDigital
import cl.duoc.tiotecpreventivo.repository.PerfilDaoFalso
import cl.duoc.tiotecpreventivo.repository.PerfilRepository
import cl.duoc.tiotecpreventivo.repository.perfilDePrueba
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

/** TT-21 (TT-09): validación por campo y guardado del perfil ficticio. */
@OptIn(ExperimentalCoroutinesApi::class)
class PerfilViewModelTest {

    @Before
    fun dispatcher() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun restaurar() = Dispatchers.resetMain()

    private fun crear(dao: PerfilDaoFalso = PerfilDaoFalso()) = PerfilViewModel(PerfilRepository(dao) { 1_000L })

    @Test
    fun sinPerfilCargaElFormularioVacio() {
        val estado = crear().uiState.value

        assertFalse(estado.cargando)
        assertTrue(estado.perfilCargado)
        assertEquals("", estado.alias)
        assertNull(estado.experienciaDigital)
    }

    @Test
    fun conPerfilGuardadoCargaSusDatos() {
        val estado = crear(PerfilDaoFalso(perfilDePrueba())).uiState.value

        assertEquals("Tío Prueba", estado.alias)
        assertEquals(ExperienciaDigital.PRINCIPIANTE, estado.experienciaDigital)
    }

    @Test
    fun camposVaciosMuestranAmbosErrores() {
        val dao = PerfilDaoFalso()
        val viewModel = crear(dao)

        viewModel.guardarPerfil()

        assertEquals("Ingresa un alias ficticio.", viewModel.uiState.value.errorAlias)
        assertEquals("Selecciona tu nivel de experiencia digital.", viewModel.uiState.value.errorExperiencia)
        assertNull(dao.perfil.value)
    }

    @Test
    fun aliasDeUnCaracterOMasDe30NoEsValido() {
        val viewModel = crear()
        viewModel.onExperienciaChange(ExperienciaDigital.INTERMEDIO)

        viewModel.onAliasChange(" T ")
        viewModel.guardarPerfil()
        assertEquals("El alias debe tener entre 2 y 30 caracteres.", viewModel.uiState.value.errorAlias)

        viewModel.onAliasChange("x".repeat(31))
        viewModel.guardarPerfil()
        assertEquals("El alias debe tener entre 2 y 30 caracteres.", viewModel.uiState.value.errorAlias)

        viewModel.onAliasChange("x".repeat(30))
        viewModel.guardarPerfil()
        assertNull(viewModel.uiState.value.errorAlias)
    }

    @Test
    fun escribirBorraElErrorDelCampo() {
        val viewModel = crear()
        viewModel.guardarPerfil()

        viewModel.onAliasChange("Tí")

        assertNull(viewModel.uiState.value.errorAlias)
        assertEquals("Selecciona tu nivel de experiencia digital.", viewModel.uiState.value.errorExperiencia)
    }

    @Test
    fun guardarValidoQuitaEspaciosYGuardaUnSoloPerfil() {
        val dao = PerfilDaoFalso()
        val viewModel = crear(dao)
        viewModel.onAliasChange("  Tía Demo  ")
        viewModel.onExperienciaChange(ExperienciaDigital.AVANZADO)

        viewModel.guardarPerfil()
        viewModel.onAliasChange("Tía Demo 2")
        viewModel.guardarPerfil()

        assertEquals("Perfil guardado correctamente.", viewModel.uiState.value.mensaje)
        assertEquals(1L, dao.perfil.value!!.id)
        assertEquals("Tía Demo 2", dao.perfil.value!!.alias)
        assertEquals(ExperienciaDigital.AVANZADO, dao.perfil.value!!.experienciaDigital)
    }

    @Test
    fun errorAlGuardarMuestraMensajeYPermiteReintentar() {
        val dao = PerfilDaoFalso().apply { falla = true }
        val viewModel = crear(dao)
        viewModel.onAliasChange("Tía Demo")
        viewModel.onExperienciaChange(ExperienciaDigital.AVANZADO)

        viewModel.guardarPerfil()
        assertEquals("No pudimos guardar tu perfil. Inténtalo nuevamente.", viewModel.uiState.value.errorGeneral)
        assertFalse(viewModel.uiState.value.guardando)

        dao.falla = false
        viewModel.guardarPerfil()
        assertEquals("Tía Demo", dao.perfil.value!!.alias)
    }
}
