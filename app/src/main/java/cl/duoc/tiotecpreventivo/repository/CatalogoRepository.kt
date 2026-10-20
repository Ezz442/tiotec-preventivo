package cl.duoc.tiotecpreventivo.repository

import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.data.catalogo.Contenido
import cl.duoc.tiotecpreventivo.data.catalogo.Pregunta
import cl.duoc.tiotecpreventivo.data.catalogo.Recomendacion
import cl.duoc.tiotecpreventivo.data.catalogo.ValorRespuesta

/**
 * Acceso al catálogo fijo (TT-04, sección 5). Los ViewModels usan esta interfaz,
 * nunca el objeto CatalogoLocal directamente.
 */
interface CatalogoRepository {

    fun categorias(): List<Categoria>

    /** Preguntas ordenadas por categoría y luego por su orden dentro de la categoría. */
    fun preguntas(): List<Pregunta>

    fun preguntasDe(categoria: Categoria): List<Pregunta>

    fun pregunta(id: String): Pregunta?

    /**
     * Recomendaciones activadas por las respuestas, ordenadas ALTA → MEDIA → BAJA.
     * @param respuestas mapa preguntaId → valor respondido. Se ignoran IDs desconocidos.
     */
    fun recomendacionesPara(respuestas: Map<String, ValorRespuesta>): List<Recomendacion>

    /** Contenidos educativos; con `categoria = null` devuelve todos. */
    fun contenidos(categoria: Categoria? = null): List<Contenido>

    fun contenido(id: String): Contenido?
}
