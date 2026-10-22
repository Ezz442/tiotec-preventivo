package cl.duoc.tiotecpreventivo.domain

import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.data.catalogo.Pregunta
import cl.duoc.tiotecpreventivo.data.catalogo.Recomendacion
import cl.duoc.tiotecpreventivo.data.catalogo.ValorRespuesta
import cl.duoc.tiotecpreventivo.data.local.entity.NivelRiesgo
import cl.duoc.tiotecpreventivo.repository.CatalogoRepository
import cl.duoc.tiotecpreventivo.repository.ResultadoCalculado

/** Resultado de aplicar las reglas a un conjunto de respuestas. */
sealed interface ResultadoReglas {

    /**
     * Todas las preguntas respondidas.
     * @param resultados uno por categoría, en el orden del catálogo.
     * @param recomendaciones activadas, ordenadas por prioridad y luego por la categoría con más riesgo.
     */
    data class Completa(
        val resultados: List<ResultadoCalculado>,
        val nivelGlobal: NivelRiesgo,
        val recomendaciones: List<Recomendacion>,
        val aviso: String = ReglasRiesgo.AVISO_ORIENTACION
    ) : ResultadoReglas

    /** Faltan respuestas: no se calcula nada a medias. `faltantes` va en el orden del cuestionario. */
    data class Incompleta(val faltantes: List<String>) : ResultadoReglas
}

/**
 * Reglas de riesgo de TT-10 (ver docs/TT-10-reglas-riesgo.md).
 *
 * Lógica pura, sin Android ni Compose: el ViewModel de la evaluación (TT-11/TT-12)
 * llama a [evaluar] y entrega los resultados a `EvaluacionRepository.guardarEvaluacion`.
 *
 * El resultado orienta según lo que la persona responde; no revisa ni escanea el dispositivo.
 */
class ReglasRiesgo(private val catalogo: CatalogoRepository) {

    /** Calcula resultados por categoría, nivel global y recomendaciones priorizadas. */
    fun evaluar(respuestas: Map<String, ValorRespuesta>): ResultadoReglas {
        val faltantes = preguntasFaltantes(respuestas)
        if (faltantes.isNotEmpty()) return ResultadoReglas.Incompleta(faltantes)

        val resultados = catalogo.categorias().map { resultadoCategoria(it, respuestas) }
        val nivelPorCategoria = resultados.associate { it.categoria to it.nivel }

        val recomendaciones = catalogo.recomendacionesPara(respuestas).sortedWith(
            compareBy<Recomendacion> { it.prioridad.ordinal }
                .thenByDescending { recomendacion ->
                    catalogo.pregunta(recomendacion.preguntaId)
                        ?.let { nivelPorCategoria[it.categoria]?.ordinal } ?: 0
                }
            // sortedWith es estable: dentro del mismo nivel se mantiene el orden del catálogo.
        )

        return ResultadoReglas.Completa(
            resultados = resultados,
            nivelGlobal = resultados.maxOf { it.nivel },
            recomendaciones = recomendaciones
        )
    }

    /** IDs de preguntas sin responder, en el orden del cuestionario. Los IDs desconocidos se ignoran. */
    fun preguntasFaltantes(respuestas: Map<String, ValorRespuesta>): List<String> =
        catalogo.preguntas().map { it.id }.filterNot { it in respuestas }

    /** Resultado de una categoría. Requiere que todas sus preguntas estén respondidas. */
    fun resultadoCategoria(
        categoria: Categoria,
        respuestas: Map<String, ValorRespuesta>
    ): ResultadoCalculado {
        val preguntas = catalogo.preguntasDe(categoria)
        val respondidas = preguntas.map { pregunta ->
            val valor = requireNotNull(respuestas[pregunta.id]) { "Falta la respuesta de ${pregunta.id}" }
            pregunta to valor
        }

        val puntaje = respondidas.sumOf { (pregunta, valor) -> pregunta.puntajes.getValue(valor) }
        val maximo = preguntas.sumOf { pregunta -> pregunta.puntajes.values.max() }
        val porcentaje = if (maximo == 0) 0 else puntaje * 100 / maximo

        val claveSinAplicar = respondidas.any { (pregunta, valor) ->
            pregunta.clave && valor != ValorRespuesta.SI
        }

        var nivel = nivelPorPorcentaje(porcentaje)
        if (claveSinAplicar && nivel < NivelRiesgo.RIESGO_MEDIO) nivel = NivelRiesgo.RIESGO_MEDIO

        return ResultadoCalculado(
            categoria = categoria,
            puntaje = puntaje,
            nivel = nivel,
            explicacion = explicacion(categoria, respondidas, nivel, claveSinAplicar)
        )
    }

    private fun explicacion(
        categoria: Categoria,
        respondidas: List<Pair<Pregunta, ValorRespuesta>>,
        nivel: NivelRiesgo,
        claveSinAplicar: Boolean
    ): String {
        val aplicadas = respondidas.count { it.second == ValorRespuesta.SI }
        val noSabe = respondidas.count { it.second == ValorRespuesta.NO_SE }

        return buildString {
            append("${nivel.etiqueta} en ${categoria.nombre}: aplicas $aplicadas de ${respondidas.size} buenas prácticas.")
            if (claveSinAplicar) {
                append(" Falta confirmar una práctica clave, por eso el riesgo es al menos medio.")
            }
            if (noSabe > 0) {
                val preguntas = if (noSabe == 1) "1 pregunta" else "$noSabe preguntas"
                append(" Respondiste \"No lo sé\" en $preguntas: se cuenta como riesgo hasta que lo confirmes.")
            }
            if (nivel == NivelRiesgo.RIESGO_BAJO && aplicadas == respondidas.size) {
                append(" Sigue así.")
            }
        }
    }

    companion object {
        /** Desde este porcentaje del puntaje máximo de la categoría el riesgo es medio. */
        const val UMBRAL_MEDIO = 34

        /** Desde este porcentaje del puntaje máximo de la categoría el riesgo es alto. */
        const val UMBRAL_ALTO = 67

        const val AVISO_ORIENTACION =
            "Este resultado es una orientación según tus respuestas. " +
                "La app no revisa ni escanea tu dispositivo."

        fun nivelPorPorcentaje(porcentaje: Int): NivelRiesgo = when {
            porcentaje >= UMBRAL_ALTO -> NivelRiesgo.RIESGO_ALTO
            porcentaje >= UMBRAL_MEDIO -> NivelRiesgo.RIESGO_MEDIO
            else -> NivelRiesgo.RIESGO_BAJO
        }
    }
}
