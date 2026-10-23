package cl.duoc.tiotecpreventivo.viewmodel

import cl.duoc.tiotecpreventivo.data.imagenes.AlmacenImagenesLocal
import cl.duoc.tiotecpreventivo.data.local.dao.DispositivoDao
import cl.duoc.tiotecpreventivo.data.local.dao.PerfilDao
import cl.duoc.tiotecpreventivo.data.local.entity.DispositivoEntity
import cl.duoc.tiotecpreventivo.data.local.entity.ExperienciaDigital
import cl.duoc.tiotecpreventivo.data.local.entity.PerfilEntity
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo
import cl.duoc.tiotecpreventivo.repository.DispositivoRepository
import cl.duoc.tiotecpreventivo.repository.PerfilRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
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
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** TT-18: tomar, reemplazar, cancelar y quitar la foto de un dispositivo desde el ViewModel. */
@OptIn(ExperimentalCoroutinesApi::class)
class DispositivosFotoViewModelTest {

    @get:Rule
    val carpeta = TemporaryFolder()

    private val dao = DispositivoDaoFalso()
    private lateinit var almacen: AlmacenImagenesLocal
    private lateinit var viewModel: DispositivosViewModel

    @Before
    fun crear() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        almacen = AlmacenImagenesLocal(carpeta.root)
        dao.filas.value = listOf(
            DispositivoEntity(id = 1, perfilId = 1, nombre = "Teléfono de prueba",
                sistemaOperativo = SistemaOperativo.ANDROID, creadoEn = 0, actualizadoEn = 0)
        )
        viewModel = DispositivosViewModel(
            dispositivoRepository = DispositivoRepository(dao) { 1_000L },
            perfilRepository = PerfilRepository(PerfilDaoFalso()),
            almacenImagenes = almacen
        )
    }

    @After
    fun restaurar() = Dispatchers.resetMain()

    /** Simula que la app de cámara escribió la foto en la ruta entregada. */
    private fun camaraGuarda(ruta: String) = almacen.archivo(ruta)!!.writeBytes(byteArrayOf(1, 2, 3))

    private fun imagenGuardada(): String? = dao.filas.value.single().imagenRuta

    @Test
    fun tomarFotoLaAsociaAlDispositivo() {
        val ruta = viewModel.prepararFoto(1)!!
        assertEquals(1L, viewModel.uiState.value.capturaFoto!!.dispositivoId)

        camaraGuarda(ruta)
        viewModel.fotoTomada(exito = true)

        assertEquals(ruta, imagenGuardada())
        assertNull(viewModel.uiState.value.capturaFoto)
        assertEquals("Foto guardada.", viewModel.uiState.value.mensaje)
    }

    @Test
    fun reemplazarBorraLaFotoAnterior() {
        val primera = viewModel.prepararFoto(1)!!
        camaraGuarda(primera)
        viewModel.fotoTomada(true)

        val segunda = viewModel.prepararFoto(1)!!
        camaraGuarda(segunda)
        viewModel.fotoTomada(true)

        assertEquals(segunda, imagenGuardada())
        assertFalse(almacen.existe(primera))
        assertTrue(almacen.existe(segunda))
    }

    @Test
    fun cancelarLaCamaraConservaLaFotoAnteriorYBorraLaTemporal() {
        val primera = viewModel.prepararFoto(1)!!
        camaraGuarda(primera)
        viewModel.fotoTomada(true)

        val temporal = viewModel.prepararFoto(1)!!
        almacen.archivo(temporal)!!.createNewFile()
        viewModel.fotoTomada(exito = false)

        assertEquals(primera, imagenGuardada())
        assertTrue(almacen.existe(primera))
        assertFalse(almacen.archivo(temporal)!!.exists())
        assertTrue(viewModel.uiState.value.mensaje!!.contains("No se tomó la foto"))
    }

    @Test
    fun exitoSinArchivoSeTrataComoCancelacion() {
        viewModel.prepararFoto(1)!!

        viewModel.fotoTomada(exito = true)

        assertNull(imagenGuardada())
        assertNull(viewModel.uiState.value.capturaFoto)
    }

    @Test
    fun sinCamaraMuestraErrorYNoBloquea() {
        viewModel.prepararFoto(1)

        viewModel.camaraNoDisponible()

        assertEquals("Este dispositivo no tiene una cámara disponible.", viewModel.uiState.value.errorOperacion)
        assertNull(viewModel.uiState.value.capturaFoto)
        // Se puede seguir usando la pantalla: por ejemplo, abrir el formulario.
        viewModel.abrirNuevo()
        assertTrue(viewModel.uiState.value.formularioVisible)
    }

    @Test
    fun noSePreparaOtraFotoMientrasHayUnaEnCurso() {
        assertNotNull(viewModel.prepararFoto(1))
        assertNull(viewModel.prepararFoto(1))
    }

    @Test
    fun quitarFotoConConfirmacionBorraArchivoYAsociacion() {
        val ruta = viewModel.prepararFoto(1)!!
        camaraGuarda(ruta)
        viewModel.fotoTomada(true)

        viewModel.solicitarQuitarFoto(1)
        assertNotNull(viewModel.uiState.value.dispositivoQuitarFoto)
        viewModel.confirmarQuitarFoto()

        assertNull(imagenGuardada())
        assertFalse(almacen.existe(ruta))
        assertNull(viewModel.uiState.value.dispositivoQuitarFoto)
    }

    @Test
    fun cancelarQuitarFotoNoCambiaNada() {
        val ruta = viewModel.prepararFoto(1)!!
        camaraGuarda(ruta)
        viewModel.fotoTomada(true)

        viewModel.solicitarQuitarFoto(1)
        viewModel.cancelarQuitarFoto()

        assertEquals(ruta, imagenGuardada())
        assertTrue(almacen.existe(ruta))
    }

    @Test
    fun eliminarDispositivoSinEvaluacionesBorraSuFoto() {
        val ruta = viewModel.prepararFoto(1)!!
        camaraGuarda(ruta)
        viewModel.fotoTomada(true)

        viewModel.solicitarEliminacion(1)
        viewModel.confirmarEliminacion()

        assertTrue(dao.filas.value.isEmpty())
        assertFalse(almacen.existe(ruta))
    }

    @Test
    fun archivarDispositivoConEvaluacionesConservaSuFoto() {
        val ruta = viewModel.prepararFoto(1)!!
        camaraGuarda(ruta)
        viewModel.fotoTomada(true)
        dao.evaluaciones = 2

        viewModel.solicitarEliminacion(1)
        viewModel.confirmarEliminacion()

        assertTrue(dao.filas.value.single().archivado)
        assertTrue(almacen.existe(ruta))
    }
}

private class PerfilDaoFalso : PerfilDao {
    private val perfil = MutableStateFlow<PerfilEntity?>(
        PerfilEntity(id = 1, alias = "Tío Prueba", experienciaDigital = ExperienciaDigital.PRINCIPIANTE,
            creadoEn = 0, actualizadoEn = 0)
    )
    override fun observar(): Flow<PerfilEntity?> = perfil
    override suspend fun obtener(): PerfilEntity? = perfil.value
    override suspend fun insertar(perfil: PerfilEntity): Long = 1
    override suspend fun actualizar(perfil: PerfilEntity) {}
}

private class DispositivoDaoFalso : DispositivoDao() {
    val filas = MutableStateFlow<List<DispositivoEntity>>(emptyList())
    var evaluaciones = 0

    override fun observarActivos(): Flow<List<DispositivoEntity>> = filas.map { l -> l.filter { !it.archivado } }
    override fun observar(id: Long): Flow<DispositivoEntity?> = filas.map { l -> l.find { it.id == id } }
    override fun observarTodos(): Flow<List<DispositivoEntity>> = filas
    override suspend fun obtener(id: Long): DispositivoEntity? = filas.value.find { it.id == id }
    override suspend fun existeNombreActivo(nombre: String, excluirId: Long): Boolean = false
    override suspend fun insertar(dispositivo: DispositivoEntity): Long = error("No se usa")
    override suspend fun actualizar(dispositivo: DispositivoEntity) {
        filas.value = filas.value.map { if (it.id == dispositivo.id) dispositivo else it }
    }
    override suspend fun actualizarImagen(id: Long, ruta: String?, fecha: Long) {
        filas.value = filas.value.map { if (it.id == id) it.copy(imagenRuta = ruta, actualizadoEn = fecha) else it }
    }
    override suspend fun contarEvaluaciones(id: Long): Int = evaluaciones
    override suspend fun borrar(id: Long): Int {
        filas.value = filas.value.filterNot { it.id == id }
        return 1
    }
    override suspend fun marcarArchivado(id: Long, fecha: Long) {
        filas.value = filas.value.map { if (it.id == id) it.copy(archivado = true) else it }
    }
    override suspend fun cancelarRecordatoriosDe(id: Long): Int = 0
}
