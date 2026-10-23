package cl.duoc.tiotecpreventivo.notificaciones

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import cl.duoc.tiotecpreventivo.TioTecApplication
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoAccion
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoRecordatorio
import cl.duoc.tiotecpreventivo.repository.ProgramadorAvisos
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Se ejecuta a la hora del recordatorio y muestra el aviso (TT-16).
 * Antes de avisar vuelve a leer Room: si el recordatorio se canceló, la acción ya se
 * completó, el dispositivo se archivó o el usuario apagó los avisos en Ajustes, no avisa.
 */
class RecordatorioWorker(
    context: Context,
    parametros: WorkerParameters
) : CoroutineWorker(context, parametros) {

    override suspend fun doWork(): Result {
        val id = inputData.getLong(CLAVE_ID, -1L)
        if (id <= 0) return Result.success()

        val contenedor = (applicationContext as TioTecApplication).contenedor
        val recordatorio = contenedor.recordatorioRepository.obtener(id) ?: return Result.success()
        if (recordatorio.estado != EstadoRecordatorio.PROGRAMADO) return Result.success()

        val accionCompletada = recordatorio.accionId
            ?.let { contenedor.accionRepository.obtener(it) }
            ?.estado == EstadoAccion.COMPLETADA
        val avisosActivos = contenedor.preferenciasRepository.preferencias.first().avisosActivos

        if (!accionCompletada && avisosActivos) {
            Avisos.mostrarRecordatorio(applicationContext, id, recordatorio.titulo)
        }
        contenedor.recordatorioRepository.marcarEnviado(id)
        return Result.success()
    }

    companion object {
        const val CLAVE_ID = "recordatorioId"
        fun nombreTrabajo(id: Long) = "recordatorio-$id"
    }
}

/** Programa los avisos con WorkManager: sobreviven a cerrar la app y a reiniciar el teléfono. */
class ProgramadorWorkManager(context: Context) : ProgramadorAvisos {

    private val workManager = WorkManager.getInstance(context.applicationContext)

    override fun programar(recordatorioId: Long, fechaHora: Long) {
        val espera = (fechaHora - System.currentTimeMillis()).coerceAtLeast(0)
        val trabajo = OneTimeWorkRequestBuilder<RecordatorioWorker>()
            .setInitialDelay(espera, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(RecordatorioWorker.CLAVE_ID to recordatorioId))
            .build()
        // REPLACE: al editar la hora se reemplaza el aviso anterior, nunca quedan dos.
        workManager.enqueueUniqueWork(
            RecordatorioWorker.nombreTrabajo(recordatorioId),
            ExistingWorkPolicy.REPLACE,
            trabajo
        )
    }

    override fun cancelar(recordatorioId: Long) {
        workManager.cancelUniqueWork(RecordatorioWorker.nombreTrabajo(recordatorioId))
    }
}
