package cl.duoc.tiotecpreventivo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tiotecpreventivo.repository.DispositivoRepository
import cl.duoc.tiotecpreventivo.repository.PerfilRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo
import cl.duoc.tiotecpreventivo.repository.ResultadoEliminacion

class DispositivosViewModel(
    private val dispositivoRepository: DispositivoRepository,
    private val perfilRepository: PerfilRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DispositivosUiState())
    val uiState = _uiState.asStateFlow()

    private var observacion: Job? = null

    init {
        observarDatos()
    }

    private fun observarDatos() {
        observacion?.cancel()

        _uiState.update {
            it.copy(cargando = true, errorCarga = null)
        }

        observacion = viewModelScope.launch {
            try {
                combine(
                    perfilRepository.observarPerfil(), dispositivoRepository.observarActivos()
                ) { perfil, dispositivos ->
                    perfil?.id to dispositivos
                }.collect { (perfilId, dispositivos) ->
                    _uiState.update {
                        it.copy(
                            perfilId = perfilId,
                            dispositivos = dispositivos,
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
                        cargando = false, errorCarga = "No pudimos cargar los dispositivos."
                    )
                }
            }
        }
    }

    fun reintentarCarga() {
        if (_uiState.value.cargando) return

        observarDatos()
    }

    fun abrirNuevo() {
        val estado = _uiState.value

        if (estado.cargando || estado.errorCarga != null || estado.perfilId == null || estado.guardando || estado.eliminando) return

        _uiState.update {
            it.copy(
                formularioVisible = true,
                dispositivoEnEdicionId = null,
                nombre = "",
                sistemaOperativo = null,
                errorNombre = null,
                errorSistemaOperativo = null,
                errorOperacion = null,
                mensaje = null
            )
        }
    }

    fun editarDispositivo(id: Long) {
        val estado = _uiState.value

        if (estado.cargando || estado.errorCarga != null || estado.perfilId == null || estado.guardando || estado.eliminando) return

        val dispositivo = estado.dispositivos.find { it.id == id } ?: return

        _uiState.update {
            it.copy(
                formularioVisible = true,
                dispositivoEnEdicionId = dispositivo.id,
                nombre = dispositivo.nombre,
                sistemaOperativo = dispositivo.sistemaOperativo,
                errorNombre = null,
                errorSistemaOperativo = null,
                errorOperacion = null,
                mensaje = null
            )
        }
    }

    fun cancelarFormulario() {
        if (_uiState.value.guardando) return

        _uiState.update {
            it.copy(
                formularioVisible = false,
                dispositivoEnEdicionId = null,
                nombre = "",
                sistemaOperativo = null,
                errorNombre = null,
                errorSistemaOperativo = null,
                errorOperacion = null
            )
        }
    }

    fun onNombreChange(nombre: String) {
        if (_uiState.value.guardando) return

        _uiState.update {
            it.copy(
                nombre = nombre, errorNombre = null, errorOperacion = null, mensaje = null
            )
        }
    }

    fun onSistemaOperativoChange(sistema: SistemaOperativo) {
        if (_uiState.value.guardando) return

        _uiState.update {
            it.copy(
                sistemaOperativo = sistema,
                errorSistemaOperativo = null,
                errorOperacion = null,
                mensaje = null
            )
        }
    }

    fun guardarDispositivo() {
        val estado = _uiState.value

        if (estado.cargando || estado.errorCarga != null || estado.guardando || estado.eliminando || !estado.formularioVisible) return

        val perfilId = estado.perfilId ?: return
        val nombreLimpio = estado.nombre.trim()
        val sistema = estado.sistemaOperativo
        val idEnEdicion = estado.dispositivoEnEdicionId

        val errorNombre = when {
            nombreLimpio.isBlank() -> "Ingresa un nombre ficticio."
            nombreLimpio.length !in 2..30 -> "El nombre debe tener entre 2 y 30 caracteres."

            else -> null
        }

        val errorSistema = if (sistema == null) {
            "Selecciona un sistema operativo."
        } else {
            null
        }

        _uiState.update {
            it.copy(
                errorNombre = errorNombre,
                errorSistemaOperativo = errorSistema,
                errorOperacion = null,
                mensaje = null
            )
        }

        if (errorNombre != null || sistema == null) return

        _uiState.update {
            it.copy(guardando = true)
        }

        viewModelScope.launch {
            try {
                val nombreRepetido = dispositivoRepository.existeNombreActivo(
                    nombre = nombreLimpio, excluirId = idEnEdicion ?: 0L
                )

                if (nombreRepetido) {
                    _uiState.update {
                        it.copy(
                            errorNombre = "Ya existe un dispositivo con ese nombre."
                        )
                    }
                    return@launch
                }

                if (idEnEdicion == null) {
                    dispositivoRepository.crear(
                        perfilId = perfilId, nombre = nombreLimpio, sistemaOperativo = sistema
                    )
                } else {
                    val actualizado = dispositivoRepository.actualizar(
                        id = idEnEdicion, nombre = nombreLimpio, sistemaOperativo = sistema
                    )

                    if (!actualizado) {
                        _uiState.update {
                            it.copy(
                                errorOperacion = "El dispositivo ya no está disponible."
                            )
                        }
                        return@launch
                    }
                }

                _uiState.update {
                    it.copy(
                        formularioVisible = false,
                        dispositivoEnEdicionId = null,
                        nombre = "",
                        sistemaOperativo = null,
                        errorNombre = null,
                        errorSistemaOperativo = null,
                        errorOperacion = null,
                        mensaje = if (idEnEdicion == null) {
                            "Dispositivo registrado correctamente."
                        } else {
                            "Dispositivo actualizado correctamente."
                        }
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorOperacion = "No pudimos guardar el dispositivo. Inténtalo nuevamente."
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(guardando = false)
                }
            }
        }
    }

    fun solicitarEliminacion(id: Long) {
        val estado = _uiState.value

        if (estado.cargando || estado.errorCarga != null || estado.guardando || estado.eliminando || estado.formularioVisible) return

        val dispositivo = estado.dispositivos.find { it.id == id } ?: return

        _uiState.update {
            it.copy(
                dispositivoAEliminar = dispositivo, errorOperacion = null, mensaje = null
            )
        }
    }

    fun cancelarEliminacion() {
        if (_uiState.value.eliminando) return

        _uiState.update {
            it.copy(
                dispositivoAEliminar = null, errorOperacion = null
            )
        }
    }

    fun confirmarEliminacion() {
        val estado = _uiState.value

        if (estado.cargando || estado.errorCarga != null || estado.guardando || estado.eliminando || estado.formularioVisible) return

        val dispositivo = estado.dispositivoAEliminar ?: return

        _uiState.update {
            it.copy(
                eliminando = true, errorOperacion = null, mensaje = null
            )
        }

        viewModelScope.launch {
            try {
                val resultado = dispositivoRepository.eliminar(dispositivo.id)

                _uiState.update { actual ->
                    when (resultado) {
                        is ResultadoEliminacion.Eliminado -> actual.copy(
                            dispositivoAEliminar = null,
                            mensaje = "Dispositivo eliminado correctamente."
                        )

                        ResultadoEliminacion.Archivado -> actual.copy(
                            dispositivoAEliminar = null,
                            mensaje = "Dispositivo archivado. Su historial se conserva."
                        )

                        ResultadoEliminacion.NoEncontrado -> actual.copy(
                            dispositivoAEliminar = null,
                            errorOperacion = "El dispositivo ya no está disponible."
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorOperacion = "No pudimos eliminar el dispositivo. Inténtalo nuevamente."
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(eliminando = false)
                }
            }
        }
    }

    fun consumirMensaje() {
        _uiState.update {
            it.copy(mensaje = null)
        }
    }
}