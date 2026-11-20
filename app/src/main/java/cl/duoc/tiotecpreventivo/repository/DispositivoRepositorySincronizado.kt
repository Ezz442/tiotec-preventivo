package cl.duoc.tiotecpreventivo.repository

import cl.duoc.tiotecpreventivo.data.local.dao.DispositivoDao
import cl.duoc.tiotecpreventivo.data.local.dao.PerfilDao
import cl.duoc.tiotecpreventivo.data.local.dao.SincronizacionDao
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo
import cl.duoc.tiotecpreventivo.data.remote.DispositivosApi
import cl.duoc.tiotecpreventivo.data.remote.ErrorRemoto
import cl.duoc.tiotecpreventivo.data.remote.aEntidad
import cl.duoc.tiotecpreventivo.data.remote.llamarServicio
import cl.duoc.tiotecpreventivo.data.remote.model.ArchivadoPeticion
import cl.duoc.tiotecpreventivo.data.remote.model.DispositivoPeticion

/**
 * TT-29: dispositivos con dispositivos-service como fuente de verdad del nombre y el sistema
 * operativo. Lo que solo existe en el teléfono (foto, evaluaciones, historial) sigue en Room y no
 * se envía: por eso Room conserva una copia con el mismo id del servicio.
 *
 * Eliminar mantiene la regla de la app: con evaluaciones el dispositivo se archiva (en el servicio
 * también, con PATCH /archivado); sin evaluaciones se borra (DELETE).
 */
class DispositivoRepositorySincronizado(
    dao: DispositivoDao,
    private val api: DispositivosApi,
    private val sincronizacionDao: SincronizacionDao,
    private val perfilDao: PerfilDao,
    reloj: () -> Long = System::currentTimeMillis
) : DispositivoRepository(dao, reloj) {

    override suspend fun sincronizar() {
        val perfil = perfilDao.obtener() ?: return
        val remotos = llamarServicio { api.listar() }
        sincronizacionDao.guardarDispositivosRemotos(perfil.id, remotos.mapNotNull { it.aEntidad(perfil.id) })
    }

    override suspend fun crear(
        perfilId: Long,
        nombre: String,
        sistemaOperativo: SistemaOperativo,
        imagenRuta: String?
    ): Long {
        val creado = llamarServicio {
            api.crear(DispositivoPeticion(nombre.trim(), sistemaOperativo.name))
        }
        val entidad = creado.aEntidad(perfilId, imagenRuta)
            ?: throw ErrorRemoto("El servicio devolvió un sistema operativo desconocido.")
        sincronizacionDao.guardarDispositivo(entidad)
        return creado.id
    }

    override suspend fun actualizar(id: Long, nombre: String, sistemaOperativo: SistemaOperativo): Boolean {
        val actual = dao.obtener(id) ?: return false
        val actualizado = llamarServicio {
            api.actualizar(id, DispositivoPeticion(nombre.trim(), sistemaOperativo.name))
        }
        dao.actualizar(
            actual.copy(
                nombre = actualizado.nombre,
                sistemaOperativo = sistemaOperativo,
                actualizadoEn = actualizado.actualizadoEn
            )
        )
        return true
    }

    override suspend fun eliminar(id: Long): ResultadoEliminacion {
        if (dao.obtener(id) == null) return ResultadoEliminacion.NoEncontrado

        if (dao.contarEvaluaciones(id) > 0) {
            tolerandoAusenteOYaHecho { api.cambiarArchivado(id, ArchivadoPeticion(true)) }
        } else {
            tolerandoAusenteOYaHecho { api.eliminar(id) }
        }
        // La parte local (borrar o archivar, cancelar recordatorios) es la de siempre.
        return super.eliminar(id)
    }

    /** Si el servicio ya no lo tiene (404) o ya estaba archivado (409), lo local se completa igual. */
    private suspend fun tolerandoAusenteOYaHecho(llamada: suspend () -> Unit) {
        try {
            llamarServicio { llamada() }
        } catch (e: ErrorRemoto) {
            if (!e.noExiste && !e.conflicto) throw e
        }
    }
}
