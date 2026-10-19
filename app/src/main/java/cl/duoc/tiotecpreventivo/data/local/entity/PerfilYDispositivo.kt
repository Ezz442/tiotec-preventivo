package cl.duoc.tiotecpreventivo.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Perfil ficticio. La app maneja un solo perfil (TT-04, 4.1). */
@Entity(tableName = "perfil")
data class PerfilEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alias: String,
    val experienciaDigital: ExperienciaDigital,
    val creadoEn: Long,
    val actualizadoEn: Long
)

/**
 * Dispositivo de prueba (TT-04, 4.2).
 * Si tiene evaluaciones no se borra: se marca `archivado` para conservar el historial.
 */
@Entity(
    tableName = "dispositivo",
    foreignKeys = [
        ForeignKey(
            entity = PerfilEntity::class,
            parentColumns = ["id"],
            childColumns = ["perfilId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("perfilId")]
)
data class DispositivoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val perfilId: Long,
    val nombre: String,
    val sistemaOperativo: SistemaOperativo,
    val imagenRuta: String? = null,
    val archivado: Boolean = false,
    val creadoEn: Long,
    val actualizadoEn: Long
)
