package cl.duoc.tiotecpreventivo.repository

import cl.duoc.tiotecpreventivo.data.local.entity.RecordatorioEntity
import kotlinx.coroutines.flow.Flow

/**
 * Programa o cancela el aviso del sistema de un recordatorio (TT-16).
 * La implementación real usa WorkManager; en las pruebas se reemplaza por una falsa.
 */
interface ProgramadorAvisos {
    fun programar(recordatorioId: Long, fechaHora: Long)
    fun cancelar(recordatorioId: Long)
}

/**
 * Une el registro en Room (RecordatorioRepository, TT-07) con el aviso programado.
 * Primero se guarda y luego se programa: si programar falla, el recordatorio queda
 * guardado y se puede volver a intentar desde la pantalla.
 */
class GestorRecordatorios(
    private val repositorio: RecordatorioRepository,
    private val programador: ProgramadorAvisos
) {

    fun observarActivos(): Flow<List<RecordatorioEntity>> = repositorio.observarActivos()

    suspend fun crearParaAccion(accionId: Long, titulo: String, fechaHora: Long): Long {
        val id = repositorio.crearParaAccion(accionId, titulo, fechaHora)
        programador.programar(id, fechaHora)
        return id
    }

    /** Cambia la fecha y vuelve a programar el aviso (reemplaza el anterior). */
    suspend fun reprogramar(id: Long, titulo: String, fechaHora: Long): Boolean {
        val modificado = repositorio.modificar(id, titulo, fechaHora)
        if (modificado) programador.programar(id, fechaHora)
        return modificado
    }

    suspend fun cancelar(id: Long): Boolean {
        programador.cancelar(id)
        return repositorio.cancelar(id)
    }
}
