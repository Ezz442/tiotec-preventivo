package cl.duoc.tiotecpreventivo.data.catalogo

import cl.duoc.tiotecpreventivo.repository.CatalogoRepository

/**
 * Implementación sin red ni base de datos: lee el catálogo definido en código.
 * Al ser datos fijos, cada llamada devuelve siempre el mismo contenido y nunca se duplica.
 */
class CatalogoRepositoryLocal : CatalogoRepository {

    private val catalogo = CatalogoLocal

    private val preguntasOrdenadas: List<Pregunta> by lazy {
        catalogo.preguntas.sortedWith(
            compareBy<Pregunta> { it.categoria.ordinal }.thenBy { it.orden }
        )
    }

    private val preguntasPorId: Map<String, Pregunta> by lazy {
        catalogo.preguntas.associateBy { it.id }
    }

    private val contenidosPorId: Map<String, Contenido> by lazy {
        catalogo.contenidos.associateBy { it.id }
    }

    override fun categorias(): List<Categoria> = catalogo.categorias

    override fun preguntas(): List<Pregunta> = preguntasOrdenadas

    override fun preguntasDe(categoria: Categoria): List<Pregunta> =
        preguntasOrdenadas.filter { it.categoria == categoria }

    override fun pregunta(id: String): Pregunta? = preguntasPorId[id]

    override fun recomendacionesPara(respuestas: Map<String, ValorRespuesta>): List<Recomendacion> =
        catalogo.recomendaciones
            .filter { recomendacion ->
                val valor = respuestas[recomendacion.preguntaId]
                valor != null && valor in recomendacion.valoresQueActivan
            }
            .sortedWith(
                compareBy<Recomendacion> { it.prioridad.ordinal }
                    .thenBy { preguntasPorId[it.preguntaId]?.categoria?.ordinal ?: Int.MAX_VALUE }
                    .thenBy { preguntasPorId[it.preguntaId]?.orden ?: Int.MAX_VALUE }
            )

    override fun contenidos(categoria: Categoria?): List<Contenido> =
        if (categoria == null) catalogo.contenidos
        else catalogo.contenidos.filter { it.categoria == categoria }

    override fun contenido(id: String): Contenido? = contenidosPorId[id]
}
