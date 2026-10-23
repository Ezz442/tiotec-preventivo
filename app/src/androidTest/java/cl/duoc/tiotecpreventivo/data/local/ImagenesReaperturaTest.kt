package cl.duoc.tiotecpreventivo.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.tiotecpreventivo.data.imagenes.AlmacenImagenesLocal
import cl.duoc.tiotecpreventivo.data.imagenes.copiarImagen
import cl.duoc.tiotecpreventivo.data.local.entity.ExperienciaDigital
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo
import cl.duoc.tiotecpreventivo.repository.DispositivoRepository
import cl.duoc.tiotecpreventivo.repository.PerfilRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.File

/**
 * TT-21 (TT-18/19): la ruta guardada en Room y el archivo de la foto en el almacenamiento
 * privado siguen disponibles después de cerrar y reabrir la base, como al reiniciar la app.
 * Ejecutar con un emulador encendido: clic derecho → Run 'ImagenesReaperturaTest'.
 */
@RunWith(AndroidJUnit4::class)
class ImagenesReaperturaTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val nombreBase = "tiotec_prueba_imagenes.db"
    /** Carpeta aparte para no tocar las fotos reales de la app en el emulador. */
    private val base = File(context.filesDir, "prueba_tt21")
    private val bytes = ByteArray(2048) { (it % 251).toByte() }

    @Before
    fun limpiarAntes() {
        context.deleteDatabase(nombreBase)
        base.deleteRecursively()
    }

    @After
    fun limpiarDespues() {
        context.deleteDatabase(nombreBase)
        base.deleteRecursively()
    }

    @Test
    fun fotoSigueDisponibleAlReabrir(): Unit = runBlocking {
        // Primera apertura: se asocia una imagen al dispositivo.
        val primera = TioTecDatabase.construir(context, nombreBase)
        val perfilId = PerfilRepository(primera.perfilDao()).guardar("Tío Prueba", ExperienciaDigital.INTERMEDIO)
        val dispositivos = DispositivoRepository(primera.dispositivoDao())
        val id = dispositivos.crear(perfilId, "Teléfono de prueba", SistemaOperativo.ANDROID)

        val almacen = AlmacenImagenesLocal(base)
        val ruta = almacen.nuevaRuta(id)
        almacen.copiarImagen(ruta) { ByteArrayInputStream(bytes) }
        dispositivos.actualizarImagen(id, ruta)
        primera.close()

        // Segunda apertura: nueva base y nuevo almacén sobre los mismos archivos.
        val segunda = TioTecDatabase.construir(context, nombreBase)
        try {
            val guardado = DispositivoRepository(segunda.dispositivoDao()).obtener(id)!!
            assertEquals(ruta, guardado.imagenRuta)

            val reabierto = AlmacenImagenesLocal(base)
            assertTrue(reabierto.existe(ruta))
            assertArrayEquals(bytes, reabierto.archivo(ruta)!!.readBytes())
        } finally {
            segunda.close()
        }
    }

    @Test
    fun quitarLaFotoBorraArchivoYRutaAlReabrir(): Unit = runBlocking {
        val primera = TioTecDatabase.construir(context, nombreBase)
        val perfilId = PerfilRepository(primera.perfilDao()).guardar("Tío Prueba", ExperienciaDigital.INTERMEDIO)
        val dispositivos = DispositivoRepository(primera.dispositivoDao())
        val id = dispositivos.crear(perfilId, "Tablet de prueba", SistemaOperativo.IOS)
        val almacen = AlmacenImagenesLocal(base)
        val ruta = almacen.nuevaRuta(id)
        almacen.copiarImagen(ruta) { ByteArrayInputStream(bytes) }
        dispositivos.actualizarImagen(id, ruta)

        // Lo mismo que hace DispositivosViewModel.confirmarQuitarFoto.
        dispositivos.actualizarImagen(id, null)
        almacen.borrar(ruta)
        primera.close()

        val segunda = TioTecDatabase.construir(context, nombreBase)
        try {
            assertEquals(null, DispositivoRepository(segunda.dispositivoDao()).obtener(id)!!.imagenRuta)
            assertFalse(AlmacenImagenesLocal(base).existe(ruta))
        } finally {
            segunda.close()
        }
    }
}
