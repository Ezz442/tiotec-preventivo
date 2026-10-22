package cl.duoc.tiotecpreventivo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.duoc.tiotecpreventivo.repository.CatalogoRepository
import cl.duoc.tiotecpreventivo.repository.DispositivoRepository
import cl.duoc.tiotecpreventivo.repository.EvaluacionRepository

class EvaluacionViewModelFactory(
    private val dispositivoId: Long,
    private val catalogoRepository: CatalogoRepository,
    private val dispositivoRepository: DispositivoRepository,
    private val evaluacionRepository: EvaluacionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EvaluacionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return EvaluacionViewModel(
                dispositivoId = dispositivoId,
                catalogoRepository = catalogoRepository,
                dispositivoRepository = dispositivoRepository,
                evaluacionRepository = evaluacionRepository
            ) as T
        }

        throw IllegalArgumentException("ViewModel no compatible")
    }
}