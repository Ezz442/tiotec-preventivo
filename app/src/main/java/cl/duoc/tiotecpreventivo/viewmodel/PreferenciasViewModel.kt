package cl.duoc.tiotecpreventivo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.tiotecpreventivo.data.preferences.Tema
import cl.duoc.tiotecpreventivo.repository.PreferenciasRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PreferenciasUiState(
    val tema: Tema = Tema.SISTEMA,
    val avisosActivos: Boolean = true,
    val primeraEjecucion: Boolean = true,
    val cargando: Boolean = true,
    /** Mensaje para el Snackbar cuando algo no se pudo guardar; null si no hay nada que mostrar. */
    val mensaje: String? = null
)

/**
 * Expone las preferencias de DataStore como un único StateFlow (TT-04, sección 8).
 * La pantalla lo observa con `collectAsState()` y envía eventos; nunca toca DataStore.
 *
 * Si una escritura falla, DataStore no cambia el archivo: el Flow sigue emitiendo el
 * valor anterior, así que la pantalla mantiene la configuración previa y solo se
 * muestra un mensaje. La app sigue funcionando.
 */
class PreferenciasViewModel(
    private val repositorio: PreferenciasRepository
) : ViewModel() {

    private val mensaje = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PreferenciasUiState> =
        combine(repositorio.preferencias, mensaje) { preferencias, mensajeActual ->
            PreferenciasUiState(
                tema = preferencias.tema,
                avisosActivos = preferencias.avisosActivos,
                primeraEjecucion = preferencias.primeraEjecucion,
                cargando = false,
                mensaje = mensajeActual
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PreferenciasUiState()
        )

    fun onTemaChange(tema: Tema) = guardar(ERROR_TEMA) { repositorio.setTema(tema) }

    fun onAvisosChange(activos: Boolean) = guardar(ERROR_AVISOS) { repositorio.setAvisosActivos(activos) }

    /** Se llama al cerrar la bienvenida, para no volver a mostrarla. */
    fun onBienvenidaVista() = guardar(ERROR_BIENVENIDA) { repositorio.marcarPrimeraEjecucionCompleta() }

    /** La pantalla lo llama después de mostrar el Snackbar. */
    fun onMensajeMostrado() {
        mensaje.value = null
    }

    private fun guardar(mensajeError: String, accion: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                accion()
            } catch (e: CancellationException) {
                throw e // la pantalla se cerró: no es un error de guardado
            } catch (e: Exception) {
                mensaje.update { mensajeError }
            }
        }
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

    companion object {
        const val ERROR_TEMA = "No pudimos guardar el tema. Se mantiene el anterior."
        const val ERROR_AVISOS = "No pudimos guardar los recordatorios. Se mantiene la configuración anterior."
        const val ERROR_BIENVENIDA = "No pudimos guardar tu avance. Volverás a ver la bienvenida."
    }
}
