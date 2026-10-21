package cl.duoc.tiotecpreventivo.domain

import cl.duoc.tiotecpreventivo.data.catalogo.CatalogoLocal
import cl.duoc.tiotecpreventivo.data.catalogo.CatalogoRepositoryLocal
import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.data.catalogo.ValorRespuesta
import cl.duoc.tiotecpreventivo.data.catalogo.ValorRespuesta.NO
import cl.duoc.tiotecpreventivo.data.catalogo.ValorRespuesta.NO_SE
import cl.duoc.tiotecpreventivo.data.catalogo.ValorRespuesta.SI
import cl.duoc.tiotecpreventivo.data.local.entity.NivelRiesgo
import cl.duoc.tiotecpreventivo.data.local.entity.NivelRiesgo.RIESGO_ALTO
import cl.duoc.tiotecpreventivo.data.local.entity.NivelRiesgo.RIESGO_BAJO
import cl.duoc.tiotecpreventivo.data.local.entity.NivelRiesgo.RIESGO_MEDIO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * TT-10: reglas de puntuación y riesgo con respuestas ficticias.
 * Corre en la JVM, sin emulador: las reglas no dependen de Android.
 */
class ReglasRiesgoTest {

    private val catalogo = CatalogoRepositoryLocal()
    private val reglas = ReglasRiesgo(catalogo)

    /** Todas las preguntas con `base`, cambiando solo las indicadas. */
    private fun respuestas(
        base: ValorRespuesta = SI,
        vararg cambios: Pair<String, ValorRespuesta>
    ): Map<String, ValorRespuesta> =
        CatalogoLocal.preguntas.associate { it.id to base } + cambios

    private fun completa(respuestas: Map<String, ValorRespuesta>): ResultadoReglas.Completa {
        val resultado = reglas.evaluar(respuestas)
        assertTrue("Se esperaba evaluación completa: $resultado", resultado is ResultadoReglas.Completa)
        return resultado as ResultadoReglas.Completa
    }

    private fun ResultadoReglas.Completa.nivelDe(categoria: Categoria): NivelRiesgo =
        resultados.single { it.categoria == categoria }.nivel

    private fun ResultadoReglas.Completa.puntajeDe(categoria: Categoria): Int =
        resultados.single { it.categoria == categoria }.puntaje

    // ---------- Niveles extremos ----------

    @Test
    fun todoSiEsRiesgoBajoSinRecomendaciones() {
        val resultado = completa(respuestas(SI))

        assertEquals(Categoria.entries.toList(), resultado.resultados.map { it.categoria })
        resultado.resultados.forEach {
            assertEquals(it.categoria.name, RIESGO_BAJO, it.nivel)
            assertEquals(it.categoria.name, 0, it.puntaje)
        }
        assertEquals(RIESGO_BAJO, resultado.nivelGlobal)
        assertTrue(resultado.recomendaciones.isEmpty())
    }

    @Test
    fun todoNoEsRiesgoAltoConTodasLasRecomendaciones() {
        val resultado = completa(respuestas(NO))

        resultado.resultados.forEach { assertEquals(it.categoria.name, RIESGO_ALTO, it.nivel) }
        assertEquals(RIESGO_ALTO, resultado.nivelGlobal)
        assertEquals(CatalogoLocal.recomendaciones.size, resultado.recomendaciones.size)
    }

    @Test
    fun todoNoLoSeEsRiesgoMedio() {
        val resultado = completa(respuestas(NO_SE))

        resultado.resultados.forEach { assertEquals(it.categoria.name, RIESGO_MEDIO, it.nivel) }
        assertEquals(RIESGO_MEDIO, resultado.nivelGlobal)
        // "No lo sé" también activa las recomendaciones.
        assertEquals(CatalogoLocal.recomendaciones.size, resultado.recomendaciones.size)
    }

    // ---------- Prácticas clave ----------

    @Test
    fun unaPracticaNormalSinAplicarSigueSiendoRiesgoBajo() {
        // ACT-02 no es clave: 2 de 7 puntos = 28 %.
        val resultado = completa(respuestas(SI, "ACT-02" to NO))

        assertEquals(2, resultado.puntajeDe(Categoria.ACTUALIZACIONES))
        assertEquals(RIESGO_BAJO, resultado.nivelDe(Categoria.ACTUALIZACIONES))
        assertEquals(listOf("REC-ACT-02"), resultado.recomendaciones.map { it.id })
    }

    @Test
    fun practicaClaveConNoSubeAlMenosARiesgoMedio() {
        // AUT-01 es clave: 3 de 8 puntos = 37 % → medio.
        val resultado = completa(respuestas(SI, "AUT-01" to NO))

        assertEquals(3, resultado.puntajeDe(Categoria.AUTENTICACION))
        assertEquals(RIESGO_MEDIO, resultado.nivelDe(Categoria.AUTENTICACION))
        assertEquals(RIESGO_MEDIO, resultado.nivelGlobal)
    }

    @Test
    fun practicaClaveConNoLoSeTambienEsRiesgoMedio() {
        // RES-01 con "No lo sé": 2 de 7 puntos = 28 % (sería bajo), pero es clave → medio.
        val resultado = completa(respuestas(SI, "RES-01" to NO_SE))

        assertEquals(2, resultado.puntajeDe(Categoria.RESPALDOS))
        assertEquals(RIESGO_MEDIO, resultado.nivelDe(Categoria.RESPALDOS))
        assertTrue(resultado.resultados.single { it.categoria == Categoria.RESPALDOS }
            .explicacion.contains("práctica clave"))
    }

    @Test
    fun practicaClaveMasOtraSinAplicarEsRiesgoAlto() {
        // RES-01 (3) + RES-02 (2) = 5 de 7 = 71 %.
        val resultado = completa(respuestas(SI, "RES-01" to NO, "RES-02" to NO))

        assertEquals(RIESGO_ALTO, resultado.nivelDe(Categoria.RESPALDOS))
    }

    // ---------- Caso mixto: usuario ficticio intermedio ----------

    @Test
    fun casoMixtoCalculaCadaCategoriaYElNivelGlobal() {
        val resultado = completa(
            respuestas(
                SI,
                "RES-02" to NO, "RES-03" to NO,  // 4/7 = 57 % → medio
                "ACT-03" to NO_SE,                // 1/7 = 14 % → bajo
                "AUT-02" to NO, "AUT-03" to NO,  // 5/8 = 62 % → medio (además AUT-02 es clave)
                "RCU-01" to NO, "RCU-02" to NO   // 5/7 = 71 % → alto
            )
        )

        assertEquals(4, resultado.puntajeDe(Categoria.RESPALDOS))
        assertEquals(1, resultado.puntajeDe(Categoria.ACTUALIZACIONES))
        assertEquals(5, resultado.puntajeDe(Categoria.AUTENTICACION))
        assertEquals(5, resultado.puntajeDe(Categoria.RECUPERACION))

        assertEquals(RIESGO_MEDIO, resultado.nivelDe(Categoria.RESPALDOS))
        assertEquals(RIESGO_BAJO, resultado.nivelDe(Categoria.ACTUALIZACIONES))
        assertEquals(RIESGO_MEDIO, resultado.nivelDe(Categoria.AUTENTICACION))
        assertEquals(RIESGO_ALTO, resultado.nivelDe(Categoria.RECUPERACION))

        // El nivel global es el peor de las categorías.
        assertEquals(RIESGO_ALTO, resultado.nivelGlobal)
    }

    @Test
    fun recomendacionesPorPrioridadYLuegoPorCategoriaConMasRiesgo() {
        val resultado = completa(
            respuestas(
                SI,
                "RES-02" to NO, "RES-03" to NO,
                "ACT-03" to NO_SE,
                "AUT-02" to NO, "AUT-03" to NO,
                "RCU-01" to NO, "RCU-02" to NO
            )
        )

        assertEquals(
            listOf(
                // ALTA: Recuperación (riesgo alto) antes que Autenticación (medio)
                "REC-RCU-01", "REC-AUT-02",
                // MEDIA: alto → medio (orden del catálogo) → bajo
                "REC-RCU-02", "REC-RES-02", "REC-AUT-03", "REC-ACT-03",
                // BAJA
                "REC-RES-03"
            ),
            resultado.recomendaciones.map { it.id }
        )
    }

    // ---------- Respuestas incompletas ----------

    @Test
    fun sinRespuestasFaltanTodasLasPreguntas() {
        val resultado = reglas.evaluar(emptyMap())

        assertTrue(resultado is ResultadoReglas.Incompleta)
        assertEquals(
            catalogo.preguntas().map { it.id },
            (resultado as ResultadoReglas.Incompleta).faltantes
        )
    }

    @Test
    fun conRespuestasIncompletasNoSeCalculaNada() {
        val parciales = respuestas(SI) - "AUT-03" - "RES-02"
        val resultado = reglas.evaluar(parciales)

        assertTrue(resultado is ResultadoReglas.Incompleta)
        // En el orden del cuestionario, no en el orden en que se quitaron.
        assertEquals(listOf("RES-02", "AUT-03"), (resultado as ResultadoReglas.Incompleta).faltantes)
    }

    @Test
    fun respuestasDesconocidasSeIgnoranYNoCompletanLaEvaluacion() {
        val parciales = respuestas(SI) - "RCU-03" + ("XXX-99" to NO)
        val resultado = reglas.evaluar(parciales)

        assertEquals(ResultadoReglas.Incompleta(listOf("RCU-03")), resultado)

        val completas = respuestas(SI) + ("XXX-99" to NO)
        assertEquals(RIESGO_BAJO, completa(completas).nivelGlobal)
    }

    @Test(expected = IllegalArgumentException::class)
    fun resultadoCategoriaSinRespuestasLanzaError() {
        reglas.resultadoCategoria(Categoria.RESPALDOS, emptyMap())
    }

    // ---------- Umbrales y coherencia ----------

    @Test
    fun umbralesDeNivel() {
        assertEquals(RIESGO_BAJO, ReglasRiesgo.nivelPorPorcentaje(0))
        assertEquals(RIESGO_BAJO, ReglasRiesgo.nivelPorPorcentaje(33))
        assertEquals(RIESGO_MEDIO, ReglasRiesgo.nivelPorPorcentaje(34))
        assertEquals(RIESGO_MEDIO, ReglasRiesgo.nivelPorPorcentaje(66))
        assertEquals(RIESGO_ALTO, ReglasRiesgo.nivelPorPorcentaje(67))
        assertEquals(RIESGO_ALTO, ReglasRiesgo.nivelPorPorcentaje(100))
    }

    @Test
    fun puntajeDeCategoriaEsLaSumaDeLosPuntajesDelCatalogo() {
        // Mismo cálculo que usa EvaluacionRepository al guardar cada respuesta.
        val respuestas = respuestas(NO_SE, "RES-01" to NO, "AUT-03" to SI)
        val resultado = completa(respuestas)

        Categoria.entries.forEach { categoria ->
            val esperado = catalogo.preguntasDe(categoria)
                .sumOf { it.puntajes.getValue(respuestas.getValue(it.id)) }
            assertEquals(categoria.name, esperado, resultado.puntajeDe(categoria))
        }
    }

    @Test
    fun cadaResultadoExplicaElNivelYAvisaQueEsOrientativo() {
        val resultado = completa(respuestas(NO_SE, "ACT-01" to SI))

        resultado.resultados.forEach {
            assertTrue(it.categoria.name, it.explicacion.startsWith(it.nivel.etiqueta))
            assertTrue(it.categoria.name, it.explicacion.contains(it.categoria.nombre))
        }
        assertTrue(resultado.resultados.first().explicacion.contains("No lo sé"))
        assertTrue(resultado.aviso.contains("no revisa ni escanea"))
    }
}
