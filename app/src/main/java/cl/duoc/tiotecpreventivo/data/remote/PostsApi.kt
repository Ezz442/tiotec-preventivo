package cl.duoc.tiotecpreventivo.data.remote

import cl.duoc.tiotecpreventivo.data.remote.model.PostDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// #api externa JSONPlaceholder
/**
 * TT-30: API pública externa (https://jsonplaceholder.typicode.com). Solo lectura.
 * No es un microservicio de TíoTec: no guarda nada nuestro ni lo usan las pantallas de solicitudes.
 */
interface PostsApi {

    // #api externa: lista de posts
    /** Lista de publicaciones. `_limit` es un parámetro propio de JSONPlaceholder para acotar el resultado. */
    @GET("posts")
    suspend fun posts(@Query("_limit") limite: Int? = null): List<PostDto>

    // #api externa: un post por id
    @GET("posts/{id}")
    suspend fun post(@Path("id") id: Int): PostDto
}
