package cl.duoc.tiotecpreventivo.viewmodel

import cl.duoc.tiotecpreventivo.data.model.ConsejoComunidad

/** Estado de la pantalla Comunidad (TT-30): cargando, error, lista o lista vacía. */
data class PostsUiState(
    val cargando: Boolean = true,
    val consejos: List<ConsejoComunidad> = emptyList(),
    val error: String? = null
) {
    val vacio: Boolean get() = !cargando && error == null && consejos.isEmpty()
}
