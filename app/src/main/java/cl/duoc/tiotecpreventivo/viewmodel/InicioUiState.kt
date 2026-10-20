package cl.duoc.tiotecpreventivo.viewmodel

data class InicioUiState(
    val alias: String? = null,
    val accionesPendientes: Int = 0,
    val cargando: Boolean = true,
    val error: String? = null
)