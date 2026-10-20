package cl.duoc.tiotecpreventivo.data.local

import cl.duoc.tiotecpreventivo.repository.InicioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InicioRepositoryMemoria : InicioRepository {

    private val alias = MutableStateFlow<String?>(null)
    private val accionesPendientes = MutableStateFlow(0)

    override fun observarAlias(): Flow<String?> {
        return alias.asStateFlow()
    }

    override fun observarAccionesPendientes(): Flow<Int> {
        return accionesPendientes.asStateFlow()
    }
}