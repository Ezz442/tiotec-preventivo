package cl.duoc.tiotecpreventivo.viewmodel

import cl.duoc.tiotecpreventivo.data.local.entity.ExperienciaDigital

data class PerfilUiState(
    val alias: String = "",
    val experienciaDigital: ExperienciaDigital? = null,
    val errorAlias: String? = null,
    val errorExperiencia: String? = null,
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val errorGeneral: String? = null,
    val mensaje: String? = null,
    val perfilCargado: Boolean = false,
)