package cl.duoc.tiotecpreventivo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.tiotecpreventivo.data.local.entity.EvaluacionConDetalle
import cl.duoc.tiotecpreventivo.domain.ResultadoEvaluacion
import cl.duoc.tiotecpreventivo.domain.ResultadoEvaluacionMapper
import cl.duoc.tiotecpreventivo.repository.CatalogoRepository
import cl.duoc.tiotecpreventivo.repository.EvaluacionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResultadoUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val resultado: ResultadoEvaluacion? = null
)

/**
 * Resultados de una evaluación ya guardada (TT-12).
 * Solo lee desde Room: abrir esta pantalla muchas veces (o desde el historial, TT-14)
 * no crea evaluaciones, resultados ni acciones nuevas.
 *
 * @param cargarDetalle por defecto `EvaluacionRepository.obtenerDetalle`; se puede
 *   reemplazar en pruebas.
 */
class ResultadoViewModel(
    private val evaluacionId: Long,
    private val cargarDetalle: suspend (Long) -> EvaluacionConDetalle?,
    private val mapper: ResultadoEvaluacionMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultadoUiState())
    val uiState: StateFlow<ResultadoUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    private fun cargar() {
        _uiState.update { it.copy(cargando = true, error = null) }

        viewModelScope.launch {
            try {
                val detalle = cargarDetalle(evaluacionId)

                _uiState.update {
                    if (detalle == null) {
                        it.copy(cargando = false, error = "No encontramos esta evaluación.", resultado = null)
                    } else {
                        it.copy(cargando = false, error = null, resultado = mapper.construir(detalle))
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = "No pudimos cargar el resultado.")
                }
            }
        }
    }

    fun reintentar() {
        if (_uiState.value.cargando) return
        cargar()
    }
}

class ResultadoViewModelFactory(
    private val evaluacionId: Long,
    private val evaluacionRepository: EvaluacionRepository,
    private val catalogoRepository: CatalogoRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ResultadoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ResultadoViewModel(
                evaluacionId = evaluacionId,
                cargarDetalle = evaluacionRepository::obtenerDetalle,
                mapper = ResultadoEvaluacionMapper(catalogoRepository)
            ) as T
        }

        throw IllegalArgumentException("ViewModel no compatible")
    }
}
