package cl.duoc.tiotecpreventivo.viewmodel

import cl.duoc.tiotecpreventivo.data.imagenes.AlmacenImagenesLocal
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
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.File

/**
 * TT-21 (TT-19): imagen elegida desde la galería. La copia se hace en Dispatchers.IO,
 * por eso cada prueba espera a que termine el procesamiento.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DispositivosGaleriaViewModelTest {

    @get:Rule
    val carpeta = TemporaryFolder()

    private val dao = DispositivoDaoFalso()
    private lateinit var almacen: AlmacenImagenesLocal
    private lateinit var viewModel: DispositivosViewModel
    private val bytes = byteArrayOf(9, 8, 7, 6)

    @Before
    fun crear() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        almacen = AlmacenImagenesLocal(carpeta.root)
        dao.filas.value = listOf(dispositivoDePrueba(1, "Teléfono de prueba"))
        viewModel = DispositivosViewModel(
            DispositivoRepository(dao) { 1_000L }, PerfilRepository(PerfilDaoFalso(perfilDePrueba())), almacen
        )
    }

    @After
    fun restaurar() = Dispatchers.resetMain()

    private fun esperarProcesamiento() {
        val limite = System.currentTimeMillis() + 5_000
        while (viewModel.uiState.value.procesandoFoto || viewModel.uiState.value.dispositivoGaleriaId != null) {
            check(System.currentTimeMillis() < limite) { "La copia de la imagen no terminó" }
            Thread.sleep(10)
        }
    }

    private fun elegir(contenido: ByteArray?) {
        assertTrue(viewModel.prepararGaleria(1))
        viewModel.imagenSeleccionada { contenido?.let { ByteArrayInputStream(it) } }
        esperarProcesamiento()
    }

    private fun imagenGuardada(): String? = dao.filas.value.single().imagenRuta

    @Test
    fun copiaLaImagenAlAlmacenamientoPrivadoYLaAsocia() {
        elegir(bytes)

        val ruta = imagenGuardada()!!
        assertTrue(ruta.startsWith("imagenes/"))
        assertArrayEquals(bytes, almacen.archivo(ruta)!!.readBytes())
        assertEquals("Imagen de galería guardada.", viewModel.uiState.value.mensaje)
    }

    @Test
    fun reemplazarBorraLaImagenAnterior() {
        elegir(bytes)
        val primera = imagenGuardada()!!

        elegir(byteArrayOf(1, 2))

        assertFalse(almacen.existe(primera))
        assertTrue(almacen.existe(imagenGuardada()!!))
    }

    @Test
    fun cancelarElSelectorNoCambiaNada() {
        assertTrue(viewModel.prepararGaleria(1))

        viewModel.cancelarGaleria()

        assertNull(viewModel.uiState.value.dispositivoGaleriaId)
        assertNull(imagenGuardada())
    }

    @Test
    fun imagenVaciaOInaccesibleMuestraErrorYNoDejaArchivos() {
        elegir(ByteArray(0))
        assertEquals("No pudimos guardar la imagen. Inténtalo nuevamente.", viewModel.uiState.value.errorOperacion)

        elegir(null)
        assertNull(imagenGuardada())
        assertTrue(File(carpeta.root, "imagenes").listFiles().orEmpty().isEmpty())
    }

    @Test
    fun dispositivoArchivadoNoRecibeLaImagen() {
        assertTrue(viewModel.prepararGaleria(1))
        dao.filas.value = listOf(dispositivoDePrueba(1, "Teléfono de prueba", archivado = true))

        viewModel.imagenSeleccionada { ByteArrayInputStream(bytes) }
        esperarProcesamiento()

        assertNull(imagenGuardada())
        assertTrue(File(carpeta.root, "imagenes").listFiles().orEmpty().isEmpty())
    }
}
