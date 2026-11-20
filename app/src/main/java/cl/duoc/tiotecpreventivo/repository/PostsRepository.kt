package cl.duoc.tiotecpreventivo.repository

import cl.duoc.tiotecpreventivo.data.model.ConsejoComunidad
import cl.duoc.tiotecpreventivo.data.remote.PostsApi
import cl.duoc.tiotecpreventivo.data.remote.llamarApiExterna
import cl.duoc.tiotecpreventivo.data.remote.model.PostDto

/**
 * TT-30: lee publicaciones de la API externa y las convierte a modelo de pantalla.
 * Solo en memoria: no toca Room ni los microservicios.
 */
class PostsRepository(private val api: PostsApi) {

    /** @throws cl.duoc.tiotecpreventivo.data.remote.ErrorRemoto si la API falla. */
    suspend fun obtenerConsejos(limite: Int = LIMITE_POR_DEFECTO): List<ConsejoComunidad> =
        llamarApiExterna { api.posts(limite) }.map { it.aConsejo() }

    companion object {
        const val LIMITE_POR_DEFECTO = 10
    }
}

internal fun PostDto.aConsejo() = ConsejoComunidad(
    id = id,
    titulo = title.trim().replaceFirstChar { it.uppercase() },
    texto = body.replace('\n', ' ').trim()
)
