package cl.duoc.tiotecpreventivo.data.remote.model

/** Formato de error de los dos microservicios. `campos` solo viene en errores 400 de validación. */
data class ErrorApiDto(
    val status: Int?,
    val mensaje: String?,
    val campos: Map<String, String>?
)
