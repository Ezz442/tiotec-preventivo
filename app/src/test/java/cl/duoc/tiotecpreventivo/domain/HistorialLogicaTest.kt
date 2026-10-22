package cl.duoc.tiotecpreventivo.domain

import cl.duoc.tiotecpreventivo.data.local.entity.EvaluacionEntity
import cl.duoc.tiotecpreventivo.data.local.entity.NivelRiesgo
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** TT-14: orden y filtro del historial con evaluaciones ficticias. */
class HistorialLogicaTest {

    private fun evaluacion(id: Long, dispositivoId: Long, nombre: String, fecha: Long) =
        EvaluacionEntity(id, dispositivoId, nombre, SistemaOperativo.ANDROID, fecha, NivelRiesgo.RIESGO_MEDIO)

    private val evaluaciones = listOf(
        evaluacion(1, dispositivoId = 10, nombre = "Teléfono antiguo", fecha = 100),
        evaluacion(2, dispositivoId = 20, nombre = "computador de prueba", fecha = 200),
        evaluacion(3, dispositivoId = 10, nombre = "Teléfono de prueba", fecha = 300),
        evaluacion(4, dispositivoId = 10, nombre = "Teléfono de prueba", fecha = 300)
    )

    @Test
    fun ordenaLaMasRecientePrimero() {
        assertEquals(listOf(4L, 3L, 2L, 1L), HistorialLogica.ordenar(evaluaciones).map { it.id })
    }

    @Test
    fun unFiltroPorDispositivoConSuNombreMasRecienteYCantidad() {
        val filtros = HistorialLogica.filtros(evaluaciones)

        assertEquals(
            listOf(
                FiltroDispositivo(20, "computador de prueba", 1),
                FiltroDispositivo(10, "Teléfono de prueba", 3)
            ),
            filtros
        )
    }

    @Test
    fun filtrarPorDispositivoMantieneElOrden() {
        assertEquals(listOf(4L, 3L, 1L), HistorialLogica.filtrar(evaluaciones, 10).map { it.id })
        assertEquals(listOf(2L), HistorialLogica.filtrar(evaluaciones, 20).map { it.id })
        assertEquals(4, HistorialLogica.filtrar(evaluaciones, null).size)
    }

    @Test
    fun evaluacionAntiguaConservaElNombreQueTeniaAlEvaluar() {
        // El dispositivo 10 se renombró: la evaluación 1 sigue diciendo "Teléfono antiguo".
        val antigua = HistorialLogica.filtrar(evaluaciones, 10).last()
        assertEquals("Teléfono antiguo", antigua.nombreDispositivo)
    }

    @Test
    fun filtroUsaElNombreActualDelDispositivoAunqueNoSeHayaVueltoAEvaluar() {
        // Caso de la revisión de Gerard: se renombra sin evaluar de nuevo.
        val soloAntiguas = listOf(
            evaluacion(1, dispositivoId = 10, nombre = "iaintegradajorge", fecha = 100),
            evaluacion(2, dispositivoId = 10, nombre = "iaintegradajorge", fecha = 200)
        )
        val filtros = HistorialLogica.filtros(soloAntiguas, mapOf(10L to "superiajorge"))

        assertEquals(listOf(FiltroDispositivo(10, "superiajorge", 2)), filtros)
        // Las evaluaciones no cambian: siguen con el nombre que tenían al evaluarse.
        assertTrue(HistorialLogica.filtrar(soloAntiguas, 10).all { it.nombreDispositivo == "iaintegradajorge" })
    }

    @Test
    fun sinNombreActualSeUsaElDeLaEvaluacionMasReciente() {
        val filtros = HistorialLogica.filtros(evaluaciones, mapOf(20L to "Notebook de prueba"))

        assertEquals("Notebook de prueba", filtros.single { it.dispositivoId == 20L }.nombre)
        assertEquals("Teléfono de prueba", filtros.single { it.dispositivoId == 10L }.nombre)
    }

    @Test
    fun historialVacio() {
        assertTrue(HistorialLogica.filtros(emptyList()).isEmpty())
        assertTrue(HistorialLogica.filtrar(emptyList(), null).isEmpty())
    }
}
