package cl.duoc.tiotecpreventivo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tiotecpreventivo.data.local.entity.ExperienciaDigital
import cl.duoc.tiotecpreventivo.domain.ValidadorFormularios
import cl.duoc.tiotecpreventivo.repository.PerfilRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PerfilViewModel(
    private val repository: PerfilRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarPerfil()

    }

    private fun cargarPerfil() {
        viewModelScope.launch {
            try {
                val perfil = repository.observarPerfil().first()

                _uiState.update {
                    it.copy(
                        alias = perfil?.alias.orEmpty(),
                        experienciaDigital = perfil?.experienciaDigital,
                        perfilCargado = true,
                        cargando = false,
                        errorGeneral = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false, errorGeneral = "No pudimos cargar tu perfil."
                    )
                }
            }
        }
    }

    /** El Snackbar ya mostró el mensaje. */
    fun consumirMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    fun reintentarCarga() {
        if (_uiState.value.cargando || _uiState.value.guardando) return

        _uiState.update {
            it.copy(cargando = true, errorGeneral = null)
        }
        cargarPerfil()
    }

    fun onAliasChange(alias: String) {
        if (_uiState.value.cargando || _uiState.value.guardando) return

        _uiState.update {
            it.copy(
                alias = alias, errorAlias = null, mensaje = null
            )
        }
    }

    fun onExperienciaChange(experiencia: ExperienciaDigital) {
        if (_uiState.value.cargando || _uiState.value.guardando) return

        _uiState.update {
            it.copy(
                experienciaDigital = experiencia, errorExperiencia = null, mensaje = null
            )
        }
    }

    fun guardarPerfil() {
        val estado = _uiState.value

        if (estado.cargando || estado.guardando) return

        // TT-26: las reglas viven en ValidadorFormularios; aquí solo se copian los errores.
        val validacion = ValidadorFormularios.validarPerfil(estado.alias, estado.experienciaDigital)
        val aliasLimpio = validacion.aliasLimpio

        _uiState.update {
            it.copy(
                errorAlias = validacion.errorAlias,
                errorExperiencia = validacion.errorExperiencia,
                mensaje = null
            )
        }

        val experiencia = estado.experienciaDigital
        if (!validacion.valida || experiencia == null) return

        _uiState.update {
            it.copy(guardando = true, errorGeneral = null)
        }

        viewModelScope.launch {
            try {
                repository.guardar(
                    alias = aliasLimpio, experiencia = experiencia
                )

                _uiState.update {
                    it.copy(
                        alias = aliasLimpio, mensaje = "Perfil guardado correctamente."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorGeneral = "No pudimos guardar tu perfil. Inténtalo nuevamente."
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(guardando = false)
                }
            }
        }
    }
}