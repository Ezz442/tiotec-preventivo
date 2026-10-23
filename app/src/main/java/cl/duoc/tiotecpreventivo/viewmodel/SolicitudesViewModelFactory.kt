package cl.duoc.tiotecpreventivo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.duoc.tiotecpreventivo.repository.PerfilRepository
import cl.duoc.tiotecpreventivo.repository.SolicitudRepository

class SolicitudesViewModelFactory(
    private val solicitudRepository: SolicitudRepository,
    private val perfilRepository: PerfilRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SolicitudesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return SolicitudesViewModel(
                solicitudRepository, perfilRepository
            ) as T
        }

        throw IllegalArgumentException("ViewModel desconocido")
    }
}