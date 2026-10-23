package cl.duoc.tiotecpreventivo.viewmodel

import androidx.lifecycle.ViewModel
import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.repository.CatalogoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AprendeViewModel(
    private val repository: CatalogoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AprendeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarContenidos()
    }

    private fun cargarContenidos() {
        val categoria = _uiState.value.categoriaSeleccionada
        _uiState.update { it.copy(cargando = true, error = null) }

        try {
            val categorias = repository.categorias()
            val contenidos = repository.contenidos(categoria)

            _uiState.update {
                it.copy(
                    categorias = categorias, contenidos = contenidos, cargando = false
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    cargando = false, error = "No pudimos cargar los contenidos."
                )
            }
        }
    }

    fun seleccionarCategoria(categoria: Categoria?) {
        if (_uiState.value.categoriaSeleccionada == categoria) return

        _uiState.update {
            it.copy(
                categoriaSeleccionada = categoria, contenidos = emptyList()
            )
        }
        cargarContenidos()
    }

    fun reintentar() {
        cargarContenidos()
    }
}