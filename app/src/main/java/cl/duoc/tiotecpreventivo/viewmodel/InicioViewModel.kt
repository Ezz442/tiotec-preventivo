package cl.duoc.tiotecpreventivo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tiotecpreventivo.repository.InicioRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class InicioViewModel(
    repository: InicioRepository
) : ViewModel() {

    val uiState: StateFlow<InicioUiState> = combine(
        repository.observarAlias(),
        repository.observarAccionesPendientes()
    ) { alias, accionesPendientes ->
        InicioUiState(
            alias = alias,
            accionesPendientes = accionesPendientes,
            cargando = false
        )
    }.catch {
        emit(
            InicioUiState(
                cargando = false,
                error = "No pudimos cargar la información de inicio."
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InicioUiState()
    )
}