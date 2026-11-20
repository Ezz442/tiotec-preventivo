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
    /** TT-29: se está pidiendo la lista actualizada a solicitudes-service. */
    val sincronizando: Boolean = false,
    /** TT-29: el servicio no respondió; se muestran los datos guardados en el teléfono. */
    val errorSincronizacion: String? = null,
    val errorCarga: String? = null,
    val errorCategoria: String? = null,
    val errorOperacion: String? = null,
    val mensaje: String? = null
)