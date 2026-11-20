package cl.duoc.tiotecpreventivo.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import cl.duoc.tiotecpreventivo.data.local.entity.DispositivoEntity
import cl.duoc.tiotecpreventivo.data.local.entity.HistorialSolicitudEntity
import cl.duoc.tiotecpreventivo.data.local.entity.SolicitudEntity

/**
 * TT-29: copia local de lo que guardan los microservicios.
 *
 * Los ids de Room son los mismos que asigna el servicio, así que las claves foráneas
 * (evaluaciones → dispositivo) siguen funcionando sin columnas extra. Se usa @Upsert (insertar o
 * actualizar) y nunca REPLACE: reemplazar una fila borraría en cascada sus evaluaciones.
 */
@Dao
abstract class SincronizacionDao {

    // ---------- Solicitudes ----------

    @Upsert
    abstract suspend fun guardarSolicitud(solicitud: SolicitudEntity)

    @Upsert
    abstract suspend fun guardarHistorial(historial: List<HistorialSolicitudEntity>)

    @Query("DELETE FROM historial_solicitud WHERE solicitudId = :solicitudId")
    abstract suspend fun borrarHistorialDe(solicitudId: Long)

    @Query("DELETE FROM solicitud WHERE id = :id")
    abstract suspend fun borrarSolicitud(id: Long): Int

    @Query("DELETE FROM solicitud WHERE id NOT IN (:ids)")
    abstract suspend fun borrarSolicitudesFueraDe(ids: List<Long>): Int

    /** El servicio manda: guarda las recibidas y borra las que ya no existen allá. */
    @Transaction
    open suspend fun reemplazarSolicitudes(solicitudes: List<SolicitudEntity>) {
        solicitudes.forEach { guardarSolicitud(it) }
        borrarSolicitudesFueraDe(solicitudes.map { it.id })
    }

    /** Guarda una solicitud con su historial completo (el que trae el servicio). */
    @Transaction
    open suspend fun guardarSolicitudCompleta(
        solicitud: SolicitudEntity,
        historial: List<HistorialSolicitudEntity>
    ) {
        guardarSolicitud(solicitud)
        borrarHistorialDe(solicitud.id)
        guardarHistorial(historial)
    }

    // ---------- Dispositivos ----------

    @Query("SELECT * FROM dispositivo WHERE id = :id")
    abstract suspend fun obtenerDispositivo(id: Long): DispositivoEntity?

    @Upsert
    abstract suspend fun guardarDispositivo(dispositivo: DispositivoEntity)

    /**
     * Guarda los dispositivos recibidos conservando lo que solo existe en el teléfono
     * (la ruta de la foto y el perfil dueño). No borra dispositivos locales que el servicio no
     * traiga: podrían tener evaluaciones y su historial no se debe perder.
     */
    @Transaction
    open suspend fun guardarDispositivosRemotos(perfilId: Long, dispositivos: List<DispositivoEntity>) {
        dispositivos.forEach { remoto ->
            val local = obtenerDispositivo(remoto.id)
            guardarDispositivo(
                remoto.copy(
                    perfilId = local?.perfilId ?: perfilId,
                    imagenRuta = local?.imagenRuta
                )
            )
        }
    }
}
