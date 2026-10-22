package cl.duoc.tiotecpreventivo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tiotecpreventivo.repository.CatalogoRepository
import cl.duoc.tiotecpreventivo.repository.DispositivoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import cl.duoc.tiotecpreventivo.data.catalogo.ValorRespuesta
import cl.duoc.tiotecpreventivo.domain.ReglasRiesgo
import cl.duoc.tiotecpreventivo.domain.ResultadoReglas
import cl.duoc.tiotecpreventivo.repository.EvaluacionRepository

class EvaluacionViewModel(
    private val dispositivoId: Long,
    private val catalogoRepository: CatalogoRepository,
    private val dispositivoRepository: DispositivoRepository,
    private val evaluacionRepository: EvaluacionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EvaluacionUiState())
    val uiState = _uiState.asStateFlow()
    private val reglas = ReglasRiesgo(catalogoRepository)

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        _uiState.update {
            it.copy(cargando = true, errorCarga = null)
        }

        viewModelScope.launch {
            try {
                val dispositivo = dispositivoRepository.obtener(dispositivoId)

                if (dispositivo == null || dispositivo.archivado) {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            errorCarga = "El dispositivo no está disponible para evaluar."
                        )
                    }
                    return@launch
                }

                val preguntas = catalogoRepository.preguntas()

                if (preguntas.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            cargando = false, errorCarga = "No hay preguntas disponibles."
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        dispositivo = dispositivo,
                        preguntas = preguntas,
                        cargando = false,
                        errorCarga = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false, errorCarga = "No pudimos cargar la evaluación."
                    )
                }
            }
        }
    }

    fun reintentarCarga() {
        val estado = _uiState.value

        if (estado.cargando || estado.guardando || estado.errorCarga == null) return

        cargarDatos()
    }

    fun onRespuestaChange(respuesta: ValorRespuesta) {
        val estado = _uiState.value

        if (estado.cargando || estado.errorCarga != null || estado.guardando || estado.evaluacionGuardadaId != null) return

        val pregunta = estado.preguntas.getOrNull(estado.indiceActual) ?: return

        _uiState.update {
            it.copy(
                respuestas = it.respuestas + (pregunta.id to respuesta),
                errorPregunta = null,
                errorGuardado = null
            )
        }
    }

    fun siguiente() {
        val estado = _uiState.value

        if (estado.cargando || estado.errorCarga != null || estado.guardando || estado.evaluacionGuardadaId != null) return

        val pregunta = estado.preguntas.getOrNull(estado.indiceActual) ?: return

        if (pregunta.id !in estado.respuestas) {
            _uiState.update {
                it.copy(errorPregunta = "Selecciona una respuesta para continuar.")
            }
            return
        }

        if (estado.indiceActual < estado.preguntas.lastIndex) {
            _uiState.update {
                it.copy(
                    indiceActual = it.indiceActual + 1, errorPregunta = null
                )
            }
        }
    }

    fun anterior() {
        val estado = _uiState.value

        if (estado.cargando || estado.errorCarga != null || estado.guardando || estado.evaluacionGuardadaId != null || estado.indiceActual == 0) return

        _uiState.update {
            it.copy(
                indiceActual = it.indiceActual - 1, errorPregunta = null
            )
        }
    }

    fun prepararRevision(): Boolean {
        val estado = _uiState.value

        if (estado.cargando || estado.errorCarga != null || estado.guardando || estado.evaluacionGuardadaId != null || estado.preguntas.isEmpty()) return false

        val indicePendiente = estado.preguntas.indexOfFirst {
            it.id !in estado.respuestas
        }

        if (indicePendiente != -1) {
            _uiState.update {
                it.copy(
                    indiceActual = indicePendiente,
                    errorPregunta = "Responde esta pregunta antes de revisar."
                )
            }
            return false
        }

        _uiState.update {
            it.copy(errorPregunta = null)
        }

        return true
    }

    fun finalizarEvaluacion() {
        if (!prepararRevision()) return

        val estado = _uiState.value

        _uiState.update {
            it.copy(
                guardando = true, errorGuardado = null
            )
        }

        viewModelScope.launch {
            try {
                when (val resultado = reglas.evaluar(estado.respuestas)) {
                    is ResultadoReglas.Incompleta -> {
                        _uiState.update {
                            it.copy(
                                errorGuardado = "Faltan respuestas. Revisa la evaluación."
                            )
                        }
                    }

                    is ResultadoReglas.Completa -> {
                        val id = evaluacionRepository.guardarEvaluacion(
                            dispositivoId = dispositivoId,
                            respuestas = estado.respuestas,
                            resultados = resultado.resultados
                        )

                        _uiState.update {
                            it.copy(
                                evaluacionGuardadaId = id, mostrarConfirmarSalida = false
                            )
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorGuardado = "No pudimos guardar la evaluación. Puedes reintentar."
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(guardando = false)
                }
            }
        }
    }

    fun solicitarSalida(): Boolean {
        val estado = _uiState.value

        if (estado.guardando) return false

        if (estado.respuestas.isEmpty() || estado.evaluacionGuardadaId != null) return true

        _uiState.update {
            it.copy(mostrarConfirmarSalida = true)
        }

        return false
    }

    fun cancelarSalida() {
        _uiState.update {
            it.copy(mostrarConfirmarSalida = false)
        }
    }

    fun confirmarSalida(): Boolean {
        if (_uiState.value.guardando) return false

        _uiState.update {
            it.copy(mostrarConfirmarSalida = false)
        }

        return true
    }

    fun irAPregunta(indice: Int): Boolean {
        val estado = _uiState.value

        if (estado.cargando || estado.guardando || estado.errorCarga != null || estado.evaluacionGuardadaId != null || indice !in estado.preguntas.indices) return false

        _uiState.update {
            it.copy(
                indiceActual = indice, errorPregunta = null, errorGuardado = null
            )
        }

        return true
    }
}