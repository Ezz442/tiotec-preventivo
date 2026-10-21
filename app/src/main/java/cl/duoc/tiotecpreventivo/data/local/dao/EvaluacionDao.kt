package cl.duoc.tiotecpreventivo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import cl.duoc.tiotecpreventivo.data.local.entity.AccionEntity
import cl.duoc.tiotecpreventivo.data.local.entity.EvaluacionConDetalle
import cl.duoc.tiotecpreventivo.data.local.entity.EvaluacionEntity
import cl.duoc.tiotecpreventivo.data.local.entity.RespuestaEntity
import cl.duoc.tiotecpreventivo.data.local.entity.ResultadoCategoriaEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class EvaluacionDao {

    /** Historial: la más reciente primero. Usa la copia del nombre guardada en la evaluación. */
    @Query("SELECT * FROM evaluacion ORDER BY fecha DESC, id DESC")
    abstract fun observarHistorial(): Flow<List<EvaluacionEntity>>

    @Query("SELECT * FROM evaluacion ORDER BY fecha DESC, id DESC LIMIT 1")
    abstract fun observarUltima(): Flow<EvaluacionEntity?>

    /** Solo lee: abrir el detalle no crea otra evaluación. */
    @Transaction
    @Query("SELECT * FROM evaluacion WHERE id = :id")
    abstract suspend fun obtenerDetalle(id: Long): EvaluacionConDetalle?

    @Query("SELECT COUNT(*) FROM evaluacion WHERE dispositivoId = :dispositivoId")
    abstract suspend fun contarPorDispositivo(dispositivoId: Long): Int

    @Insert
    abstract suspend fun insertarEvaluacion(evaluacion: EvaluacionEntity): Long

    @Insert
    abstract suspend fun insertarRespuestas(respuestas: List<RespuestaEntity>)

    @Insert
    abstract suspend fun insertarResultados(resultados: List<ResultadoCategoriaEntity>)

    @Insert
    abstract suspend fun insertarAcciones(acciones: List<AccionEntity>)

    /**
     * Guarda evaluación, respuestas, resultados y acciones en una sola transacción
     * (TT-04, 4.3). Si algo falla, no queda nada guardado a medias.
     * El `evaluacionId` de las listas se reemplaza por el id generado.
     */
    @Transaction
    open suspend fun guardarCompleta(
        evaluacion: EvaluacionEntity,
        respuestas: List<RespuestaEntity>,
        resultados: List<ResultadoCategoriaEntity>,
        acciones: List<AccionEntity>
    ): Long {
        val id = insertarEvaluacion(evaluacion.copy(id = 0))
        insertarRespuestas(respuestas.map { it.copy(evaluacionId = id) })
        insertarResultados(resultados.map { it.copy(evaluacionId = id) })
        insertarAcciones(acciones.map { it.copy(id = 0, evaluacionId = id) })
        return id
    }
}
