package cl.duoc.tiotecpreventivo.viewmodel

import cl.duoc.tiotecpreventivo.data.local.entity.DispositivoEntity
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo

data class DispositivosUiState(
    val dispositivos: List<DispositivoEntity> = emptyList(),
    val perfilId: Long? = null,
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    val formularioVisible: Boolean = false,
    val dispositivoEnEdicionId: Long? = null,
    val nombre: String = "",
    val sistemaOperativo: SistemaOperativo? = null,
    val errorNombre: String? = null,
    val errorSistemaOperativo: String? = null,
    val guardando: Boolean = false,
    val dispositivoAEliminar: DispositivoEntity? = null,
    val eliminando: Boolean = false,
    val errorOperacion: String? = null,
    val mensaje: String? = null
)