package cl.duoc.tiotecpreventivo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoRecordatorio
import cl.duoc.tiotecpreventivo.data.local.entity.RecordatorioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordatorioDao {

    @Query("SELECT * FROM recordatorio WHERE estado = 'PROGRAMADO' ORDER BY fechaHora ASC")
    fun observarActivos(): Flow<List<RecordatorioEntity>>

    @Query("SELECT * FROM recordatorio WHERE id = :id")
    suspend fun obtener(id: Long): RecordatorioEntity?

    @Insert
    suspend fun insertar(recordatorio: RecordatorioEntity): Long

    @Update
    suspend fun actualizar(recordatorio: RecordatorioEntity)

    @Query("UPDATE recordatorio SET estado = :estado WHERE id = :id")
    suspend fun cambiarEstado(id: Long, estado: EstadoRecordatorio): Int
}
