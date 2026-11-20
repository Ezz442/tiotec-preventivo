package cl.duoc.tiotecpreventivo.repository

import cl.duoc.tiotecpreventivo.data.remote.ErrorRemoto
import cl.duoc.tiotecpreventivo.data.remote.PostsApi
import cl.duoc.tiotecpreventivo.data.remote.model.PostDto
import com.google.gson.JsonParseException
import kotlinx.coroutines.test.runTest
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

/** TT-30: PostsRepository con una API falsa (sin red real). */
class PostsRepositoryTest {

    private class ApiFalsa(private val accion: () -> List<PostDto>) : PostsApi {
        var limiteRecibido: Int? = null
        override suspend fun posts(limite: Int?): List<PostDto> {
            limiteRecibido = limite
            return accion()
        }

        override suspend fun post(id: Int): PostDto = error("no se usa")
    }

    private fun http(codigo: Int) =
        HttpException(Response.error<Any>(codigo, "".toResponseBody(null)))

    private suspend fun fallo(api: PostsApi): ErrorRemoto {
        try {
            PostsRepository(api).obtenerConsejos()
        } catch (e: ErrorRemoto) {
            return e
        }
        fail("Debía lanzar ErrorRemoto")
        throw IllegalStateException()
    }

    @Test
    fun convierteLosPostsAConsejosLimpiandoTextos() = runTest {
        val api = ApiFalsa {
            listOf(PostDto(1, 7, "  usa contraseñas largas", "linea uno\nlinea dos"))
        }

        val consejos = PostsRepository(api).obtenerConsejos(limite = 5)

        assertEquals(5, api.limiteRecibido)
        assertEquals(1, consejos.size)
        assertEquals(7, consejos[0].id)
        assertEquals("Usa contraseñas largas", consejos[0].titulo)
        assertEquals("linea uno linea dos", consejos[0].texto)
    }

    @Test
    fun listaVaciaDevuelveListaVacia() = runTest {
        assertTrue(PostsRepository(ApiFalsa { emptyList() }).obtenerConsejos().isEmpty())
    }

    @Test
    fun sinInternetSeTraduceAErrorSinConexion() = runTest {
        val e = fallo(ApiFalsa { throw IOException("sin red") })

        assertTrue(e.sinConexion)
        assertNull(e.codigoHttp)
    }

    @Test
    fun http404Y429YServidorTienenMensajePropio() = runTest {
        val e404 = fallo(ApiFalsa { throw http(404) })
        val e429 = fallo(ApiFalsa { throw http(429) })
        val e500 = fallo(ApiFalsa { throw http(503) })

        assertEquals(404, e404.codigoHttp)
        assertEquals(429, e429.codigoHttp)
        assertEquals(503, e500.codigoHttp)
        assertTrue(e429.message!!.contains("demasiadas"))
        assertTrue(e500.message!!.contains("más tarde"))
        assertFalse(e404.sinConexion)
    }

    @Test
    fun jsonInesperadoSeTraduceAErrorGenerico() = runTest {
        val e = fallo(ApiFalsa { throw JsonParseException("mal") })

        assertNull(e.codigoHttp)
        assertFalse(e.sinConexion)
    }
}
