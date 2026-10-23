package cl.duoc.tiotecpreventivo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoSolicitud
import cl.duoc.tiotecpreventivo.repository.SolicitudRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SolicitudDetalleViewModel(
    private val solicitudId: Long, private val repository: SolicitudRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SolicitudDetalleUiState())
    val uiState = _uiState.asStateFlow()

    private var cargaJob: Job? = null

    init {
        cargar()
    }

    fun cargar() {
        cargaJob?.cancel()
        _uiState.update { it.copy(cargando = true, errorCarga = null) }

        cargaJob = viewModelScope.launch {
            try {
                repository.observarConHistorial(solicitudId).collect { detalle ->
                    _uiState.update {
                        it.copy(
                            detalle = detalle, cargando = false, errorCarga = if (detalle == null) {
                                "La solicitud no existe."
                            } else {
                                null
                            }
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false, errorCarga = "No pudimos cargar el detalle."
                    )
                }
            }
        }
    }

    fun solicitarCambio(estado: EstadoSolicitud) {
        val actual = _uiState.value
        val solicitud = actual.detalle?.solicitud ?: return

        if (actual.cargando || actual.actualizando || actual.errorCarga != null) return
        if (solicitud.estado == estado) return

        _uiState.update {
            it.copy(
                estadoPorConfirmar = estado, errorOperacion = null, mensaje = null
            )
        }
    }

    fun cancelarCambio() {
        if (_uiState.value.actualizando) return

        _uiState.update { it.copy(estadoPorConfirmar = null) }
    }

    fun confirmarCambio() {
        val actual = _uiState.value
        val estado = actual.estadoPorConfirmar ?: return
        if (actual.actualizando) return

        _uiState.update {
            it.copy(actualizando = true, errorOperacion = null, mensaje = null)
        }

        viewModelScope.launch {
            try {
                val detalleActual = repository.obtenerConHistorial(solicitudId)

                when {
                    detalleActual == null -> {
                        _uiState.update {
                            it.copy(
                                estadoPorConfirmar = null,
                                errorOperacion = "La solicitud ya no existe."
                            )
                        }
                    }

                    detalleActual.solicitud.estado == estado -> {
                        _uiState.update {
                            it.copy(
                                estadoPorConfirmar = null,
                                mensaje = "La solicitud ya tiene ese estado."
                            )
                        }
                    }

                    repository.cambiarEstado(solicitudId, estado) -> {
                        _uiState.update {
                            it.copy(
                                estadoPorConfirmar = null,
                                mensaje = "Estado actualizado en la simulación."
                            )
                        }
                    }

                    else -> {
                        _uiState.update {
                            it.copy(errorOperacion = "No pudimos actualizar la solicitud.")
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorOperacion = "No pudimos cambiar el estado. Reintenta.")
                }
            } finally {
                _uiState.update { it.copy(actualizando = false) }
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
}