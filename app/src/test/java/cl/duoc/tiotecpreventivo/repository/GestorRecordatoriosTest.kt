package cl.duoc.tiotecpreventivo.repository

import cl.duoc.tiotecpreventivo.data.local.entity.EstadoRecordatorio
import cl.duoc.tiotecpreventivo.data.local.entity.TipoRecordatorio
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/** TT-16: guardar en Room y programar, reprogramar o cancelar el aviso. */
class GestorRecordatoriosTest {

    private val ahora = 1_000_000_000L
    private val dao = RecordatorioDaoFalso()
    private val programador = ProgramadorFalso()
    private val gestor = GestorRecordatorios(RecordatorioRepository(dao) { ahora }, programador)

    @Test
    fun crearGuardaYProgramaElAviso(): Unit = runBlocking {
        val id = gestor.crearParaAccion(accionId = 7, titulo = "Activar el bloqueo de pantalla", fechaHora = ahora + 60_000)

        val guardado = dao.obtener(id)!!
        assertEquals(TipoRecordatorio.ACCION, guardado.tipo)
        assertEquals(7L, guardado.accionId)
        assertEquals(EstadoRecordatorio.PROGRAMADO, guardado.estado)
        assertEquals(mapOf(id to ahora + 60_000), programador.programados)
        assertEquals(1, gestor.observarActivos().first().size)
    }

    @Test
    fun reprogramarCambiaLaHoraYReemplazaElAviso(): Unit = runBlocking {
        val id = gestor.crearParaAccion(7, "Activar el bloqueo de pantalla", ahora + 60_000)

        assertTrue(gestor.reprogramar(id, "Activar el bloqueo de pantalla", ahora + 120_000))

        assertEquals(ahora + 120_000, dao.obtener(id)!!.fechaHora)
        assertEquals(mapOf(id to ahora + 120_000), programador.programados)
        assertEquals(1, dao.filas.value.size)
    }

    @Test
    fun cancelarQuitaElAvisoYMarcaCancelado(): Unit = runBlocking {
        val id = gestor.crearParaAccion(7, "Activar el bloqueo de pantalla", ahora + 60_000)

        assertTrue(gestor.cancelar(id))

        assertEquals(EstadoRecordatorio.CANCELADO, dao.obtener(id)!!.estado)
        assertTrue(programador.programados.isEmpty())
        assertTrue(gestor.observarActivos().first().isEmpty())
    }

    @Test
    fun noSeReprogramaUnRecordatorioCancelado(): Unit = runBlocking {
        val id = gestor.crearParaAccion(7, "Activar el bloqueo de pantalla", ahora + 60_000)
        gestor.cancelar(id)
        val programacionesAntes = programador.vecesProgramado

        assertFalse(gestor.reprogramar(id, "Activar el bloqueo de pantalla", ahora + 120_000))

        assertEquals(programacionesAntes, programador.vecesProgramado)
        assertNull(programador.programados[id])
    }

    @Test
    fun fechaPasadaNoSeGuardaNiSeProgramaAviso(): Unit = runBlocking {
        try {
            gestor.crearParaAccion(7, "Activar el bloqueo de pantalla", ahora - 1)
            fail("Debía rechazar una fecha pasada")
        } catch (e: IllegalArgumentException) {
            assertTrue(dao.filas.value.isEmpty())
            assertTrue(programador.programados.isEmpty())
        }
    }
}
