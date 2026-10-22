package cl.duoc.tiotecpreventivo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.duoc.tiotecpreventivo.repository.AccionRepository

class AccionesViewModelFactory(
    private val repository: AccionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccionesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AccionesViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}