package cl.duoc.tiotecpreventivo.viewmodel

import cl.duoc.tiotecpreventivo.data.catalogo.CatalogoLocal
import cl.duoc.tiotecpreventivo.data.catalogo.CatalogoRepositoryLocal
import cl.duoc.tiotecpreventivo.data.catalogo.ValorRespuesta
import cl.duoc.tiotecpreventivo.data.local.entity.EvaluacionConDetalle
import cl.duoc.tiotecpreventivo.data.local.entity.EvaluacionEntity
import cl.duoc.tiotecpreventivo.data.local.entity.NivelRiesgo
import cl.duoc.tiotecpreventivo.data.local.entity.RespuestaEntity
import cl.duoc.tiotecpreventivo.data.local.entity.ResultadoCategoriaEntity
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo
import cl.duoc.tiotecpreventivo.domain.ReglasRiesgo
import cl.duoc.tiotecpreventivo.domain.ResultadoEvaluacionMapper
import cl.duoc.tiotecpreventivo.domain.ResultadoReglas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/** TT-12: el estado de la pantalla de resultados se expone desde el ViewModel. */
@OptIn(ExperimentalCoroutinesApi::class)
class ResultadoViewModelTest {

    private val catalogo = CatalogoRepositoryLocal()
    private val mapper = ResultadoEvaluacionMapper(catalogo)

    @Before
    fun usarDispatcherDePrueba() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun restaurarDispatcher() = Dispatchers.resetMain()

    private fun detalle(): EvaluacionConDetalle {
        val respuestas = CatalogoLocal.preguntas.associate { it.id to ValorRespuesta.SI } +
            ("AUT-01" to ValorRespuesta.NO)
        val completa = ReglasRiesgo(catalogo).evaluar(respuestas) as ResultadoReglas.Completa
        return EvaluacionConDetalle(
            evaluacion = EvaluacionEntity(1, 1, "Tablet de prueba", SistemaOperativo.IOS, 0L, completa.nivelGlobal),
            respuestas = catalogo.preguntas().map {
                val valor = respuestas.getValue(it.id)
                RespuestaEntity(1, it.id, it.categoria, it.texto, valor, it.puntajes.getValue(valor))
            },
            resultados = completa.resultados.map {
                ResultadoCategoriaEntity(1, it.categoria, it.puntaje, it.nivel, it.explicacion)
            },
            acciones = emptyList()
        )
    }

    @Test
    fun cargaElResultadoGuardado() {
        var consultas = 0
        val viewModel = ResultadoViewModel(1, { consultas++; detalle() }, mapper)

        val estado = viewModel.uiState.value
        assertFalse(estado.cargando)
        assertNull(estado.error)
        assertNotNull(estado.resultado)
        assertEquals(NivelRiesgo.RIESGO_MEDIO, estado.resultado!!.nivelGlobal)
        assertEquals(listOf("REC-AUT-01"), estado.resultado!!.recomendaciones.map { it.recomendacionId })
        assertEquals(1, consultas)
    }

    @Test
    fun evaluacionInexistenteMuestraMensaje() {
        val viewModel = ResultadoViewModel(99, { null }, mapper)

        val estado = viewModel.uiState.value
        assertFalse(estado.cargando)
        assertEquals("No encontramos esta evaluación.", estado.error)
        assertNull(estado.resultado)
    }

    @Test
    fun errorAlLeerPermiteReintentar() {
        var falla = true
        val viewModel = ResultadoViewModel(1, {
            if (falla) error("Base de datos no disponible") else detalle()
        }, mapper)

        assertEquals("No pudimos cargar el resultado.", viewModel.uiState.value.error)

        falla = false
        viewModel.reintentar()

        assertNull(viewModel.uiState.value.error)
        assertNotNull(viewModel.uiState.value.resultado)
    }
}
