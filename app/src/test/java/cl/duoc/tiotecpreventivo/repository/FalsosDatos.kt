package cl.duoc.tiotecpreventivo.repository

import cl.duoc.tiotecpreventivo.data.catalogo.Prioridad
import cl.duoc.tiotecpreventivo.data.local.dao.AccionDao
import cl.duoc.tiotecpreventivo.data.local.dao.DispositivoDao
import cl.duoc.tiotecpreventivo.data.local.dao.EvaluacionDao
import cl.duoc.tiotecpreventivo.data.local.dao.PerfilDao
import cl.duoc.tiotecpreventivo.data.local.dao.SolicitudDao
import cl.duoc.tiotecpreventivo.data.local.entity.AccionEntity
import cl.duoc.tiotecpreventivo.data.local.entity.DispositivoEntity
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoAccion
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoSolicitud
import cl.duoc.tiotecpreventivo.data.local.entity.EvaluacionConDetalle
import cl.duoc.tiotecpreventivo.data.local.entity.EvaluacionEntity
import cl.duoc.tiotecpreventivo.data.local.entity.ExperienciaDigital
import cl.duoc.tiotecpreventivo.data.local.entity.HistorialSolicitudEntity
import cl.duoc.tiotecpreventivo.data.local.entity.PerfilEntity
import cl.duoc.tiotecpreventivo.data.local.entity.RespuestaEntity
import cl.duoc.tiotecpreventivo.data.local.entity.ResultadoCategoriaEntity
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo
import cl.duoc.tiotecpreventivo.data.local.entity.SolicitudConHistorial
import cl.duoc.tiotecpreventivo.data.local.entity.SolicitudEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/*
 * TT-21: DAOs en memoria para probar ViewModels en la JVM, sin Room ni emulador.
 * Imitan las consultas reales (orden, filtros, transacciones) que importan a cada prueba.
 * Las pruebas de Room de verdad están en app/src/androidTest.
 */

/** Pone `falla = true` para simular un error de base de datos en la siguiente escritura. */
interface ConFalla {
    var falla: Boolean
    fun revisarFalla() {
        if (falla) error("Base de datos no disponible (simulado)")
    }
}

fun perfilDePrueba(id: Long = 1) = PerfilEntity(
    id = id, alias = "Tío Prueba", experienciaDigital = ExperienciaDigital.PRINCIPIANTE,
    creadoEn = 0, actualizadoEn = 0
)

fun dispositivoDePrueba(id: Long, nombre: String, archivado: Boolean = false, imagenRuta: String? = null) =
    DispositivoEntity(
        id = id, perfilId = 1, nombre = nombre, sistemaOperativo = SistemaOperativo.ANDROID,
        imagenRuta = imagenRuta, archivado = archivado, creadoEn = 0, actualizadoEn = 0
    )

class PerfilDaoFalso(inicial: PerfilEntity? = null) : PerfilDao, ConFalla {
    override var falla = false
    val perfil = MutableStateFlow(inicial)

    override fun observar(): Flow<PerfilEntity?> = perfil
    override suspend fun obtener(): PerfilEntity? = perfil.value
    override suspend fun insertar(perfil: PerfilEntity): Long {
        revisarFalla()
        this.perfil.value = perfil.copy(id = 1)
        return 1
    }
    override suspend fun actualizar(perfil: PerfilEntity) {
        revisarFalla()
        this.perfil.value = perfil
    }
}

class DispositivoDaoFalso : DispositivoDao(), ConFalla {
    override var falla = false
    val filas = MutableStateFlow<List<DispositivoEntity>>(emptyList())
    /** dispositivoId → cantidad de evaluaciones. */
    val evaluaciones = mutableMapOf<Long, Int>()
    private var siguienteId = 100L

    override fun observarActivos(): Flow<List<DispositivoEntity>> =
        filas.map { l -> l.filter { !it.archivado }.sortedBy { it.nombre.lowercase() } }
    override fun observar(id: Long): Flow<DispositivoEntity?> = filas.map { l -> l.find { it.id == id } }
    override fun observarTodos(): Flow<List<DispositivoEntity>> = filas
    override suspend fun obtener(id: Long): DispositivoEntity? = filas.value.find { it.id == id }
    override suspend fun existeNombreActivo(nombre: String, excluirId: Long): Boolean =
        filas.value.any { !it.archivado && it.id != excluirId && it.nombre.trim().lowercase() == nombre.trim().lowercase() }
    override suspend fun insertar(dispositivo: DispositivoEntity): Long {
        revisarFalla()
        val id = siguienteId++
        filas.value = filas.value + dispositivo.copy(id = id)
        return id
    }
    override suspend fun actualizar(dispositivo: DispositivoEntity) {
        revisarFalla()
        filas.value = filas.value.map { if (it.id == dispositivo.id) dispositivo else it }
    }
    override suspend fun actualizarImagen(id: Long, ruta: String?, fecha: Long) {
        revisarFalla()
        filas.value = filas.value.map { if (it.id == id) it.copy(imagenRuta = ruta, actualizadoEn = fecha) else it }
    }
    override suspend fun contarEvaluaciones(id: Long): Int = evaluaciones[id] ?: 0
    override suspend fun borrar(id: Long): Int {
        val antes = filas.value.size
        filas.value = filas.value.filterNot { it.id == id }
        return antes - filas.value.size
    }
    override suspend fun marcarArchivado(id: Long, fecha: Long) {
        filas.value = filas.value.map { if (it.id == id) it.copy(archivado = true, actualizadoEn = fecha) else it }
    }
    override suspend fun cancelarRecordatoriosDe(id: Long): Int = 0
}

class AccionDaoFalso : AccionDao, ConFalla {
    override var falla = false
    val filas = MutableStateFlow<List<AccionEntity>>(emptyList())

    /** Mismo orden que la consulta real: ALTA → MEDIA → BAJA y luego fecha de revisión. */
    override fun observarPorEstado(estado: EstadoAccion): Flow<List<AccionEntity>> =
        filas.map { l ->
            l.filter { it.estado == estado }
                .sortedWith(compareBy<AccionEntity> { it.prioridad.ordinal }.thenBy { it.fechaRevision })
        }
    override fun contarPendientes(): Flow<Int> = filas.map { l -> l.count { it.estado == EstadoAccion.PENDIENTE } }
    override suspend fun obtener(id: Long): AccionEntity? = filas.value.find { it.id == id }
    override suspend fun cambiarEstado(id: Long, estado: EstadoAccion, completadaEn: Long?, fecha: Long): Int {
        revisarFalla()
        if (filas.value.none { it.id == id }) return 0
        filas.value = filas.value.map {
            if (it.id == id) it.copy(estado = estado, completadaEn = completadaEn, actualizadoEn = fecha) else it
        }
        return 1
    }

    companion object {
        fun accion(id: Long, prioridad: Prioridad, revision: Long, estado: EstadoAccion = EstadoAccion.PENDIENTE) =
            AccionEntity(
                id = id, evaluacionId = 1, recomendacionId = "REC-$id", titulo = "Acción $id",
                descripcion = "Descripción $id", prioridad = prioridad, estado = estado,
                fechaRevision = revision, actualizadoEn = 0
            )
    }
}

class EvaluacionDaoFalso : EvaluacionDao(), ConFalla {
    override var falla = false
    val evaluaciones = MutableStateFlow<List<EvaluacionEntity>>(emptyList())
    val respuestas = mutableListOf<RespuestaEntity>()
    val resultados = mutableListOf<ResultadoCategoriaEntity>()
    val acciones = mutableListOf<AccionEntity>()
    private var siguienteId = 1L

    override fun observarHistorial(): Flow<List<EvaluacionEntity>> =
        evaluaciones.map { l -> l.sortedWith(compareByDescending<EvaluacionEntity> { it.fecha }.thenByDescending { it.id }) }
    override fun observarUltima(): Flow<EvaluacionEntity?> = observarHistorial().map { it.firstOrNull() }
    override suspend fun obtenerDetalle(id: Long): EvaluacionConDetalle? =
        evaluaciones.value.find { it.id == id }?.let { e ->
            EvaluacionConDetalle(
                e, respuestas.filter { it.evaluacionId == id },
                resultados.filter { it.evaluacionId == id }, acciones.filter { it.evaluacionId == id }
            )
        }
    override suspend fun contarPorDispositivo(dispositivoId: Long): Int =
        evaluaciones.value.count { it.dispositivoId == dispositivoId }
    override suspend fun insertarEvaluacion(evaluacion: EvaluacionEntity): Long {
        revisarFalla()
        val id = siguienteId++
        evaluaciones.value = evaluaciones.value + evaluacion.copy(id = id)
        return id
    }
    override suspend fun insertarRespuestas(respuestas: List<RespuestaEntity>) { this.respuestas += respuestas }
    override suspend fun insertarResultados(resultados: List<ResultadoCategoriaEntity>) { this.resultados += resultados }
    override suspend fun insertarAcciones(acciones: List<AccionEntity>) {
        this.acciones += acciones.mapIndexed { i, a -> a.copy(id = this.acciones.size + i + 1L) }
    }
}

class SolicitudDaoFalso : SolicitudDao(), ConFalla {
    override var falla = false
    val solicitudes = MutableStateFlow<List<SolicitudEntity>>(emptyList())
    val historial = MutableStateFlow<List<HistorialSolicitudEntity>>(emptyList())
    private var siguienteId = 1L

    override fun observarTodas(): Flow<List<SolicitudEntity>> =
        solicitudes.map { l -> l.sortedWith(compareByDescending<SolicitudEntity> { it.creadaEn }.thenByDescending { it.id }) }
    override fun observarConHistorial(id: Long): Flow<SolicitudConHistorial?> =
        combine(solicitudes, historial) { s, h -> s.find { it.id == id }?.let { SolicitudConHistorial(it, h.filter { r -> r.solicitudId == id }) } }
    override suspend fun obtenerConHistorial(id: Long): SolicitudConHistorial? =
        solicitudes.value.find { it.id == id }?.let { SolicitudConHistorial(it, historial.value.filter { r -> r.solicitudId == id }) }
    override suspend fun contar(): Int = solicitudes.value.size
    override suspend fun insertarSolicitud(solicitud: SolicitudEntity): Long {
        revisarFalla()
        val id = siguienteId++
        solicitudes.value = solicitudes.value + solicitud.copy(id = id)
        return id
    }
    override suspend fun insertarHistorial(historial: HistorialSolicitudEntity): Long {
        val id = this.historial.value.size + 1L
        this.historial.value = this.historial.value + historial.copy(id = id)
        return id
    }
    override suspend fun actualizarEstado(id: Long, estado: EstadoSolicitud): Int {
        revisarFalla()
        if (solicitudes.value.none { it.id == id }) return 0
        solicitudes.value = solicitudes.value.map { if (it.id == id) it.copy(estado = estado) else it }
        return 1
    }
}
