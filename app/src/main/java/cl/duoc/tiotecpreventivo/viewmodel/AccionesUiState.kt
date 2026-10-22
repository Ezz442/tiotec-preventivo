package cl.duoc.tiotecpreventivo.viewmodel

import cl.duoc.tiotecpreventivo.data.local.entity.AccionEntity
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoAccion

data class AccionesUiState(
    val filtro: EstadoAccion = EstadoAccion.PENDIENTE,
    val acciones: List<AccionEntity> = emptyList(),
    val pendientes: Int = 0,
    val cargando: Boolean = true,
    val actualizandoId: Long? = null,
    val errorCarga: String? = null,
    val errorOperacion: String? = null
)