package cl.duoc.tiotecpreventivo.viewmodel

import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.data.local.entity.SolicitudEntity

data class SolicitudesUiState(
    val perfilId: Long? = null,
    val solicitudes: List<SolicitudEntity> = emptyList(),
    val mostrarFormulario: Boolean = false,
    val categoriaSeleccionada: Categoria? = null,
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val errorCarga: String? = null,
    val errorCategoria: String? = null,
    val errorOperacion: String? = null,
    val mensaje: String? = null
)