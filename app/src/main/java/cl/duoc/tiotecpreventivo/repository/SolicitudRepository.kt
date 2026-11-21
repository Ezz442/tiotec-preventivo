package cl.duoc.tiotecpreventivo.repository

import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.data.local.dao.SolicitudDao
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoSolicitud
import cl.duoc.tiotecpreventivo.data.local.entity.SolicitudConHistorial
import cl.duoc.tiotecpreventivo.data.local.entity.SolicitudEntity
import kotlinx.coroutines.flow.Flow

// #repositorio de solicitudes (Room)
/**
 * Solicitudes de asistencia simuladas (TT-04, 4.8). Esta clase lee la copia local (Room);
 * crear, cambiar estado y eliminar se hacen en el microservicio (ver SolicitudRepositorySincronizado).
 * No se envía nada a especialistas reales.
 */
open class SolicitudRepository(
    protected val dao: SolicitudDao,
    protected val reloj: () -> Long = System::currentTimeMillis
) {

    fun observarTodas(): Flow<List<SolicitudEntity>> = dao.observarTodas()

    fun observarConHistorial(id: Long): Flow<SolicitudConHistorial?> = dao.observarConHistorial(id)

    suspend fun obtenerConHistorial(id: Long): SolicitudConHistorial? = dao.obtenerConHistorial(id)

    /** Crea la solicitud con código DEMO-00X y su primer registro de historial. */
    open suspend fun crear(perfilId: Long, categoria: Categoria): Long =
        dao.crear(perfilId, categoria, reloj())

    open suspend fun cambiarEstado(id: Long, estado: EstadoSolicitud): Boolean =
        dao.cambiarEstado(id, estado, reloj())

    /** Borra la solicitud. Devuelve false si ya no existía. */
    open suspend fun eliminar(id: Long): Boolean = dao.borrar(id) > 0

    /** Trae la lista del servicio y actualiza la copia local. Sin servicio no hace nada. */
    open suspend fun sincronizar() {}

    /** Trae del servicio el detalle (con historial) de una solicitud. Sin servicio no hace nada. */
    open suspend fun sincronizarDetalle(id: Long) {}
}
