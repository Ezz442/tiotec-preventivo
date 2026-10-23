package cl.duoc.tiotecpreventivo.repository

import cl.duoc.tiotecpreventivo.data.local.dao.RecordatorioDao
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoRecordatorio
import cl.duoc.tiotecpreventivo.data.local.entity.RecordatorioEntity
import cl.duoc.tiotecpreventivo.data.local.entity.TipoRecordatorio
import kotlinx.coroutines.flow.Flow

/**
 * Recordatorios guardados localmente (TT-04, 4.7). Aquí solo se persisten; el aviso
 * del sistema lo programa GestorRecordatorios con WorkManager (TT-16).
 *
 * Los errores de formulario por campo los muestra el ViewModel (TT-26); estas
 * validaciones son la última barrera y lanzan IllegalArgumentException.
 */
class RecordatorioRepository(
    private val dao: RecordatorioDao,
    private val reloj: () -> Long = System::currentTimeMillis
) {

    fun observarActivos(): Flow<List<RecordatorioEntity>> = dao.observarActivos()

    suspend fun obtener(id: Long): RecordatorioEntity? = dao.obtener(id)

    suspend fun crearParaAccion(accionId: Long, titulo: String, fechaHora: Long): Long {
        validar(titulo, fechaHora)
        return dao.insertar(
            RecordatorioEntity(
                tipo = TipoRecordatorio.ACCION,
                accionId = accionId,
                titulo = titulo.trim(),
                fechaHora = fechaHora
            )
        )
    }

    suspend fun crearProximaEvaluacion(dispositivoId: Long, titulo: String, fechaHora: Long): Long {
        validar(titulo, fechaHora)
        return dao.insertar(
            RecordatorioEntity(
                tipo = TipoRecordatorio.PROXIMA_EVALUACION,
                dispositivoId = dispositivoId,
                titulo = titulo.trim(),
                fechaHora = fechaHora
            )
        )
    }

    /** Cambia título y fecha de un recordatorio programado. */
    suspend fun modificar(id: Long, titulo: String, fechaHora: Long): Boolean {
        validar(titulo, fechaHora)
        val actual = dao.obtener(id) ?: return false
        if (actual.estado != EstadoRecordatorio.PROGRAMADO) return false
        dao.actualizar(actual.copy(titulo = titulo.trim(), fechaHora = fechaHora))
        return true
    }

    suspend fun cancelar(id: Long): Boolean =
        dao.cambiarEstado(id, EstadoRecordatorio.CANCELADO) > 0

    /** Lo llama RecordatorioWorker cuando llega la hora del aviso (TT-16). */
    suspend fun marcarEnviado(id: Long): Boolean =
        dao.cambiarEstado(id, EstadoRecordatorio.ENVIADO) > 0

    private fun validar(titulo: String, fechaHora: Long) {
        require(titulo.isNotBlank()) { "El recordatorio necesita un título" }
        require(fechaHora > reloj()) { "La fecha y hora deben estar en el futuro" }
    }
}
