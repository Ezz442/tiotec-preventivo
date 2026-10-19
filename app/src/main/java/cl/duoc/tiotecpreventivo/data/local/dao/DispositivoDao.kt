package cl.duoc.tiotecpreventivo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import cl.duoc.tiotecpreventivo.data.local.entity.DispositivoEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class DispositivoDao {

    /** Dispositivos visibles en "Mis dispositivos": los archivados no aparecen. */
    @Query("SELECT * FROM dispositivo WHERE archivado = 0 ORDER BY nombre COLLATE NOCASE")
    abstract fun observarActivos(): Flow<List<DispositivoEntity>>

    @Query("SELECT * FROM dispositivo WHERE id = :id")
    abstract fun observar(id: Long): Flow<DispositivoEntity?>

    @Query("SELECT * FROM dispositivo WHERE id = :id")
    abstract suspend fun obtener(id: Long): DispositivoEntity?

    /** Para validar que el nombre no se repita entre los dispositivos activos. */
    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM dispositivo
            WHERE archivado = 0
              AND LOWER(TRIM(nombre)) = LOWER(TRIM(:nombre))
              AND id != :excluirId
        )
        """
    )
    abstract suspend fun existeNombreActivo(nombre: String, excluirId: Long): Boolean

    @Insert
    abstract suspend fun insertar(dispositivo: DispositivoEntity): Long

    @Update
    abstract suspend fun actualizar(dispositivo: DispositivoEntity)

    @Query("UPDATE dispositivo SET imagenRuta = :ruta, actualizadoEn = :fecha WHERE id = :id")
    abstract suspend fun actualizarImagen(id: Long, ruta: String?, fecha: Long)

    @Query("SELECT COUNT(*) FROM evaluacion WHERE dispositivoId = :id")
    abstract suspend fun contarEvaluaciones(id: Long): Int

    @Query("DELETE FROM dispositivo WHERE id = :id")
    abstract suspend fun borrar(id: Long): Int

    @Query("UPDATE dispositivo SET archivado = 1, actualizadoEn = :fecha WHERE id = :id")
    abstract suspend fun marcarArchivado(id: Long, fecha: Long)

    @Query(
        """
        UPDATE recordatorio SET estado = 'CANCELADO'
        WHERE dispositivoId = :id AND estado = 'PROGRAMADO'
        """
    )
    abstract suspend fun cancelarRecordatoriosDe(id: Long)

    /**
     * Regla de eliminación de TT-04 (4.2), en una sola transacción:
     * - sin evaluaciones → se borra el registro;
     * - con evaluaciones → se archiva y se cancelan sus recordatorios programados,
     *   pero sus evaluaciones siguen en el historial.
     * @return true si se archivó, false si se borró.
     */
    @Transaction
    open suspend fun eliminarOArchivar(id: Long, fecha: Long): Boolean {
        return if (contarEvaluaciones(id) == 0) {
            borrar(id)
            false
        } else {
            marcarArchivado(id, fecha)
            cancelarRecordatoriosDe(id)
            true
        }
    }
}
