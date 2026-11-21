package cl.duoc.tiotecpreventivo.repository

import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.data.local.dao.PerfilDao
import cl.duoc.tiotecpreventivo.data.local.dao.SincronizacionDao
import cl.duoc.tiotecpreventivo.data.local.dao.SolicitudDao
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoSolicitud
import cl.duoc.tiotecpreventivo.data.local.entity.ExperienciaDigital
import cl.duoc.tiotecpreventivo.data.local.entity.PerfilEntity
import cl.duoc.tiotecpreventivo.data.remote.ErrorRemoto
import cl.duoc.tiotecpreventivo.data.remote.SolicitudesApi
import cl.duoc.tiotecpreventivo.data.remote.model.CambioEstadoPeticion
import cl.duoc.tiotecpreventivo.data.remote.model.HistorialDto
import cl.duoc.tiotecpreventivo.data.remote.model.SolicitudDto
import cl.duoc.tiotecpreventivo.data.remote.model.SolicitudPeticion
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

// #pruebas del repositorio de solicitudes con el microservicio simulado
/** TT-31: SolicitudRepositorySincronizado con APIs y DAOs simulados con MockK. */
class SolicitudRepositorySincronizadoTest {

    private val dao = mockk<SolicitudDao>(relaxed = true)
    private val api = mockk<SolicitudesApi>()
    private val sincronizacion = mockk<SincronizacionDao>(relaxed = true)
    private val perfilDao = mockk<PerfilDao>()
    private lateinit var repo: SolicitudRepositorySincronizado

    private val perfil = PerfilEntity(1, "Ana", ExperienciaDigital.PRINCIPIANTE, 0, 0)

    @Before
    fun preparar() {
        repo = SolicitudRepositorySincronizado(dao, api, sincronizacion, perfilDao) { 1000L }
        coEvery { perfilDao.obtener() } returns perfil
    }

    private fun dto(id: Long = 1, categoria: String = "RESPALDOS", estado: String = "PENDIENTE") =
        SolicitudDto(
            id, "DEMO-00$id", "Ana", categoria, estado, 10, 20,
            historial = listOf(HistorialDto(1, estado, 10, "Nota"))
        )

    private fun http(codigo: Int) = HttpException(Response.error<Any>(codigo, "".toResponseBody(null)))

    private suspend fun fallo(bloque: suspend () -> Unit): ErrorRemoto {
        try {
            bloque()
        } catch (e: ErrorRemoto) {
            return e
        }
        fail("Debía lanzar ErrorRemoto")
        throw IllegalStateException()
    }

    // ---------- sincronizar ----------

    @Test
    fun sincronizarSinPerfilNoLlamaAlServicio() = runTest {
        coEvery { perfilDao.obtener() } returns null

        repo.sincronizar()

        coVerify(exactly = 0) { api.listar() }
    }

    @Test
    fun sincronizarGuardaLasRemotasValidasEnRoom() = runTest {
        coEvery { api.listar() } returns listOf(dto(1), dto(2, categoria = "RARA"))

        repo.sincronizar()

        coVerify { sincronizacion.reemplazarSolicitudes(match { it.size == 1 && it[0].id == 1L && it[0].perfilId == 1L }) }
    }

    @Test
    fun sincronizarSinConexionLanzaErrorRemotoYNoTocaRoom() = runTest {
        coEvery { api.listar() } throws IOException("sin red")

        val e = fallo { repo.sincronizar() }

        assertTrue(e.sinConexion)
        coVerify(exactly = 0) { sincronizacion.reemplazarSolicitudes(any()) }
    }

    // ---------- sincronizarDetalle ----------

    @Test
    fun sincronizarDetalleSinPerfilNoHaceNada() = runTest {
        coEvery { perfilDao.obtener() } returns null

        repo.sincronizarDetalle(5)

        coVerify(exactly = 0) { api.obtener(any()) }
    }

    @Test
    fun sincronizarDetalleGuardaSolicitudEHistorial() = runTest {
        coEvery { api.obtener(5) } returns dto(5, estado = "EN_REVISION")

        repo.sincronizarDetalle(5)

        coVerify { sincronizacion.guardarSolicitudCompleta(match { it.id == 5L }, match { it.size == 1 }) }
    }

    @Test
    fun sincronizarDetalleSiElServicioNoLaTieneLaBorraDeRoom() = runTest {
        coEvery { api.obtener(5) } throws http(404)

        repo.sincronizarDetalle(5)

        coVerify { sincronizacion.borrarSolicitud(5) }
    }

    @Test
    fun sincronizarDetalleConOtroErrorLoPropagaYNoBorra() = runTest {
        coEvery { api.obtener(5) } throws http(500)

        val e = fallo { repo.sincronizarDetalle(5) }

        assertEquals(500, e.codigoHttp)
        coVerify(exactly = 0) { sincronizacion.borrarSolicitud(any()) }
    }

    // ---------- crear ----------

    @Test
    fun crearSinPerfilPideCrearUno() = runTest {
        coEvery { perfilDao.obtener() } returns null

        val e = fallo { repo.crear(1, Categoria.RESPALDOS) }

        assertTrue(e.message!!.contains("perfil"))
        coVerify(exactly = 0) { api.crear(any()) }
    }

    @Test
    fun crearEnviaAliasYCategoriaYGuardaLaRespuesta() = runTest {
        coEvery { api.crear(SolicitudPeticion("Ana", "RESPALDOS")) } returns dto(9)

        val id = repo.crear(1, Categoria.RESPALDOS)

        assertEquals(9L, id)
        coVerify { sincronizacion.guardarSolicitudCompleta(match { it.id == 9L }, any()) }
    }

    @Test
    fun crearConRespuestaDesconocidaLanzaErrorYNoGuarda() = runTest {
        coEvery { api.crear(any()) } returns dto(9, estado = "RARO")

        val e = fallo { repo.crear(1, Categoria.RESPALDOS) }

        assertTrue(e.message!!.contains("desconocido"))
        coVerify(exactly = 0) { sincronizacion.guardarSolicitudCompleta(any(), any()) }
    }

    @Test
    fun crearSinConexionNoGuardaNadaLocal() = runTest {
        coEvery { api.crear(any()) } throws IOException("sin red")

        val e = fallo { repo.crear(1, Categoria.RESPALDOS) }

        assertTrue(e.sinConexion)
        coVerify(exactly = 0) { sincronizacion.guardarSolicitudCompleta(any(), any()) }
    }

    // ---------- cambiarEstado ----------

    @Test
    fun cambiarEstadoSinPerfilDevuelveFalse() = runTest {
        coEvery { perfilDao.obtener() } returns null

        assertFalse(repo.cambiarEstado(3, EstadoSolicitud.EN_REVISION))
    }

    @Test
    fun cambiarEstadoEnviaElNuevoEstadoYGuarda() = runTest {
        coEvery { api.cambiarEstado(3, any()) } returns dto(3, estado = "EN_REVISION")

        assertTrue(repo.cambiarEstado(3, EstadoSolicitud.EN_REVISION))

        coVerify {
            api.cambiarEstado(3, CambioEstadoPeticion("EN_REVISION", "Estado cambiado a En revisión"))
            sincronizacion.guardarSolicitudCompleta(match { it.estado == EstadoSolicitud.EN_REVISION }, any())
        }
    }

    @Test
    fun cambiarEstadoDeUnaSolicitudAusenteLaBorraYDevuelveFalse() = runTest {
        coEvery { api.cambiarEstado(3, any()) } throws http(404)

        assertFalse(repo.cambiarEstado(3, EstadoSolicitud.CANCELADA))

        coVerify { sincronizacion.borrarSolicitud(3) }
    }

    @Test
    fun cambiarEstadoConTransicionNoPermitidaPropagaElConflicto() = runTest {
        coEvery { api.cambiarEstado(3, any()) } throws http(409)

        val e = fallo { repo.cambiarEstado(3, EstadoSolicitud.RESUELTA) }

        assertTrue(e.conflicto)
        coVerify(exactly = 0) { sincronizacion.borrarSolicitud(any()) }
    }

    // ---------- eliminar ----------

    @Test
    fun eliminarBorraEnElServicioYEnRoom() = runTest {
        coEvery { api.eliminar(4) } returns Unit
        coEvery { sincronizacion.borrarSolicitud(4) } returns 1

        assertTrue(repo.eliminar(4))

        coVerify { api.eliminar(4) }
    }

    @Test
    fun eliminarToleraQueElServicioYaNoLaTenga() = runTest {
        coEvery { api.eliminar(4) } throws http(404)
        coEvery { sincronizacion.borrarSolicitud(4) } returns 1

        assertTrue(repo.eliminar(4))
    }

    @Test
    fun eliminarDevuelveFalseSiRoomNoTeniaLaFila() = runTest {
        coEvery { api.eliminar(4) } returns Unit
        coEvery { sincronizacion.borrarSolicitud(4) } returns 0

        assertFalse(repo.eliminar(4))
    }

    @Test
    fun eliminarConErrorDelServicioNoBorraLaCopiaLocal() = runTest {
        coEvery { api.eliminar(4) } throws http(500)

        fallo { repo.eliminar(4) }

        coVerify(exactly = 0) { sincronizacion.borrarSolicitud(any()) }
    }
}
