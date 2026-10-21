package cl.duoc.tiotecpreventivo.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import cl.duoc.tiotecpreventivo.data.local.entity.AccionEntity
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoAccion
import kotlinx.coroutines.flow.Flow

@Dao
interface AccionDao {

    /**
     * Room guarda la prioridad como texto, así que `ORDER BY prioridad` ordenaría
     * ALTA → BAJA → MEDIA. Se usa CASE para ordenar ALTA → MEDIA → BAJA (TT-04, sección 5).
     */
    @Query(
        """
        SELECT * FROM accion
        WHERE estado = :estado
        ORDER BY
            CASE prioridad
                WHEN 'ALTA' THEN 0
                WHEN 'MEDIA' THEN 1
                WHEN 'BAJA' THEN 2
            END,
            fechaRevision ASC
        """
    )
    fun observarPorEstado(estado: EstadoAccion): Flow<List<AccionEntity>>

    @Query("SELECT COUNT(*) FROM accion WHERE estado = 'PENDIENTE'")
    fun contarPendientes(): Flow<Int>

    @Query("SELECT * FROM accion WHERE id = :id")
    suspend fun obtener(id: Long): AccionEntity?

    @Query(
        """
        UPDATE accion
        SET estado = :estado, completadaEn = :completadaEn, actualizadoEn = :fecha
        WHERE id = :id
        """
    )
    suspend fun cambiarEstado(id: Long, estado: EstadoAccion, completadaEn: Long?, fecha: Long): Int
}
