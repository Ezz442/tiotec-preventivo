package cl.duoc.tiotecpreventivo.viewmodel

import cl.duoc.tiotecpreventivo.data.local.entity.EstadoSolicitud
import cl.duoc.tiotecpreventivo.data.local.entity.SolicitudConHistorial

data class SolicitudDetalleUiState(
    val detalle: SolicitudConHistorial? = null,
    val cargando: Boolean = true,
    val actualizando: Boolean = false,
    val estadoPorConfirmar: EstadoSolicitud? = null,
    val errorCarga: String? = null,
    val errorOperacion: String? = null,
    val mensaje: String? = null
)