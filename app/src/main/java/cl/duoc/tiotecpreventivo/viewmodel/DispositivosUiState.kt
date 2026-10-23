package cl.duoc.tiotecpreventivo.viewmodel

import cl.duoc.tiotecpreventivo.data.local.entity.DispositivoEntity
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo

/** TT-18: foto en curso. La app de cámara escribe en `rutaTemporal` y se confirma al volver. */
data class CapturaFoto(
    val dispositivoId: Long,
    val rutaTemporal: String
)

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
    val mensaje: String? = null,
    /** TT-18: foto que la cámara está tomando ahora. */
    val capturaFoto: CapturaFoto? = null,
    /** TT-18: dispositivo cuya foto se pidió quitar (diálogo de confirmación). */
    val dispositivoQuitarFoto: DispositivoEntity? = null,
    val procesandoFoto: Boolean = false
)