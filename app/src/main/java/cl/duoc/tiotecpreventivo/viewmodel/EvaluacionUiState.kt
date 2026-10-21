package cl.duoc.tiotecpreventivo.viewmodel

import cl.duoc.tiotecpreventivo.data.catalogo.Pregunta
import cl.duoc.tiotecpreventivo.data.catalogo.ValorRespuesta
import cl.duoc.tiotecpreventivo.data.local.entity.DispositivoEntity

data class EvaluacionUiState(
    val dispositivo: DispositivoEntity? = null,
    val preguntas: List<Pregunta> = emptyList(),
    val indiceActual: Int = 0,
    val respuestas: Map<String, ValorRespuesta> = emptyMap(),
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val errorCarga: String? = null,
    val errorPregunta: String? = null,
    val errorGuardado: String? = null,
    val mostrarConfirmarSalida: Boolean = false,
    val evaluacionGuardadaId: Long? = null
)