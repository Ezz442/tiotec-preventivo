package cl.duoc.tiotecpreventivo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoSolicitud
import cl.duoc.tiotecpreventivo.data.local.entity.HistorialSolicitudEntity
import cl.duoc.tiotecpreventivo.data.local.entity.SolicitudConHistorial
import cl.duoc.tiotecpreventivo.data.local.entity.SolicitudEntity
import kotlinx.coroutines.flow.Flow
import java.util.Locale

@Dao
abstract class SolicitudDao {

    @Query("SELECT * FROM solicitud ORDER BY creadaEn DESC, id DESC")
    abstract fun observarTodas(): Flow<List<SolicitudEntity>>

    @Transaction
    @Query("SELECT * FROM solicitud WHERE id = :id")
    abstract fun observarConHistorial(id: Long): Flow<SolicitudConHistorial?>

    @Transaction
    @Query("SELECT * FROM solicitud WHERE id = :id")
    abstract suspend fun obtenerConHistorial(id: Long): SolicitudConHistorial?

    @Query("SELECT COUNT(*) FROM solicitud")
    abstract suspend fun contar(): Int

    @Insert
    abstract suspend fun insertarSolicitud(solicitud: SolicitudEntity): Long

    @Insert
    abstract suspend fun insertarHistorial(historial: HistorialSolicitudEntity): Long

    @Query("UPDATE solicitud SET estado = :estado WHERE id = :id")
    abstract suspend fun actualizarEstado(id: Long, estado: EstadoSolicitud): Int

    /** Crea la solicitud con código "DEMO-001", "DEMO-002"… y su primer registro de historial. */
    @Transaction
    open suspend fun crear(perfilId: Long, categoria: Categoria, fecha: Long): Long {
        val codigo = String.format(Locale.ROOT, "DEMO-%03d", contar() + 1)
        val id = insertarSolicitud(
            SolicitudEntity(
                perfilId = perfilId,
                codigo = codigo,
                categoria = categoria,
                creadaEn = fecha
            )
        )
        insertarHistorial(
            HistorialSolicitudEntity(
                solicitudId = id,
                estado = EstadoSolicitud.PENDIENTE,
                fecha = fecha,
                nota = "Solicitud creada (simulación)"
            )
        )
        return id
    }

    /** Cambia el estado y deja el cambio registrado en el historial. */
    @Transaction
    open suspend fun cambiarEstado(id: Long, estado: EstadoSolicitud, fecha: Long): Boolean {
        if (actualizarEstado(id, estado) == 0) return false
        insertarHistorial(
            HistorialSolicitudEntity(
                solicitudId = id,
                estado = estado,
                fecha = fecha,
                nota = "Estado cambiado a ${estado.etiqueta} (simulación)"
            )
        )
        return true
    }
}
