package cl.duoc.tiotecpreventivo.domain

import cl.duoc.tiotecpreventivo.data.catalogo.CatalogoLocal
import cl.duoc.tiotecpreventivo.data.catalogo.CatalogoRepositoryLocal
import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.data.catalogo.ValorRespuesta
import cl.duoc.tiotecpreventivo.data.catalogo.ValorRespuesta.NO
import cl.duoc.tiotecpreventivo.data.catalogo.ValorRespuesta.NO_SE
import cl.duoc.tiotecpreventivo.data.catalogo.ValorRespuesta.SI
import cl.duoc.tiotecpreventivo.data.local.entity.AccionEntity
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoAccion
import cl.duoc.tiotecpreventivo.data.local.entity.EvaluacionConDetalle
import cl.duoc.tiotecpreventivo.data.local.entity.EvaluacionEntity
import cl.duoc.tiotecpreventivo.data.local.entity.NivelRiesgo
import cl.duoc.tiotecpreventivo.data.local.entity.RespuestaEntity
import cl.duoc.tiotecpreventivo.data.local.entity.ResultadoCategoriaEntity
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * TT-12: la pantalla de resultados muestra lo que se guardó, con cada recomendación
 * relacionada a su respuesta y prioridad. Corre en la JVM, sin emulador.
 */
class ResultadoEvaluacionMapperTest {

    private val catalogo = CatalogoRepositoryLocal()
    private val reglas = ReglasRiesgo(catalogo)
    private val mapper = ResultadoEvaluacionMapper(catalogo)

    /** Arma lo mismo que guarda EvaluacionRepository.guardarEvaluacion (TT-07). */
    private fun detalleGuardado(
        respuestas: Map<String, ValorRespuesta>,
        conAcciones: Boolean = true
    ): EvaluacionConDetalle {
        val completa = reglas.evaluar(respuestas) as ResultadoReglas.Completa
        return EvaluacionConDetalle(
            evaluacion = EvaluacionEntity(
                id = 7,
                dispositivoId = 3,
                nombreDispositivo = "Teléfono de prueba",
                sistemaOperativoDispositivo = SistemaOperativo.ANDROID,
                fecha = 1_000L,
                nivelGlobal = completa.nivelGlobal
            ),
            respuestas = catalogo.preguntas().map {
                val valor = respuestas.getValue(it.id)
                RespuestaEntity(7, it.id, it.categoria, it.texto, valor, it.puntajes.getValue(valor))
            },
            // Room no garantiza el orden: se entregan al revés a propósito.
            resultados = completa.resultados.reversed().map {
                ResultadoCategoriaEntity(7, it.categoria, it.puntaje, it.nivel, it.explicacion)
            },
            acciones = if (!conAcciones) emptyList() else
                catalogo.recomendacionesPara(respuestas).mapIndexed { i, r ->
                    AccionEntity(
                        id = 100L + i, evaluacionId = 7, recomendacionId = r.id,
                        titulo = r.titulo, descripcion = r.descripcion, prioridad = r.prioridad,
                        fechaRevision = 2_000L, actualizadoEn = 1_000L
                    )
                }
        )
    }

    private fun respuestas(base: ValorRespuesta, vararg cambios: Pair<String, ValorRespuesta>) =
        CatalogoLocal.preguntas.associate { it.id to base } + cambios

    private val mixtas = respuestas(
        SI,
        "RES-02" to NO, "RES-03" to NO,
        "ACT-03" to NO_SE,
        "AUT-02" to NO, "AUT-03" to NO,
        "RCU-01" to NO, "RCU-02" to NO
    )

    @Test
    fun muestraLosDatosGuardadosDeLaEvaluacion() {
        val resultado = mapper.construir(detalleGuardado(mixtas))

        assertEquals(7L, resultado.evaluacionId)
        assertEquals("Teléfono de prueba", resultado.nombreDispositivo)
        assertEquals(SistemaOperativo.ANDROID, resultado.sistemaOperativo)
        assertEquals(NivelRiesgo.RIESGO_ALTO, resultado.nivelGlobal)
        assertTrue(resultado.aviso.contains("no revisa ni escanea"))
    }

    @Test
    fun categoriasEnOrdenDelCatalogoConPuntajeMaximoYExplicacion() {
        val resultado = mapper.construir(detalleGuardado(mixtas))

        assertEquals(Categoria.entries.toList(), resultado.categorias.map { it.categoria })
        assertEquals(listOf(7, 7, 8, 7), resultado.categorias.map { it.puntajeMaximo })
        assertEquals(listOf(4, 1, 5, 5), resultado.categorias.map { it.puntaje })
        assertEquals(
            listOf(NivelRiesgo.RIESGO_MEDIO, NivelRiesgo.RIESGO_BAJO, NivelRiesgo.RIESGO_MEDIO, NivelRiesgo.RIESGO_ALTO),
            resultado.categorias.map { it.nivel }
        )
        resultado.categorias.forEach {
            assertTrue(it.categoria.name, it.explicacion.startsWith(it.nivel.etiqueta))
        }
    }

    @Test
    fun recomendacionesEnElMismoOrdenQueLasReglas() {
        val resultado = mapper.construir(detalleGuardado(mixtas))
        val esperado = (reglas.evaluar(mixtas) as ResultadoReglas.Completa).recomendaciones.map { it.id }

        assertEquals(esperado, resultado.recomendaciones.map { it.recomendacionId })
        val prioridades = resultado.recomendaciones.map { it.prioridad.ordinal }
        assertEquals(prioridades.sorted(), prioridades)
    }

    @Test
    fun cadaRecomendacionMuestraLaRespuestaQueLaActivo() {
        val resultado = mapper.construir(detalleGuardado(mixtas))

        val rcu01 = resultado.recomendaciones.single { it.recomendacionId == "REC-RCU-01" }
        assertEquals(Categoria.RECUPERACION, rcu01.categoria)
        assertEquals(catalogo.pregunta("RCU-01")!!.texto, rcu01.textoPregunta)
        assertEquals(NO, rcu01.respuesta)
        assertEquals("CON-RCU-01", rcu01.contenidoId)

        val act03 = resultado.recomendaciones.single { it.recomendacionId == "REC-ACT-03" }
        assertEquals(NO_SE, act03.respuesta)

        // Ninguna recomendación aparece por una respuesta "Sí".
        assertTrue(resultado.recomendaciones.none { it.respuesta == SI })
    }

    @Test
    fun cadaRecomendacionSeEnlazaConSuAccionDelChecklist() {
        val detalle = detalleGuardado(mixtas)
        val resultado = mapper.construir(detalle)

        resultado.recomendaciones.forEach { vista ->
            val accion = detalle.acciones.single { it.recomendacionId == vista.recomendacionId }
            assertEquals(accion.id, vista.accionId)
            assertEquals(EstadoAccion.PENDIENTE, vista.estadoAccion)
        }
    }

    @Test
    fun sinAccionesGuardadasLaRecomendacionSeMuestraIgual() {
        val resultado = mapper.construir(detalleGuardado(mixtas, conAcciones = false))

        assertEquals(7, resultado.recomendaciones.size)
        resultado.recomendaciones.forEach {
            assertNull(it.accionId)
            assertTrue(it.titulo.isNotBlank())
        }
    }

    @Test
    fun todoSiNoTieneRecomendaciones() {
        val resultado = mapper.construir(detalleGuardado(respuestas(SI)))

        assertEquals(NivelRiesgo.RIESGO_BAJO, resultado.nivelGlobal)
        assertTrue(resultado.recomendaciones.isEmpty())
        resultado.categorias.forEach { assertEquals(NivelRiesgo.RIESGO_BAJO, it.nivel) }
    }

    @Test
    fun usaLosNivelesGuardadosAunqueLasReglasCambien() {
        // El historial no debe cambiar: si Room dice "alto", se muestra "alto".
        val original = detalleGuardado(respuestas(SI))
        val guardado = original.copy(
            evaluacion = original.evaluacion.copy(nivelGlobal = NivelRiesgo.RIESGO_ALTO),
            resultados = original.resultados.map { it.copy(nivel = NivelRiesgo.RIESGO_ALTO) }
        )
        val resultado = mapper.construir(guardado)

        assertEquals(NivelRiesgo.RIESGO_ALTO, resultado.nivelGlobal)
        resultado.categorias.forEach { assertEquals(NivelRiesgo.RIESGO_ALTO, it.nivel) }
    }

    @Test
    fun muestraLasRespuestasGuardadasEnOrdenDelCuestionario() {
        val detalle = detalleGuardado(mixtas)
        // Room no garantiza el orden y el texto guardado puede ser distinto al del catálogo actual.
        val guardado = detalle.copy(
            respuestas = detalle.respuestas.reversed().map {
                if (it.preguntaId == "AUT-01") it.copy(textoPregunta = "Texto antiguo de AUT-01") else it
            }
        )
        val resultado = mapper.construir(guardado)

        assertEquals(catalogo.preguntas().map { it.id }, resultado.respuestas.map { it.preguntaId })
        assertEquals("Texto antiguo de AUT-01",
            resultado.respuestas.single { it.preguntaId == "AUT-01" }.textoPregunta)
        assertEquals(NO_SE, resultado.respuestas.single { it.preguntaId == "ACT-03" }.respuesta)
    }

    @Test
    fun consultarVariasVecesDaElMismoResultado() {
        val detalle = detalleGuardado(mixtas)
        val primera = mapper.construir(detalle)
        val segunda = mapper.construir(detalle)

        assertEquals(primera, segunda)
        assertEquals(detalle.acciones.size, segunda.recomendaciones.size)
    }
}
