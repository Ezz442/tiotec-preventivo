package cl.duoc.tiotecpreventivo.viewmodel

import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.data.catalogo.Contenido

data class AprendeUiState(
    val categorias: List<Categoria> = emptyList(),
    val categoriaSeleccionada: Categoria? = null,
    val contenidos: List<Contenido> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null
)