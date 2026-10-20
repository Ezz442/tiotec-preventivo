package cl.duoc.tiotecpreventivo.repository

import kotlinx.coroutines.flow.Flow

interface InicioRepository {
    fun observarAlias(): Flow<String?>

    fun observarAccionesPendientes(): Flow<Int>
}