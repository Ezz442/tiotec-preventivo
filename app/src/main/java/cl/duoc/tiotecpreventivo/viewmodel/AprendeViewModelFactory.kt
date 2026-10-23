package cl.duoc.tiotecpreventivo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.duoc.tiotecpreventivo.repository.CatalogoRepository

class AprendeViewModelFactory(
    private val repository: CatalogoRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AprendeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return AprendeViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}