package cl.duoc.tiotecpreventivo.repository

import cl.duoc.tiotecpreventivo.data.local.dao.RecordatorioDao
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoRecordatorio
import cl.duoc.tiotecpreventivo.data.local.entity.RecordatorioEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** DAO en memoria para probar recordatorios sin Room ni emulador (TT-16). */
class RecordatorioDaoFalso : RecordatorioDao {

    val filas = MutableStateFlow<List<RecordatorioEntity>>(emptyList())
    private var siguienteId = 1L

    override fun observarActivos(): Flow<List<RecordatorioEntity>> =
        filas.map { lista -> lista.filter { it.estado == EstadoRecordatorio.PROGRAMADO }.sortedBy { it.fechaHora } }

    override suspend fun obtener(id: Long): RecordatorioEntity? = filas.value.find { it.id == id }

    override suspend fun insertar(recordatorio: RecordatorioEntity): Long {
        val id = siguienteId++
        filas.value = filas.value + recordatorio.copy(id = id)
        return id
    }

    override suspend fun actualizar(recordatorio: RecordatorioEntity) {
        filas.value = filas.value.map { if (it.id == recordatorio.id) recordatorio else it }
    }

    override suspend fun cambiarEstado(id: Long, estado: EstadoRecordatorio): Int {
        if (filas.value.none { it.id == id }) return 0
        filas.value = filas.value.map { if (it.id == id) it.copy(estado = estado) else it }
        return 1
    }
}

/** Registra lo que se programa en lugar de usar WorkManager. */
class ProgramadorFalso : ProgramadorAvisos {
    /** recordatorioId → fechaHora del aviso programado. */
    val programados = mutableMapOf<Long, Long>()
    var vecesProgramado = 0

    override fun programar(recordatorioId: Long, fechaHora: Long) {
        programados[recordatorioId] = fechaHora
        vecesProgramado++
    }

    override fun cancelar(recordatorioId: Long) {
        programados.remove(recordatorioId)
    }
}
