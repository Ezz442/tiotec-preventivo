package cl.duoc.tiotecpreventivo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.tiotecpreventivo.repository.PreferenciasRepository
import cl.duoc.tiotecpreventivo.data.preferences.Tema
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PreferenciasUiState(
    val tema: Tema = Tema.SISTEMA,
    val avisosActivos: Boolean = true,
    val primeraEjecucion: Boolean = true,
    val cargando: Boolean = true
)

/**
 * Expone las preferencias de DataStore como un único StateFlow (TT-04, sección 8).
 * La pantalla lo observa con `collectAsState()` y envía eventos; nunca toca DataStore.
 */
class PreferenciasViewModel(
    private val repositorio: PreferenciasRepository
) : ViewModel() {

    val uiState: StateFlow<PreferenciasUiState> = repositorio.preferencias
        .map { preferencias ->
            PreferenciasUiState(
                tema = preferencias.tema,
                avisosActivos = preferencias.avisosActivos,
                primeraEjecucion = preferencias.primeraEjecucion,
                cargando = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PreferenciasUiState()
        )

    fun onTemaChange(tema: Tema) {
        viewModelScope.launch { repositorio.setTema(tema) }
    }

    fun onAvisosChange(activos: Boolean) {
        viewModelScope.launch { repositorio.setAvisosActivos(activos) }
    }

    /** Se llama al cerrar la bienvenida, para no volver a mostrarla. */
    fun onBienvenidaVista() {
        viewModelScope.launch { repositorio.marcarPrimeraEjecucionCompleta() }
    }

    class Factory(
        private val repositorio: PreferenciasRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PreferenciasViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PreferenciasViewModel(repositorio) as T
            }
            throw IllegalArgumentException("ViewModel no compatible")
        }
    }
}
