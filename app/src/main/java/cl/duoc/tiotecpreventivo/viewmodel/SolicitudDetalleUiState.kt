package cl.duoc.tiotecpreventivo.viewmodel

import cl.duoc.tiotecpreventivo.data.local.entity.EstadoSolicitud
import cl.duoc.tiotecpreventivo.data.local.entity.SolicitudConHistorial

data class SolicitudDetalleUiState(
    val detalle: SolicitudConHistorial? = null,
    val cargando: Boolean = true,
    val actualizando: Boolean = false,
    val sincronizando: Boolean = false,
    val errorSincronizacion: String? = null,
    /** TT-29: diálogo "¿Eliminar la solicitud?" abierto. */
    val confirmarEliminar: Boolean = false,
    val eliminando: Boolean = false,
    /** TT-29: la solicitud ya se eliminó; la pantalla vuelve a la lista. */
    val eliminada: Boolean = false,
    val estadoPorConfirmar: EstadoSolicitud? = null,
    val errorCarga: String? = null,
    val errorOperacion: String? = null,
    val mensaje: String? = null
)