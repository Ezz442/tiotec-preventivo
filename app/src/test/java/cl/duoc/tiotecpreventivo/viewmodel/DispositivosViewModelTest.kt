package cl.duoc.tiotecpreventivo.viewmodel

import cl.duoc.tiotecpreventivo.data.imagenes.AlmacenImagenesLocal
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo
import cl.duoc.tiotecpreventivo.domain.ValidadorFormularios
import cl.duoc.tiotecpreventivo.repository.DispositivoDaoFalso
import cl.duoc.tiotecpreventivo.repository.DispositivoRepository
import cl.duoc.tiotecpreventivo.repository.PerfilDaoFalso
import cl.duoc.tiotecpreventivo.repository.PerfilRepository
import cl.duoc.tiotecpreventivo.repository.dispositivoDePrueba
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
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** TT-21 (TT-09): CRUD de dispositivos, validaciones por campo y regla de archivado. */
@OptIn(ExperimentalCoroutinesApi::class)
class DispositivosViewModelTest {

    @get:Rule
    val carpeta = TemporaryFolder()

    private val dao = DispositivoDaoFalso()
    private val perfiles = PerfilDaoFalso(perfilDePrueba())
    private lateinit var viewModel: DispositivosViewModel

    @Before
    fun crear() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        dao.filas.value = listOf(dispositivoDePrueba(1, "Teléfono de prueba"))
        viewModel = DispositivosViewModel(
            DispositivoRepository(dao) { 1_000L },
            PerfilRepository(perfiles),
            AlmacenImagenesLocal(carpeta.root)
        )
    }

    @After
    fun restaurar() = Dispatchers.resetMain()

    private fun llenar(nombre: String, so: SistemaOperativo? = SistemaOperativo.IOS) {
        viewModel.onNombreChange(nombre)
        so?.let(viewModel::onSistemaOperativoChange)
    }

    @Test
    fun cargaLosDispositivosActivosDelPerfil() {
        val estado = viewModel.uiState.value

        assertFalse(estado.cargando)
        assertEquals(1L, estado.perfilId)
        assertEquals(listOf("Teléfono de prueba"), estado.dispositivos.map { it.nombre })
    }

    @Test
    fun sinPerfilNoSeAbreElFormulario() {
        perfiles.perfil.value = null

        viewModel.abrirNuevo()

        assertFalse(viewModel.uiState.value.formularioVisible)
    }

    @Test
    fun formularioVacioMuestraErroresPorCampo() {
        viewModel.abrirNuevo()

        viewModel.guardarDispositivo()

        assertEquals("Ingresa un nombre ficticio.", viewModel.uiState.value.errorNombre)
        assertEquals("Selecciona un sistema operativo.", viewModel.uiState.value.errorSistemaOperativo)
        assertEquals(1, dao.filas.value.size)
    }

    @Test
    fun nombreFueraDeRangoNoEsValido() {
        viewModel.abrirNuevo()

        llenar("T")
        viewModel.guardarDispositivo()
        assertEquals("El nombre debe tener entre 2 y 30 caracteres.", viewModel.uiState.value.errorNombre)

        llenar("x".repeat(31))
        viewModel.guardarDispositivo()
        assertEquals("El nombre debe tener entre 2 y 30 caracteres.", viewModel.uiState.value.errorNombre)
        assertEquals(1, dao.filas.value.size)
    }

    @Test
    fun nombreRepetidoSinImportarMayusculasNoSeGuarda() {
        viewModel.abrirNuevo()
        llenar("  TELÉFONO de prueba ")

        viewModel.guardarDispositivo()

        assertEquals("Ya existe un dispositivo con ese nombre.", viewModel.uiState.value.errorNombre)
        assertTrue(viewModel.uiState.value.formularioVisible)
        assertEquals(1, dao.filas.value.size)
    }

    @Test
    fun crearDispositivoValido() {
        viewModel.abrirNuevo()
        llenar("  Tablet de prueba  ")

        viewModel.guardarDispositivo()

        val estado = viewModel.uiState.value
        assertFalse(estado.formularioVisible)
        assertEquals("Dispositivo registrado correctamente.", estado.mensaje)
        assertEquals(listOf("Tablet de prueba", "Teléfono de prueba"), estado.dispositivos.map { it.nombre })
    }

    @Test
    fun editarConservandoSuPropioNombreEsValido() {
        viewModel.editarDispositivo(1)
        assertEquals("Teléfono de prueba", viewModel.uiState.value.nombre)

        viewModel.onSistemaOperativoChange(SistemaOperativo.WINDOWS)
        viewModel.guardarDispositivo()

        assertEquals("Dispositivo actualizado correctamente.", viewModel.uiState.value.mensaje)
        assertEquals(SistemaOperativo.WINDOWS, dao.filas.value.single().sistemaOperativo)
    }

    @Test
    fun cancelarElFormularioNoGuarda() {
        viewModel.abrirNuevo()
        llenar("Tablet de prueba")

        // TT-26: con cambios sin guardar primero se pide confirmación.
        viewModel.cancelarFormulario()
        assertTrue(viewModel.uiState.value.confirmarDescartar)
        assertTrue(viewModel.uiState.value.formularioVisible)

        viewModel.descartarCambios()

        assertFalse(viewModel.uiState.value.formularioVisible)
        assertFalse(viewModel.uiState.value.confirmarDescartar)
        assertEquals(1, dao.filas.value.size)
    }

    @Test
    fun cancelarSinCambiosCierraSinPreguntar() {
        viewModel.abrirNuevo()
        viewModel.cancelarFormulario()
        assertFalse(viewModel.uiState.value.formularioVisible)
        assertFalse(viewModel.uiState.value.confirmarDescartar)

        // Editar y cancelar sin tocar nada tampoco pregunta.
        viewModel.editarDispositivo(1)
        viewModel.cancelarFormulario()
        assertFalse(viewModel.uiState.value.formularioVisible)
        assertFalse(viewModel.uiState.value.confirmarDescartar)
    }

    @Test
    fun seguirEditandoConservaLoEscrito() {
        viewModel.editarDispositivo(1)
        viewModel.onNombreChange("Teléfono renombrado")

        viewModel.cancelarFormulario()
        assertTrue(viewModel.uiState.value.confirmarDescartar)
        viewModel.seguirEditando()

        val estado = viewModel.uiState.value
        assertFalse(estado.confirmarDescartar)
        assertTrue(estado.formularioVisible)
        assertEquals("Teléfono renombrado", estado.nombre)
        assertEquals("Teléfono de prueba", dao.filas.value.single().nombre)
    }

    @Test
    fun nombreConCorreoOTelefonoNoSeGuarda() {
        viewModel.abrirNuevo()
        llenar("tio@correo.cl")
        viewModel.guardarDispositivo()
        assertEquals(ValidadorFormularios.NOMBRE_PERSONAL, viewModel.uiState.value.errorNombre)

        viewModel.onNombreChange("+56 9 1234 5678")
        viewModel.guardarDispositivo()
        assertEquals(ValidadorFormularios.NOMBRE_PERSONAL, viewModel.uiState.value.errorNombre)
        assertEquals(1, dao.filas.value.size)
    }

    @Test
    fun espaciosDeMasSeNormalizanAlGuardar() {
        viewModel.abrirNuevo()
        llenar("  Tablet   de   prueba  ")

        viewModel.guardarDispositivo()

        assertTrue(dao.filas.value.any { it.nombre == "Tablet de prueba" })
    }

    @Test
    fun eliminarPideConfirmacionYBorraSinEvaluaciones() {
        viewModel.solicitarEliminacion(1)
        assertEquals(1L, viewModel.uiState.value.dispositivoAEliminar!!.id)

        viewModel.cancelarEliminacion()
        assertEquals(1, dao.filas.value.size)

        viewModel.solicitarEliminacion(1)
        viewModel.confirmarEliminacion()
        assertTrue(dao.filas.value.isEmpty())
        assertEquals("Dispositivo eliminado correctamente.", viewModel.uiState.value.mensaje)
    }

    @Test
    fun eliminarConEvaluacionesArchivaYLoQuitaDeLaLista() {
        dao.evaluaciones[1] = 3

        viewModel.solicitarEliminacion(1)
        viewModel.confirmarEliminacion()

        assertTrue(dao.filas.value.single().archivado)
        assertTrue(viewModel.uiState.value.dispositivos.isEmpty())
        assertEquals("Dispositivo archivado. Su historial se conserva.", viewModel.uiState.value.mensaje)
    }

    @Test
    fun errorAlGuardarMantieneElFormulario() {
        dao.falla = true
        viewModel.abrirNuevo()
        llenar("Tablet de prueba")

        viewModel.guardarDispositivo()

        assertEquals("No pudimos guardar el dispositivo. Inténtalo nuevamente.", viewModel.uiState.value.errorOperacion)
        assertTrue(viewModel.uiState.value.formularioVisible)
        assertNull(viewModel.uiState.value.mensaje)
    }
}
