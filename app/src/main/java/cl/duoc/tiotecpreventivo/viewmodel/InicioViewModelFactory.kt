package cl.duoc.tiotecpreventivo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.duoc.tiotecpreventivo.repository.InicioRepository

class InicioViewModelFactory(
    private val repository: InicioRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InicioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InicioViewModel(repository) as T
        }

        throw IllegalArgumentException("ViewModel no compatible")
    }
}