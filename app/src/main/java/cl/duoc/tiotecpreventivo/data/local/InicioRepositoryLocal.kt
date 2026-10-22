package cl.duoc.tiotecpreventivo.data.local

import cl.duoc.tiotecpreventivo.repository.AccionRepository
import cl.duoc.tiotecpreventivo.repository.InicioRepository
import cl.duoc.tiotecpreventivo.repository.PerfilRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InicioRepositoryLocal(
    private val perfilRepository: PerfilRepository,
    private val accionRepository: AccionRepository
) : InicioRepository {

    override fun observarAlias(): Flow<String?> =
        perfilRepository.observarPerfil().map { perfil -> perfil?.alias }

    override fun observarAccionesPendientes(): Flow<Int> =
        accionRepository.contarPendientes()
}