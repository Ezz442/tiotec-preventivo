package cl.duoc.tiotecpreventivo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.tiotecpreventivo.data.local.entity.EvaluacionEntity
import cl.duoc.tiotecpreventivo.domain.FiltroDispositivo
import cl.duoc.tiotecpreventivo.domain.HistorialLogica
import cl.duoc.tiotecpreventivo.repository.EvaluacionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistorialUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val todas: List<EvaluacionEntity> = emptyList(),
    val filtros: List<FiltroDispositivo> = emptyList(),
    /** null = todos los dispositivos. */
    val dispositivoSeleccionado: Long? = null
) {
    /** Evaluaciones que se ven con el filtro actual, la más reciente primero. */
    val visibles: List<EvaluacionEntity>
        get() = HistorialLogica.filtrar(todas, dispositivoSeleccionado)
}

/**
 * Historial preventivo local (TT-14). Observa Room: una evaluación nueva aparece sola
 * en la lista. Solo lee, nunca escribe.
 */
class HistorialViewModel(
    private val observarHistorial: () -> Flow<List<EvaluacionEntity>>
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialUiState())
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    private var observacion: Job? = null

    init {
        observar()
    }

    private fun observar() {
        observacion?.cancel()
        _uiState.update { it.copy(cargando = true, error = null) }

        observacion = viewModelScope.launch {
            try {
                observarHistorial().collect { evaluaciones ->
                    _uiState.update { actual ->
                        val filtros = HistorialLogica.filtros(evaluaciones)
                        // Si el dispositivo filtrado ya no tiene evaluaciones, se vuelve a "Todos".
                        val seleccionado = actual.dispositivoSeleccionado
                            ?.takeIf { id -> filtros.any { it.dispositivoId == id } }
                        actual.copy(
                            cargando = false,
                            error = null,
                            todas = evaluaciones,
                            filtros = filtros,
                            dispositivoSeleccionado = seleccionado
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = "No pudimos cargar el historial.")
                }
            }
        }
    }

    fun seleccionarDispositivo(dispositivoId: Long?) {
        val estado = _uiState.value
        if (dispositivoId != null && estado.filtros.none { it.dispositivoId == dispositivoId }) return
        _uiState.update { it.copy(dispositivoSeleccionado = dispositivoId) }
    }

    fun reintentar() {
        if (_uiState.value.cargando) return
        observar()
    }
}

class HistorialViewModelFactory(
    private val evaluacionRepository: EvaluacionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistorialViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistorialViewModel(evaluacionRepository::observarHistorial) as T
        }

        throw IllegalArgumentException("ViewModel no compatible")
    }
}
