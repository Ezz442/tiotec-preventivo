package cl.duoc.tiotecpreventivo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tiotecpreventivo.data.remote.ErrorRemoto
import cl.duoc.tiotecpreventivo.repository.PostsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// #estado de la pantalla Comunidad (api externa)
/** TT-30: carga los textos de la API externa y expone el estado para ComunidadScreen. */
class PostsViewModel(private val repository: PostsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PostsUiState())
    val uiState = _uiState.asStateFlow()

    private var cargaJob: Job? = null

    init {
        cargar()
    }

    // #carga los consejos: cargando, exito o error
    /** Se llama al abrir la pantalla y desde el botón Reintentar. */
    fun cargar() {
        cargaJob?.cancel()
        _uiState.update { it.copy(cargando = true, error = null) }

        cargaJob = viewModelScope.launch {
            try {
                val consejos = repository.obtenerConsejos()
                _uiState.update { it.copy(cargando = false, consejos = consejos, error = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ErrorRemoto) {
                _uiState.update { it.copy(cargando = false, error = e.message) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = "No pudimos cargar los consejos de la comunidad.")
                }
            }
        }
    }
}
