package cl.duoc.tiotecpreventivo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.duoc.tiotecpreventivo.repository.SolicitudRepository

class SolicitudDetalleViewModelFactory(
    private val solicitudId: Long, private val repository: SolicitudRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SolicitudDetalleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return SolicitudDetalleViewModel(
                solicitudId, repository
            ) as T
        }

        throw IllegalArgumentException("ViewModel desconocido")
    }
}