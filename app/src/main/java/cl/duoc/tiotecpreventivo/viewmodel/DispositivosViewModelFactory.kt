package cl.duoc.tiotecpreventivo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.duoc.tiotecpreventivo.repository.DispositivoRepository
import cl.duoc.tiotecpreventivo.repository.PerfilRepository

class DispositivosViewModelFactory(
    private val dispositivoRepository: DispositivoRepository,
    private val perfilRepository: PerfilRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DispositivosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DispositivosViewModel(
                dispositivoRepository = dispositivoRepository,
                perfilRepository = perfilRepository
            ) as T
        }

        throw IllegalArgumentException("ViewModel no compatible")
    }
}