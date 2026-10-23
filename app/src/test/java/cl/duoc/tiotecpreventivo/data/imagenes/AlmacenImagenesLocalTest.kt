package cl.duoc.tiotecpreventivo.data.imagenes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/** TT-18: archivos de fotos en el almacenamiento privado, con una carpeta temporal en la JVM. */
class AlmacenImagenesLocalTest {

    @get:Rule
    val carpeta = TemporaryFolder()

    private val almacen by lazy { AlmacenImagenesLocal(carpeta.root) }

    private fun escribir(ruta: String) = almacen.archivo(ruta)!!.writeBytes(byteArrayOf(1, 2, 3))

    @Test
    fun nuevaRutaQuedaDentroDeImagenesConElIdDelDispositivo() {
        val ruta = almacen.nuevaRuta(dispositivoId = 3)

        assertTrue(ruta, ruta.matches(Regex("imagenes/disp_3_\\d+\\.jpg")))
        assertTrue(File(carpeta.root, "imagenes").isDirectory)
    }

    @Test
    fun cadaFotoTieneUnaRutaDistinta() {
        val primera = almacen.nuevaRuta(3)
        escribir(primera)

        assertNotEquals(primera, almacen.nuevaRuta(3))
    }

    @Test
    fun existeSoloSiElArchivoTieneContenido() {
        val ruta = almacen.nuevaRuta(3)
        assertFalse(almacen.existe(ruta))

        almacen.archivo(ruta)!!.createNewFile()
        assertFalse("Un archivo vacío es una foto cancelada", almacen.existe(ruta))

        escribir(ruta)
        assertTrue(almacen.existe(ruta))
    }

    @Test
    fun borrarQuitaElArchivoYNoFallaSiNoExiste() {
        val ruta = almacen.nuevaRuta(3)
        escribir(ruta)

        almacen.borrar(ruta)
        assertFalse(almacen.existe(ruta))

        almacen.borrar(ruta)
        almacen.borrar(null)
    }

    @Test
    fun rechazaRutasFueraDeLaCarpetaDeImagenes() {
        val ajeno = File(carpeta.root, "preferencias.txt").apply { writeText("no borrar") }

        assertNull(almacen.archivo("imagenes/../preferencias.txt"))
        assertNull(almacen.archivo("otra/disp_1.jpg"))
        almacen.borrar("imagenes/../preferencias.txt")

        assertTrue(ajeno.exists())
        assertEquals("no borrar", ajeno.readText())
    }

    @Test
    fun laRutaGuardadaSigueFuncionandoAlReabrir() {
        val ruta = almacen.nuevaRuta(3)
        escribir(ruta)

        // Otra instancia sobre la misma carpeta = la app se volvió a abrir.
        val reabierto = AlmacenImagenesLocal(carpeta.root)
        assertTrue(reabierto.existe(ruta))
        assertNotNull(reabierto.archivo(ruta))
    }
}
