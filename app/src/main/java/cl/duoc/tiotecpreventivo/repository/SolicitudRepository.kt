package cl.duoc.tiotecpreventivo.repository

import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.data.local.dao.SolicitudDao
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoSolicitud
import cl.duoc.tiotecpreventivo.data.local.entity.SolicitudConHistorial
import cl.duoc.tiotecpreventivo.data.local.entity.SolicitudEntity
import kotlinx.coroutines.flow.Flow

/**
 * Solicitudes de asistencia simuladas (TT-04, 4.8). Todo queda en el teléfono:
 * no se envía nada a ningún servicio real.
 */
class SolicitudRepository(
    private val dao: SolicitudDao,
    private val reloj: () -> Long = System::currentTimeMillis
) {

    fun observarTodas(): Flow<List<SolicitudEntity>> = dao.observarTodas()

    fun observarConHistorial(id: Long): Flow<SolicitudConHistorial?> = dao.observarConHistorial(id)

    suspend fun obtenerConHistorial(id: Long): SolicitudConHistorial? = dao.obtenerConHistorial(id)

    /** Crea la solicitud con código DEMO-00X y su primer registro de historial. */
    suspend fun crear(perfilId: Long, categoria: Categoria): Long =
        dao.crear(perfilId, categoria, reloj())

    suspend fun cambiarEstado(id: Long, estado: EstadoSolicitud): Boolean =
        dao.cambiarEstado(id, estado, reloj())
}
