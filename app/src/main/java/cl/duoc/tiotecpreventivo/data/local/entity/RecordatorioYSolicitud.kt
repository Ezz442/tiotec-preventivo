package cl.duoc.tiotecpreventivo.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import cl.duoc.tiotecpreventivo.data.catalogo.Categoria

/**
 * Recordatorio (TT-04, 4.7).
 * Tipo ACCION → `accionId` obligatorio. Tipo PROXIMA_EVALUACION → `dispositivoId` obligatorio.
 * El aviso del sistema se programa en TT-16.
 */
@Entity(
    tableName = "recordatorio",
    foreignKeys = [
        ForeignKey(
            entity = AccionEntity::class,
            parentColumns = ["id"],
            childColumns = ["accionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DispositivoEntity::class,
            parentColumns = ["id"],
            childColumns = ["dispositivoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("accionId"), Index("dispositivoId")]
)
data class RecordatorioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: TipoRecordatorio,
    val accionId: Long? = null,
    val dispositivoId: Long? = null,
    val titulo: String,
    val fechaHora: Long,
    val estado: EstadoRecordatorio = EstadoRecordatorio.PROGRAMADO
)

/**
 * Solicitud de asistencia simulada (TT-04, 4.8). No tiene texto libre para
 * evitar que se escriban datos personales.
 */
@Entity(
    tableName = "solicitud",
    foreignKeys = [
        ForeignKey(
            entity = PerfilEntity::class,
            parentColumns = ["id"],
            childColumns = ["perfilId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("perfilId"), Index(value = ["codigo"], unique = true)]
)
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val perfilId: Long,
    val codigo: String,
    val categoria: Categoria,
    val estado: EstadoSolicitud = EstadoSolicitud.PENDIENTE,
    val creadaEn: Long
)

@Entity(
    tableName = "historial_solicitud",
    foreignKeys = [
        ForeignKey(
            entity = SolicitudEntity::class,
            parentColumns = ["id"],
            childColumns = ["solicitudId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("solicitudId")]
)
data class HistorialSolicitudEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val solicitudId: Long,
    val estado: EstadoSolicitud,
    val fecha: Long,
    val nota: String
)

data class SolicitudConHistorial(
    @Embedded val solicitud: SolicitudEntity,
    @Relation(parentColumn = "id", entityColumn = "solicitudId")
    val historial: List<HistorialSolicitudEntity>
)
