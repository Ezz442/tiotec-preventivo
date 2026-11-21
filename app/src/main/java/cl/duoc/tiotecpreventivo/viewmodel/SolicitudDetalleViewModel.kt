package cl.duoc.tiotecpreventivo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoSolicitud
import cl.duoc.tiotecpreventivo.data.remote.ErrorRemoto
import cl.duoc.tiotecpreventivo.repository.SolicitudRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// #estado y logica del detalle de solicitud
class SolicitudDetalleViewModel(
    private val solicitudId: Long, private val repository: SolicitudRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SolicitudDetalleUiState())
    val uiState = _uiState.asStateFlow()

    private var cargaJob: Job? = null
    private var sincronizacionJob: Job? = null

    init {
        cargar()
    }

    // #carga el detalle e historial
    fun cargar() {
        cargaJob?.cancel()
        _uiState.update { it.copy(cargando = true, errorCarga = null) }

        cargaJob = viewModelScope.launch {
            try {
                repository.observarConHistorial(solicitudId).collect { detalle ->
                    _uiState.update {
                        it.copy(
                            detalle = detalle,
                            cargando = false,
                            // Al eliminarla, Room emite null un instante antes de volver a la lista.
                            errorCarga = if (detalle == null && !it.eliminando && !it.eliminada) {
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

        sincronizar()
    }

    // #pide el detalle al microservicio
    /** TT-29: trae del servicio el detalle con su historial; si falla, queda lo guardado. */
    private fun sincronizar() {
        sincronizacionJob?.cancel()
        _uiState.update { it.copy(sincronizando = true, errorSincronizacion = null) }

        sincronizacionJob = viewModelScope.launch {
            try {
                repository.sincronizarDetalle(solicitudId)
                _uiState.update { it.copy(sincronizando = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ErrorRemoto) {
                _uiState.update { it.copy(sincronizando = false, errorSincronizacion = e.message) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        sincronizando = false,
                        errorSincronizacion = "No pudimos actualizar desde el servicio."
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

    // #actualizar estado de la solicitud en el microservicio
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
                                mensaje = "Estado actualizado en el servicio."
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
            } catch (e: ErrorRemoto) {
                _uiState.update { it.copy(errorOperacion = e.message) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorOperacion = "No pudimos cambiar el estado. Reintenta.")
                }
            } finally {
                _uiState.update { it.copy(actualizando = false) }
            }
        }
    }

    fun solicitarEliminar() {
        val actual = _uiState.value
        if (actual.detalle == null || actual.cargando || actual.actualizando || actual.eliminando) return

        _uiState.update { it.copy(confirmarEliminar = true, errorOperacion = null, mensaje = null) }
    }

    fun cancelarEliminar() {
        if (_uiState.value.eliminando) return

        _uiState.update { it.copy(confirmarEliminar = false, errorOperacion = null) }
    }

    // #eliminar solicitud en el microservicio
    /** TT-29: DELETE en solicitudes-service y, si resulta bien, también de la copia local. */
    fun confirmarEliminar() {
        val actual = _uiState.value
        if (!actual.confirmarEliminar || actual.eliminando) return

        _uiState.update { it.copy(eliminando = true, errorOperacion = null) }

        viewModelScope.launch {
            try {
                repository.eliminar(solicitudId)
                _uiState.update { it.copy(confirmarEliminar = false, eliminada = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ErrorRemoto) {
                _uiState.update { it.copy(errorOperacion = e.message) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorOperacion = "No pudimos eliminar la solicitud. Reintenta.")
                }
            } finally {
                _uiState.update { it.copy(eliminando = false) }
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
}