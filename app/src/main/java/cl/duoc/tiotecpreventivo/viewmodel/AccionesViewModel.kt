package cl.duoc.tiotecpreventivo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoAccion
import cl.duoc.tiotecpreventivo.repository.AccionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccionesViewModel(
    private val repository: AccionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccionesUiState())
    val uiState = _uiState.asStateFlow()

    private var observacion: Job? = null

    init {
        observarAcciones()
    }

    private fun observarAcciones() {
        observacion?.cancel()
        val filtro = _uiState.value.filtro

        _uiState.update {
            it.copy(cargando = true, errorCarga = null)
        }

        observacion = viewModelScope.launch {
            try {
                combine(
                    repository.observarPorEstado(filtro), repository.contarPendientes()
                ) { acciones, pendientes ->
                    acciones to pendientes
                }.collect { (acciones, pendientes) ->
                    _uiState.update {
                        it.copy(
                            acciones = acciones,
                            pendientes = pendientes,
                            cargando = false,
                            errorCarga = null
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false, errorCarga = "No pudimos cargar las acciones."
                    )
                }
            }
        }
    }

    fun cambiarFiltro(filtro: EstadoAccion) {
        val estado = _uiState.value
        if (estado.filtro == filtro || estado.actualizandoId != null) return

        _uiState.update {
            it.copy(filtro = filtro, acciones = emptyList(), errorOperacion = null)
        }
        observarAcciones()
    }

    fun reintentarCarga() {
        if (_uiState.value.cargando) return
        observarAcciones()
    }

    fun cambiarEstado(id: Long) {
        val estado = _uiState.value
        if (estado.cargando || estado.errorCarga != null || estado.actualizandoId != null) return

        val accion = estado.acciones.find { it.id == id } ?: return
        val nuevoEstado = if (accion.estado == EstadoAccion.PENDIENTE) {
            EstadoAccion.COMPLETADA
        } else {
            EstadoAccion.PENDIENTE
        }

        _uiState.update {
            it.copy(actualizandoId = id, errorOperacion = null)
        }

        viewModelScope.launch {
            try {
                val actualizado = repository.cambiarEstado(id, nuevoEstado)
                if (!actualizado) {
                    _uiState.update {
                        it.copy(errorOperacion = "La acción ya no está disponible.")
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorOperacion = "No pudimos actualizar la acción. Reintenta.")
                }
            } finally {
                _uiState.update { it.copy(actualizandoId = null) }
            }
        }
    }
}