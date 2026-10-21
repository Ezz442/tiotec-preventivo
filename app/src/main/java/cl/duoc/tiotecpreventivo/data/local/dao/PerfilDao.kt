package cl.duoc.tiotecpreventivo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.tiotecpreventivo.data.local.entity.PerfilEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PerfilDao {

    /** La app maneja un solo perfil; si no existe, emite null. */
    @Query("SELECT * FROM perfil ORDER BY id LIMIT 1")
    fun observar(): Flow<PerfilEntity?>

    @Query("SELECT * FROM perfil ORDER BY id LIMIT 1")
    suspend fun obtener(): PerfilEntity?

    @Insert
    suspend fun insertar(perfil: PerfilEntity): Long

    @Update
    suspend fun actualizar(perfil: PerfilEntity)
}
