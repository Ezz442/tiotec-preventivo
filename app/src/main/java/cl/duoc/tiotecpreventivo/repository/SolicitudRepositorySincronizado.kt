package cl.duoc.tiotecpreventivo.repository

import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.data.local.dao.PerfilDao
import cl.duoc.tiotecpreventivo.data.local.dao.SincronizacionDao
import cl.duoc.tiotecpreventivo.data.local.dao.SolicitudDao
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoSolicitud
import cl.duoc.tiotecpreventivo.data.remote.ErrorRemoto
import cl.duoc.tiotecpreventivo.data.remote.SolicitudesApi
import cl.duoc.tiotecpreventivo.data.remote.aEntidad
import cl.duoc.tiotecpreventivo.data.remote.historialEntidades
import cl.duoc.tiotecpreventivo.data.remote.llamarServicio
import cl.duoc.tiotecpreventivo.data.remote.model.CambioEstadoPeticion
import cl.duoc.tiotecpreventivo.data.remote.model.SolicitudDto
import cl.duoc.tiotecpreventivo.data.remote.model.SolicitudPeticion

/**
 * TT-29: solicitudes con solicitudes-service como fuente de verdad.
 *
 * Cada operación primero se hace en el servicio y, si resulta bien, se refleja en Room con el mismo
 * id. Las pantallas siguen leyendo de Room, así que la lista se ve al instante y también sin
 * conexión; escribir (crear, cambiar estado, eliminar) sí exige que el servicio responda.
 *
 * El perfil (alias) es un dato local que no se sincroniza: solo se envía el alias al crear.
 */
class SolicitudRepositorySincronizado(
    dao: SolicitudDao,
    private val api: SolicitudesApi,
    private val sincronizacionDao: SincronizacionDao,
    private val perfilDao: PerfilDao,
    reloj: () -> Long = System::currentTimeMillis
) : SolicitudRepository(dao, reloj) {

    override suspend fun sincronizar() {
        val perfil = perfilDao.obtener() ?: return
        val remotas = llamarServicio { api.listar() }
        sincronizacionDao.reemplazarSolicitudes(remotas.mapNotNull { it.aEntidad(perfil.id) })
    }

    override suspend fun sincronizarDetalle(id: Long) {
        val perfil = perfilDao.obtener() ?: return
        try {
            guardar(llamarServicio { api.obtener(id) }, perfil.id)
        } catch (e: ErrorRemoto) {
            if (e.noExiste) sincronizacionDao.borrarSolicitud(id) else throw e
        }
    }

    override suspend fun crear(perfilId: Long, categoria: Categoria): Long {
        val perfil = perfilDao.obtener()
            ?: throw ErrorRemoto("Crea tu perfil antes de solicitar asistencia.")
        val creada = llamarServicio { api.crear(SolicitudPeticion(perfil.alias, categoria.name)) }
        guardar(creada, perfilId)
        return creada.id
    }

    override suspend fun cambiarEstado(id: Long, estado: EstadoSolicitud): Boolean {
        val perfil = perfilDao.obtener() ?: return false
        return try {
            val actualizada = llamarServicio {
                api.cambiarEstado(
                    id,
                    CambioEstadoPeticion(estado.name, "Estado cambiado a ${estado.etiqueta}")
                )
            }
            guardar(actualizada, perfil.id)
            true
        } catch (e: ErrorRemoto) {
            if (e.noExiste) {
                sincronizacionDao.borrarSolicitud(id)
                false
            } else {
                throw e
            }
        }
    }

    override suspend fun eliminar(id: Long): Boolean {
        try {
            llamarServicio { api.eliminar(id) }
        } catch (e: ErrorRemoto) {
            // Si el servicio ya no la tenía, igual hay que quitarla de la copia local.
            if (!e.noExiste) throw e
        }
        return sincronizacionDao.borrarSolicitud(id) > 0
    }

    private suspend fun guardar(dto: SolicitudDto, perfilId: Long) {
        val entidad = dto.aEntidad(perfilId)
            ?: throw ErrorRemoto("El servicio devolvió una categoría o estado desconocido.")
        sincronizacionDao.guardarSolicitudCompleta(entidad, dto.historialEntidades())
    }
}
