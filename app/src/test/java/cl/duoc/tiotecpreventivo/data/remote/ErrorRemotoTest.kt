package cl.duoc.tiotecpreventivo.data.remote

import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

// #pruebas de errores de red (sin conexion, 404, 409)
/** TT-31: llamarServicio traduce los fallos de red y HTTP a ErrorRemoto con mensajes para el usuario. */
class ErrorRemotoTest {

    private fun http(codigo: Int, cuerpo: String = "") = HttpException(
        Response.error<Any>(codigo, cuerpo.toResponseBody("application/json".toMediaType()))
    )

    private suspend fun fallo(llamada: suspend () -> Unit): ErrorRemoto {
        try {
            llamarServicio { llamada() }
        } catch (e: ErrorRemoto) {
            return e
        }
        fail("Debía lanzar ErrorRemoto")
        throw IllegalStateException()
    }

    @Test
    fun siTodoSaleBienDevuelveElResultado() = runTest {
        assertEquals(42, llamarServicio { 42 })
    }

    @Test
    fun sinRedSeTraduceAMensajeDeConexion() = runTest {
        val e = fallo { throw IOException("sin red") }

        assertTrue(e.sinConexion)
        assertNull(e.codigoHttp)
        assertEquals(MENSAJE_SIN_CONEXION, e.message)
    }

    @Test
    fun jsonInesperadoSeTraduceAMensajeGenerico() = runTest {
        val e = fallo { throw JsonParseException("mal") }

        assertFalse(e.sinConexion)
        assertNull(e.codigoHttp)
        assertTrue(e.message!!.contains("inesperado"))
    }

    @Test
    fun laCancelacionDeCorrutinasSeDejaPasar() = runTest {
        try {
            llamarServicio { throw CancellationException("cancelada") }
            fail("Debía propagar la cancelación")
        } catch (e: CancellationException) {
            assertEquals("cancelada", e.message)
        }
    }

    @Test
    fun http404SinCuerpoUsaMensajePorCodigo() = runTest {
        val e = fallo { throw http(404) }

        assertEquals(404, e.codigoHttp)
        assertTrue(e.noExiste)
        assertFalse(e.conflicto)
        assertEquals("No existe en el servicio.", e.message)
    }

    @Test
    fun http409EsConflicto() = runTest {
        val e = fallo { throw http(409) }

        assertTrue(e.conflicto)
        assertFalse(e.noExiste)
        assertTrue(e.message!!.contains("estado actual"))
    }

    @Test
    fun http5xxPideIntentarMasTarde() = runTest {
        val e = fallo { throw http(503) }

        assertEquals(503, e.codigoHttp)
        assertTrue(e.message!!.contains("más tarde"))
    }

    @Test
    fun otroCodigoIncluyeElNumero() = runTest {
        val e = fallo { throw http(418) }

        assertEquals("El servicio rechazó la solicitud (418).", e.message)
    }

    @Test
    fun errorDeValidacionMuestraLosCampos() = runTest {
        val cuerpo = """{"status":400,"mensaje":"Datos inválidos","campos":{"alias":"Debe tener entre 3 y 40 caracteres"}}"""

        val e = fallo { throw http(400, cuerpo) }

        assertEquals(400, e.codigoHttp)
        assertEquals("Debe tener entre 3 y 40 caracteres", e.message)
    }

    @Test
    fun errorConMensajeUsaElMensajeDelServicio() = runTest {
        val e = fallo { throw http(409, """{"status":409,"mensaje":"Transición no permitida"}""") }

        assertEquals("Transición no permitida", e.message)
    }

    @Test
    fun cuerpoQueNoEsJsonUsaMensajePorCodigo() = runTest {
        val e = fallo { throw http(404, "esto no es json") }

        assertEquals("No existe en el servicio.", e.message)
    }
}
