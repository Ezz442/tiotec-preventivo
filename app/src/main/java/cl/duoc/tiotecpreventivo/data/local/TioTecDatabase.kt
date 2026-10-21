package cl.duoc.tiotecpreventivo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import cl.duoc.tiotecpreventivo.data.local.dao.AccionDao
import cl.duoc.tiotecpreventivo.data.local.dao.DispositivoDao
import cl.duoc.tiotecpreventivo.data.local.dao.EvaluacionDao
import cl.duoc.tiotecpreventivo.data.local.dao.PerfilDao
import cl.duoc.tiotecpreventivo.data.local.dao.RecordatorioDao
import cl.duoc.tiotecpreventivo.data.local.dao.SolicitudDao
import cl.duoc.tiotecpreventivo.data.local.entity.AccionEntity
import cl.duoc.tiotecpreventivo.data.local.entity.DispositivoEntity
import cl.duoc.tiotecpreventivo.data.local.entity.EvaluacionEntity
import cl.duoc.tiotecpreventivo.data.local.entity.HistorialSolicitudEntity
import cl.duoc.tiotecpreventivo.data.local.entity.PerfilEntity
import cl.duoc.tiotecpreventivo.data.local.entity.RecordatorioEntity
import cl.duoc.tiotecpreventivo.data.local.entity.RespuestaEntity
import cl.duoc.tiotecpreventivo.data.local.entity.ResultadoCategoriaEntity
import cl.duoc.tiotecpreventivo.data.local.entity.SolicitudEntity

/**
 * Base SQLite de la app mediante Room (TT-04, 4.9).
 *
 * Cambios de esquema: subir `version`, agregar la migración a [MIGRACIONES] y subir
 * el JSON generado en `app/schemas`. No se usa fallbackToDestructiveMigration()
 * porque borraría los datos guardados.
 */
@Database(
    entities = [
        PerfilEntity::class,
        DispositivoEntity::class,
        EvaluacionEntity::class,
        RespuestaEntity::class,
        ResultadoCategoriaEntity::class,
        AccionEntity::class,
        RecordatorioEntity::class,
        SolicitudEntity::class,
        HistorialSolicitudEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class TioTecDatabase : RoomDatabase() {

    abstract fun perfilDao(): PerfilDao
    abstract fun dispositivoDao(): DispositivoDao
    abstract fun evaluacionDao(): EvaluacionDao
    abstract fun accionDao(): AccionDao
    abstract fun recordatorioDao(): RecordatorioDao
    abstract fun solicitudDao(): SolicitudDao

    companion object {
        const val NOMBRE = "tiotec_preventivo.db"

        /** Migraciones entre versiones. Vacío mientras la base siga en la versión 1. */
        val MIGRACIONES: Array<Migration> = emptyArray()

        @Volatile
        private var instancia: TioTecDatabase? = null

        fun obtener(context: Context): TioTecDatabase =
            instancia ?: synchronized(this) {
                instancia ?: construir(context.applicationContext, NOMBRE).also { instancia = it }
            }

        /** Separado de [obtener] para que las pruebas puedan abrir una base con otro nombre. */
        fun construir(context: Context, nombre: String): TioTecDatabase =
            Room.databaseBuilder(context, TioTecDatabase::class.java, nombre)
                .addMigrations(*MIGRACIONES)
                .build()
    }
}
