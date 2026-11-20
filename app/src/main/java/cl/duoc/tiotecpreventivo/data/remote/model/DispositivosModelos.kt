package cl.duoc.tiotecpreventivo.data.remote.model

/** Cuerpos JSON de dispositivos-service (TT-28). La foto del dispositivo no viaja: es local. */
data class DispositivoDto(
    val id: Long,
    val nombre: String,
    val sistemaOperativo: String,
    val archivado: Boolean,
    val creadoEn: Long,
    val actualizadoEn: Long
)

/** Cuerpo de POST y PUT /api/dispositivos. */
data class DispositivoPeticion(
    val nombre: String,
    val sistemaOperativo: String
)

/** Cuerpo de PATCH /api/dispositivos/{id}/archivado. */
data class ArchivadoPeticion(
    val archivado: Boolean
)
