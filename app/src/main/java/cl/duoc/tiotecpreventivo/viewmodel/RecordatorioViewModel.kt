package cl.duoc.tiotecpreventivo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.tiotecpreventivo.data.local.entity.RecordatorioEntity
import cl.duoc.tiotecpreventivo.data.local.entity.TipoRecordatorio
import cl.duoc.tiotecpreventivo.domain.ValidadorRecordatorio
import cl.duoc.tiotecpreventivo.repository.GestorRecordatorios
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Formulario abierto para crear o editar el recordatorio de una acción (TT-16). */
data class FormularioRecordatorio(
    val accionId: Long,
    val titulo: String,
    /** null al crear; id del recordatorio al editar. */
    val recordatorioId: Long? = null,
    /** Medianoche UTC del día elegido, como lo entrega el DatePicker de Material 3. */
    val fechaUtc: Long? = null,
    val hora: Int? = null,
    val minuto: Int? = null,
    val errorFecha: String? = null,
    val errorHora: String? = null,
    /** TT-26: AlertDialog "¿Cancelar este recordatorio?" antes de borrarlo. */
    val confirmandoCancelacion: Boolean = false
)

data class RecordatoriosUiState(
    /** accionId → su recordatorio programado. */
    val porAccion: Map<Long, RecordatorioEntity> = emptyMap(),
    val formulario: FormularioRecordatorio? = null,
    val guardando: Boolean = false,
    val errorOperacion: String? = null,
    /** Mensaje para un Snackbar; se limpia con [RecordatorioViewModel.consumirMensaje]. */
    val mensaje: String? = null
)

/**
 * Recordatorios de acciones (TT-16). Valida la fecha y hora aquí, no en la pantalla,
 * y usa [GestorRecordatorios] para guardar en Room y programar el aviso.
 */
class RecordatorioViewModel(
    private val gestor: GestorRecordatorios,
    private val reloj: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordatoriosUiState())
    val uiState: StateFlow<RecordatoriosUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                gestor.observarActivos().collect { activos ->
                    _uiState.update { actual ->
                        actual.copy(
                            porAccion = activos
                                .filter { it.tipo == TipoRecordatorio.ACCION && it.accionId != null }
                                .associateBy { it.accionId!! }
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorOperacion = "No pudimos cargar tus recordatorios.") }
            }
        }
    }

    fun abrirNuevo(accionId: Long, tituloAccion: String) {
        if (_uiState.value.guardando) return
        val existente = _uiState.value.porAccion[accionId]
        if (existente != null) {
            editar(existente)
            return
        }
        _uiState.update {
            it.copy(
                formulario = FormularioRecordatorio(accionId = accionId, titulo = tituloAccion),
                errorOperacion = null
            )
        }
    }

    fun editar(recordatorio: RecordatorioEntity) {
        if (_uiState.value.guardando) return
        val accionId = recordatorio.accionId ?: return
        val (fechaUtc, hora, minuto) = ValidadorRecordatorio.separar(recordatorio.fechaHora)
        _uiState.update {
            it.copy(
                formulario = FormularioRecordatorio(
                    accionId = accionId,
                    titulo = recordatorio.titulo,
                    recordatorioId = recordatorio.id,
                    fechaUtc = fechaUtc,
                    hora = hora,
                    minuto = minuto
                ),
                errorOperacion = null
            )
        }
    }

    fun onFechaChange(fechaUtc: Long) = actualizarFormulario {
        it.copy(fechaUtc = fechaUtc, errorFecha = null)
    }

    fun onHoraChange(hora: Int, minuto: Int) = actualizarFormulario {
        it.copy(hora = hora, minuto = minuto, errorHora = null)
    }

    fun cerrarFormulario() {
        if (_uiState.value.guardando) return
        _uiState.update { it.copy(formulario = null, errorOperacion = null) }
    }

    /**
     * @param puedeNotificar si el teléfono permite mostrar avisos. Si no, el recordatorio
     *   se guarda igual y se avisa al usuario cómo activarlos, sin bloquear la app.
     */
    fun guardar(puedeNotificar: Boolean) {
        val estado = _uiState.value
        val formulario = estado.formulario ?: return
        if (estado.guardando) return

        val validacion = ValidadorRecordatorio.validar(
            formulario.fechaUtc, formulario.hora, formulario.minuto, reloj()
        )
        if (!validacion.esValido) {
            actualizarFormulario {
                it.copy(errorFecha = validacion.errorFecha, errorHora = validacion.errorHora)
            }
            return
        }

        _uiState.update { it.copy(guardando = true, errorOperacion = null) }
        viewModelScope.launch {
            try {
                val fechaHora = validacion.fechaHora!!
                val exito = if (formulario.recordatorioId == null) {
                    gestor.crearParaAccion(formulario.accionId, formulario.titulo, fechaHora)
                    true
                } else {
                    gestor.reprogramar(formulario.recordatorioId, formulario.titulo, fechaHora)
                }

                _uiState.update {
                    if (!exito) {
                        it.copy(errorOperacion = "El recordatorio ya no está disponible.")
                    } else {
                        it.copy(
                            formulario = null,
                            mensaje = when {
                                !puedeNotificar ->
                                    "Recordatorio guardado. Activa las notificaciones en Ajustes del teléfono para recibir el aviso."
                                formulario.recordatorioId == null -> "Recordatorio programado."
                                else -> "Recordatorio reprogramado."
                            }
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorOperacion = "No pudimos guardar el recordatorio. Inténtalo nuevamente.")
                }
            } finally {
                _uiState.update { it.copy(guardando = false) }
            }
        }
    }

    /** TT-26: cancelar un recordatorio programado pide confirmación primero. */
    fun pedirCancelacion() = actualizarFormulario {
        if (it.recordatorioId == null) it else it.copy(confirmandoCancelacion = true)
    }

    fun volverDeCancelacion() = actualizarFormulario { it.copy(confirmandoCancelacion = false) }

    fun cancelarRecordatorio(recordatorioId: Long) {
        if (_uiState.value.guardando) return
        _uiState.update { it.copy(guardando = true, errorOperacion = null) }
        viewModelScope.launch {
            try {
                gestor.cancelar(recordatorioId)
                _uiState.update { it.copy(formulario = null, mensaje = "Recordatorio cancelado.") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorOperacion = "No pudimos cancelar el recordatorio.",
                        formulario = it.formulario?.copy(confirmandoCancelacion = false)
                    )
                }
            } finally {
                _uiState.update { it.copy(guardando = false) }
            }
        }
    }

    fun consumirMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    private fun actualizarFormulario(cambio: (FormularioRecordatorio) -> FormularioRecordatorio) {
        if (_uiState.value.guardando) return
        _uiState.update { estado -> estado.formulario?.let { estado.copy(formulario = cambio(it)) } ?: estado }
    }
}

class RecordatorioViewModelFactory(
    private val gestor: GestorRecordatorios
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordatorioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecordatorioViewModel(gestor) as T
        }
        throw IllegalArgumentException("ViewModel no compatible")
    }
}
