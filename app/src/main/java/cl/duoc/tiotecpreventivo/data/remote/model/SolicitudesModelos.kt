package cl.duoc.tiotecpreventivo.data.remote.model

/**
 * Cuerpos JSON de solicitudes-service (TT-28). Los nombres de los campos son los del JSON.
 * `categoria` y `estado` viajan como texto (RESPALDOS, EN_REVISION…) y se convierten a los
 * enums de la app al guardar, para que un valor desconocido no rompa la lectura completa.
 */
data class SolicitudDto(
    val id: Long,
    val codigo: String?,
    val alias: String,
    val categoria: String,
    val estado: String,
    val creadaEn: Long,
    val actualizadaEn: Long,
    /** Solo viene en el detalle y al crear; el listado no lo incluye. */
    val historial: List<HistorialDto>?
)

data class HistorialDto(
    val id: Long,
    val estado: String,
    val fecha: Long,
    val nota: String
)

/** Cuerpo de POST y PUT /api/solicitudes. */
data class SolicitudPeticion(
    val alias: String,
    val categoria: String
)

/** Cuerpo de PATCH /api/solicitudes/{id}/estado. */
data class CambioEstadoPeticion(
    val estado: String,
    val nota: String? = null
)
