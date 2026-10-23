package cl.duoc.tiotecpreventivo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.repository.PerfilRepository
import cl.duoc.tiotecpreventivo.repository.SolicitudRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SolicitudesViewModel(
    private val solicitudRepository: SolicitudRepository,
    private val perfilRepository: PerfilRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SolicitudesUiState())
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
                combine(
                    perfilRepository.observarPerfil(), solicitudRepository.observarTodas()
                ) { perfil, solicitudes ->
                    perfil?.id to solicitudes
                }.collect { (perfilId, solicitudes) ->
                    _uiState.update {
                        it.copy(
                            perfilId = perfilId,
                            solicitudes = solicitudes,
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
                        cargando = false, errorCarga = "No pudimos cargar las solicitudes."
                    )
                }
            }
        }
    }

    fun abrirFormulario() {
        val estado = _uiState.value
        if (estado.cargando || estado.guardando) return

        if (estado.perfilId == null) {
            _uiState.update {
                it.copy(errorOperacion = "Crea tu perfil antes de solicitar asistencia.")
            }
            return
        }

        _uiState.update {
            it.copy(
                mostrarFormulario = true,
                categoriaSeleccionada = null,
                errorCategoria = null,
                errorOperacion = null,
                mensaje = null
            )
        }
    }

    fun cerrarFormulario() {
        if (_uiState.value.guardando) return

        _uiState.update {
            it.copy(
                mostrarFormulario = false,
                categoriaSeleccionada = null,
                errorCategoria = null,
                errorOperacion = null
            )
        }
    }

    fun seleccionarCategoria(categoria: Categoria) {
        if (_uiState.value.guardando) return

        _uiState.update {
            it.copy(
                categoriaSeleccionada = categoria, errorCategoria = null
            )
        }
    }

    fun guardar() {
        val estado = _uiState.value
        if (estado.guardando || !estado.mostrarFormulario) return

        val perfilId = estado.perfilId
        if (perfilId == null) {
            _uiState.update {
                it.copy(errorOperacion = "Crea tu perfil antes de solicitar asistencia.")
            }
            return
        }

        val categoria = estado.categoriaSeleccionada
        if (categoria == null) {
            _uiState.update {
                it.copy(errorCategoria = "Selecciona una categoría.")
            }
            return
        }

        _uiState.update {
            it.copy(guardando = true, errorOperacion = null, mensaje = null)
        }

        viewModelScope.launch {
            try {
                solicitudRepository.crear(perfilId, categoria)

                _uiState.update {
                    it.copy(
                        mostrarFormulario = false,
                        categoriaSeleccionada = null,
                        errorCategoria = null,
                        mensaje = "Solicitud simulada guardada. No se envió a ningún servicio."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorOperacion = "No pudimos guardar. Intenta nuevamente.")
                }
            } finally {
                _uiState.update { it.copy(guardando = false) }
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
}