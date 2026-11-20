package cl.duoc.tiotecpreventivo.data.model

/** Modelo de pantalla para un texto de la API externa (TT-30). No se guarda en Room. */
data class ConsejoComunidad(
    val id: Int,
    val titulo: String,
    val texto: String
)
