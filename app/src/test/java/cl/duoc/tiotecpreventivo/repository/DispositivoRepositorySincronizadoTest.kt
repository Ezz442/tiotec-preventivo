package cl.duoc.tiotecpreventivo.repository

import cl.duoc.tiotecpreventivo.data.local.dao.DispositivoDao
import cl.duoc.tiotecpreventivo.data.local.dao.PerfilDao
import cl.duoc.tiotecpreventivo.data.local.dao.SincronizacionDao
import cl.duoc.tiotecpreventivo.data.local.entity.DispositivoEntity
import cl.duoc.tiotecpreventivo.data.local.entity.ExperienciaDigital
import cl.duoc.tiotecpreventivo.data.local.entity.PerfilEntity
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo
import cl.duoc.tiotecpreventivo.data.remote.DispositivosApi
import cl.duoc.tiotecpreventivo.data.remote.ErrorRemoto
import cl.duoc.tiotecpreventivo.data.remote.model.ArchivadoPeticion
import cl.duoc.tiotecpreventivo.data.remote.model.DispositivoDto
import cl.duoc.tiotecpreventivo.data.remote.model.DispositivoPeticion
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

/** TT-31: DispositivoRepositorySincronizado con APIs y DAOs simulados con MockK. */
class DispositivoRepositorySincronizadoTest {

    private val dao = mockk<DispositivoDao>(relaxed = true)
    private val api = mockk<DispositivosApi>()
    private val sincronizacion = mockk<SincronizacionDao>(relaxed = true)
    private val perfilDao = mockk<PerfilDao>()
    private lateinit var repo: DispositivoRepositorySincronizado

    private val perfil = PerfilEntity(1, "Ana", ExperienciaDigital.PRINCIPIANTE, 0, 0)

    private val local = DispositivoEntity(
        id = 1, perfilId = 1, nombre = "Table", sistemaOperativo = SistemaOperativo.ANDROID,
        imagenRuta = "fotos/a.jpg", creadoEn = 1, actualizadoEn = 1
    )

    @Before
    fun preparar() {
        repo = DispositivoRepositorySincronizado(dao, api, sincronizacion, perfilDao) { 99L }
        coEvery { perfilDao.obtener() } returns perfil
    }

    private fun dto(id: Long = 1, nombre: String = "Table", sistema: String = "ANDROID") =
        DispositivoDto(id, nombre, sistema, archivado = false, creadoEn = 1, actualizadoEn = 50)

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
    fun sincronizarGuardaLosDispositivosValidos() = runTest {
        coEvery { api.listar() } returns listOf(dto(1), dto(2, sistema = "LINUX"))

        repo.sincronizar()

        coVerify { sincronizacion.guardarDispositivosRemotos(1L, match { it.size == 1 && it[0].id == 1L }) }
    }

    @Test
    fun sincronizarSinConexionPropagaElError() = runTest {
        coEvery { api.listar() } throws IOException("sin red")

        assertTrue(fallo { repo.sincronizar() }.sinConexion)
    }

    // ---------- crear ----------

    @Test
    fun crearEnviaNombreSinEspaciosYGuardaConLaImagenLocal() = runTest {
        coEvery { api.crear(DispositivoPeticion("Table", "ANDROID")) } returns dto(7)

        val id = repo.crear(1, "  Table  ", SistemaOperativo.ANDROID, "fotos/a.jpg")

        assertEquals(7L, id)
        coVerify { sincronizacion.guardarDispositivo(match { it.id == 7L && it.imagenRuta == "fotos/a.jpg" }) }
    }

    @Test
    fun crearConSistemaDesconocidoEnLaRespuestaLanzaError() = runTest {
        coEvery { api.crear(any()) } returns dto(7, sistema = "LINUX")

        val e = fallo { repo.crear(1, "Table", SistemaOperativo.ANDROID) }

        assertTrue(e.message!!.contains("desconocido"))
        coVerify(exactly = 0) { sincronizacion.guardarDispositivo(any()) }
    }

    @Test
    fun crearConNombreRepetidoPropagaElConflicto() = runTest {
        coEvery { api.crear(any()) } throws http(409)

        assertTrue(fallo { repo.crear(1, "Table", SistemaOperativo.ANDROID) }.conflicto)
    }

    // ---------- actualizar ----------

    @Test
    fun actualizarUnDispositivoInexistenteDevuelveFalse() = runTest {
        coEvery { dao.obtener(1) } returns null

        assertFalse(repo.actualizar(1, "Nuevo", SistemaOperativo.IOS))

        coVerify(exactly = 0) { api.actualizar(any(), any()) }
    }

    @Test
    fun actualizarUsaLaRespuestaDelServicioYConservaLoLocal() = runTest {
        coEvery { dao.obtener(1) } returns local
        coEvery { api.actualizar(1, DispositivoPeticion("Nuevo", "IOS")) } returns dto(1, "Nuevo", "IOS")

        assertTrue(repo.actualizar(1, " Nuevo ", SistemaOperativo.IOS))

        coVerify {
            dao.actualizar(
                match {
                    it.nombre == "Nuevo" && it.sistemaOperativo == SistemaOperativo.IOS &&
                        it.actualizadoEn == 50L && it.imagenRuta == "fotos/a.jpg"
                }
            )
        }
    }

    @Test
    fun actualizarConErrorDelServicioNoCambiaRoom() = runTest {
        coEvery { dao.obtener(1) } returns local
        coEvery { api.actualizar(any(), any()) } throws http(409)

        fallo { repo.actualizar(1, "Nuevo", SistemaOperativo.IOS) }

        coVerify(exactly = 0) { dao.actualizar(any()) }
    }

    // ---------- eliminar ----------

    @Test
    fun eliminarUnDispositivoInexistenteDevuelveNoEncontrado() = runTest {
        coEvery { dao.obtener(1) } returns null

        assertEquals(ResultadoEliminacion.NoEncontrado, repo.eliminar(1))

        coVerify(exactly = 0) { api.eliminar(any()) }
    }

    @Test
    fun eliminarSinEvaluacionesBorraEnElServicioYDevuelveLaRuta() = runTest {
        coEvery { dao.obtener(1) } returns local
        coEvery { dao.contarEvaluaciones(1) } returns 0
        coEvery { dao.eliminarOArchivar(1, 99L) } returns false
        coEvery { api.eliminar(1) } returns Unit

        val resultado = repo.eliminar(1)

        assertEquals(ResultadoEliminacion.Eliminado("fotos/a.jpg"), resultado)
        coVerify { api.eliminar(1) }
    }

    @Test
    fun eliminarConEvaluacionesLoArchivaEnElServicio() = runTest {
        coEvery { dao.obtener(1) } returns local
        coEvery { dao.contarEvaluaciones(1) } returns 3
        coEvery { dao.eliminarOArchivar(1, 99L) } returns true
        coEvery { api.cambiarArchivado(1, ArchivadoPeticion(true)) } returns dto(1)

        assertEquals(ResultadoEliminacion.Archivado, repo.eliminar(1))

        coVerify(exactly = 0) { api.eliminar(any()) }
    }

    @Test
    fun eliminarToleraQueElServicioYNoLoTenga() = runTest {
        coEvery { dao.obtener(1) } returns local
        coEvery { dao.contarEvaluaciones(1) } returns 0
        coEvery { dao.eliminarOArchivar(1, 99L) } returns false
        coEvery { api.eliminar(1) } throws http(404)

        assertEquals(ResultadoEliminacion.Eliminado("fotos/a.jpg"), repo.eliminar(1))
    }

    @Test
    fun eliminarToleraQueYaEstuvieraArchivadoEnElServicio() = runTest {
        coEvery { dao.obtener(1) } returns local
        coEvery { dao.contarEvaluaciones(1) } returns 2
        coEvery { dao.eliminarOArchivar(1, 99L) } returns true
        coEvery { api.cambiarArchivado(1, any()) } throws http(409)

        assertEquals(ResultadoEliminacion.Archivado, repo.eliminar(1))
    }

    @Test
    fun eliminarConErrorDelServicioNoTocaRoom() = runTest {
        coEvery { dao.obtener(1) } returns local
        coEvery { dao.contarEvaluaciones(1) } returns 0
        coEvery { api.eliminar(1) } throws http(500)

        fallo { repo.eliminar(1) }

        coVerify(exactly = 0) { dao.eliminarOArchivar(any(), any()) }
    }
}
