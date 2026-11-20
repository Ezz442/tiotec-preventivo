package cl.duoc.tiotecpreventivo.data.remote.model

/** Un elemento de `GET /posts` de JSONPlaceholder (TT-30). Los nombres coinciden con el JSON. */
data class PostDto(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)
