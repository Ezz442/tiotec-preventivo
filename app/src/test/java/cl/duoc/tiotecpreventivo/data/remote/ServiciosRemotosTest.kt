package cl.duoc.tiotecpreventivo.data.remote

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/** TT-31: los clientes Retrofit se crean sin salir a la red y las URLs son válidas para Retrofit. */
class ServiciosRemotosTest {

    private val servicios = ServiciosRemotos("http://localhost:8081/", "http://localhost:8082/")

    @Test
    fun creaLosClientesDeLosDosMicroservicios() {
        assertNotNull(servicios.solicitudes)
        assertNotNull(servicios.dispositivos)
    }

    @Test
    fun cadaClienteSeCreaUnaSolaVez() {
        assertSame(servicios.solicitudes, servicios.solicitudes)
        assertSame(servicios.dispositivos, servicios.dispositivos)
    }

    @Test
    fun crearApiSirveParaUnaApiExterna() {
        val api = servicios.crearApi("https://jsonplaceholder.typicode.com/", PostsApi::class.java)

        assertNotNull(api)
    }

    @Test
    fun lasUrlsBaseTerminanEnBarraComoExigeRetrofit() {
        listOf(ConfigRed.URL_SOLICITUDES, ConfigRed.URL_DISPOSITIVOS, ConfigRed.URL_API_EXTERNA)
            .forEach { assertTrue("$it debe terminar en /", it.endsWith("/")) }
        assertTrue(ConfigRed.TIEMPO_ESPERA_SEGUNDOS > 0)
    }
}
