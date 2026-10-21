package cl.duoc.tiotecpreventivo.repository

import cl.duoc.tiotecpreventivo.data.local.dao.AccionDao
import cl.duoc.tiotecpreventivo.data.local.entity.AccionEntity
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoAccion
import kotlinx.coroutines.flow.Flow

/** Checklist de acciones (TT-04, 4.6). Las acciones se crean al guardar una evaluación. */
class AccionRepository(
    private val dao: AccionDao,
    private val reloj: () -> Long = System::currentTimeMillis
) {

    /** Ordenadas ALTA → MEDIA → BAJA y luego por fecha de revisión. */
    fun observarPorEstado(estado: EstadoAccion): Flow<List<AccionEntity>> =
        dao.observarPorEstado(estado)

    fun contarPendientes(): Flow<Int> = dao.contarPendientes()

    suspend fun obtener(id: Long): AccionEntity? = dao.obtener(id)

    /** Al completar guarda la fecha; al volver a pendiente la borra. */
    suspend fun cambiarEstado(id: Long, estado: EstadoAccion): Boolean {
        val ahora = reloj()
        val completadaEn = if (estado == EstadoAccion.COMPLETADA) ahora else null
        return dao.cambiarEstado(id, estado, completadaEn, ahora) > 0
    }
}
