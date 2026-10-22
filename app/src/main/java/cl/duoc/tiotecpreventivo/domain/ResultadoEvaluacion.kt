package cl.duoc.tiotecpreventivo.domain

import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.data.catalogo.Prioridad
import cl.duoc.tiotecpreventivo.data.catalogo.ValorRespuesta
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoAccion
import cl.duoc.tiotecpreventivo.data.local.entity.EvaluacionConDetalle
import cl.duoc.tiotecpreventivo.data.local.entity.NivelRiesgo
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo
import cl.duoc.tiotecpreventivo.repository.CatalogoRepository

/** Resultado de una evaluación guardada, listo para mostrar (TT-12). */
data class ResultadoEvaluacion(
    val evaluacionId: Long,
    val nombreDispositivo: String,
    val sistemaOperativo: SistemaOperativo,
    val fecha: Long,
    val nivelGlobal: NivelRiesgo,
    val categorias: List<ResultadoCategoriaVista>,
    val recomendaciones: List<RecomendacionVista>,
    val aviso: String = ReglasRiesgo.AVISO_ORIENTACION
)

data class ResultadoCategoriaVista(
    val categoria: Categoria,
    val nivel: NivelRiesgo,
    val puntaje: Int,
    val puntajeMaximo: Int,
    val explicacion: String
)

/**
 * Recomendación junto con la respuesta que la activó, para que la persona entienda
 * por qué aparece.
 * @param accionId acción creada en el checklist al guardar (TT-13); null si no se encontró.
 * @param contenidoId contenido educativo de "Aprender cómo hacerlo" (TT-15).
 */
data class RecomendacionVista(
    val recomendacionId: String,
    val titulo: String,
    val descripcion: String,
    val prioridad: Prioridad,
    val categoria: Categoria,
    val textoPregunta: String,
    val respuesta: ValorRespuesta,
    val accionId: Long?,
    val estadoAccion: EstadoAccion?,
    val contenidoId: String?
)

/**
 * Convierte lo que guardó Room (TT-07/TT-11) en lo que muestra la pantalla de resultados.
 *
 * Solo lee: no recalcula niveles (usa los guardados, para que el historial no cambie si
 * en el futuro cambian las reglas) y no escribe nada, así que consultar el resultado o el
 * historial nunca crea registros duplicados.
 */
class ResultadoEvaluacionMapper(
    private val catalogo: CatalogoRepository,
    private val reglas: ReglasRiesgo = ReglasRiesgo(catalogo)
) {

    fun construir(detalle: EvaluacionConDetalle): ResultadoEvaluacion {
        val categorias = detalle.resultados
            .sortedBy { it.categoria.ordinal }
            .map {
                ResultadoCategoriaVista(
                    categoria = it.categoria,
                    nivel = it.nivel,
                    puntaje = it.puntaje,
                    puntajeMaximo = reglas.puntajeMaximo(it.categoria),
                    explicacion = it.explicacion
                )
            }

        val respuestasPorPregunta = detalle.respuestas.associateBy { it.preguntaId }
        val accionesPorRecomendacion = detalle.acciones.associateBy { it.recomendacionId }
        val nivelPorCategoria = categorias.associate { it.categoria to it.nivel }

        val activadas = catalogo.recomendacionesPara(
            detalle.respuestas.associate { it.preguntaId to it.valor }
        )

        val recomendaciones = reglas.ordenarRecomendaciones(activadas, nivelPorCategoria)
            .mapNotNull { recomendacion ->
                val respuesta = respuestasPorPregunta[recomendacion.preguntaId]
                    ?: return@mapNotNull null
                val accion = accionesPorRecomendacion[recomendacion.id]

                RecomendacionVista(
                    recomendacionId = recomendacion.id,
                    // La acción guarda una copia del texto: si existe, se muestra esa.
                    titulo = accion?.titulo ?: recomendacion.titulo,
                    descripcion = accion?.descripcion ?: recomendacion.descripcion,
                    prioridad = recomendacion.prioridad,
                    categoria = respuesta.categoria,
                    textoPregunta = respuesta.textoPregunta,
                    respuesta = respuesta.valor,
                    accionId = accion?.id,
                    estadoAccion = accion?.estado,
                    contenidoId = recomendacion.contenidoId
                )
            }

        return ResultadoEvaluacion(
            evaluacionId = detalle.evaluacion.id,
            nombreDispositivo = detalle.evaluacion.nombreDispositivo,
            sistemaOperativo = detalle.evaluacion.sistemaOperativoDispositivo,
            fecha = detalle.evaluacion.fecha,
            nivelGlobal = detalle.evaluacion.nivelGlobal,
            categorias = categorias,
            recomendaciones = recomendaciones
        )
    }
}
