package cl.duoc.tiotecpreventivo.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.data.catalogo.Prioridad
import cl.duoc.tiotecpreventivo.data.catalogo.ValorRespuesta

/**
 * Evaluación (TT-04, 4.3). Guarda una copia del nombre y sistema operativo del
 * dispositivo para que el historial no cambie si el dispositivo se edita o se archiva.
 */
@Entity(
    tableName = "evaluacion",
    foreignKeys = [
        ForeignKey(
            entity = DispositivoEntity::class,
            parentColumns = ["id"],
            childColumns = ["dispositivoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dispositivoId")]
)
data class EvaluacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dispositivoId: Long,
    val nombreDispositivo: String,
    val sistemaOperativoDispositivo: SistemaOperativo,
    val fecha: Long,
    val nivelGlobal: NivelRiesgo
)

/** Respuesta (TT-04, 4.4). Copia el texto de la pregunta para que el historial no cambie. */
@Entity(
    tableName = "respuesta",
    primaryKeys = ["evaluacionId", "preguntaId"],
    foreignKeys = [
        ForeignKey(
            entity = EvaluacionEntity::class,
            parentColumns = ["id"],
            childColumns = ["evaluacionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RespuestaEntity(
    val evaluacionId: Long = 0,
    val preguntaId: String,
    val categoria: Categoria,
    val textoPregunta: String,
    val valor: ValorRespuesta,
    val puntaje: Int
)

/** Resultado por categoría (TT-04, 4.5). */
@Entity(
    tableName = "resultado_categoria",
    primaryKeys = ["evaluacionId", "categoria"],
    foreignKeys = [
        ForeignKey(
            entity = EvaluacionEntity::class,
            parentColumns = ["id"],
            childColumns = ["evaluacionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ResultadoCategoriaEntity(
    val evaluacionId: Long = 0,
    val categoria: Categoria,
    val puntaje: Int,
    val nivel: NivelRiesgo,
    val explicacion: String
)

/**
 * Acción del checklist (TT-04, 4.6). El índice único evita repetir la misma
 * recomendación dentro de una evaluación.
 */
@Entity(
    tableName = "accion",
    foreignKeys = [
        ForeignKey(
            entity = EvaluacionEntity::class,
            parentColumns = ["id"],
            childColumns = ["evaluacionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("evaluacionId"),
        Index(value = ["evaluacionId", "recomendacionId"], unique = true)
    ]
)
data class AccionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val evaluacionId: Long = 0,
    val recomendacionId: String,
    val titulo: String,
    val descripcion: String,
    val prioridad: Prioridad,
    val estado: EstadoAccion = EstadoAccion.PENDIENTE,
    val fechaRevision: Long,
    val completadaEn: Long? = null,
    val actualizadoEn: Long
)

/** Evaluación con todo lo que se guardó junto a ella; se usa en el detalle del historial. */
data class EvaluacionConDetalle(
    @Embedded val evaluacion: EvaluacionEntity,
    @Relation(parentColumn = "id", entityColumn = "evaluacionId")
    val respuestas: List<RespuestaEntity>,
    @Relation(parentColumn = "id", entityColumn = "evaluacionId")
    val resultados: List<ResultadoCategoriaEntity>,
    @Relation(parentColumn = "id", entityColumn = "evaluacionId")
    val acciones: List<AccionEntity>
)
