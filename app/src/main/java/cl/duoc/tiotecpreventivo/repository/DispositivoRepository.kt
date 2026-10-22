package cl.duoc.tiotecpreventivo.repository

import cl.duoc.tiotecpreventivo.data.local.dao.DispositivoDao
import cl.duoc.tiotecpreventivo.data.local.entity.DispositivoEntity
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo
import kotlinx.coroutines.flow.Flow

/** Resultado de [DispositivoRepository.eliminar]; la pantalla elige el mensaje según el caso. */
sealed interface ResultadoEliminacion {
    /** Se borró el registro. Si tenía imagen, su ruta se entrega para borrar el archivo (TT-18/19). */
    data class Eliminado(val imagenRuta: String?) : ResultadoEliminacion

    /** Tenía evaluaciones: se ocultó de la lista y su historial se conserva. */
    data object Archivado : ResultadoEliminacion

    data object NoEncontrado : ResultadoEliminacion
}

/**
 * CRUD de dispositivos de prueba (TT-04, 4.2 y sección 5).
 * La confirmación con AlertDialog la muestra la pantalla (TT-09) antes de llamar a [eliminar].
 */
class DispositivoRepository(
    private val dao: DispositivoDao,
    private val reloj: () -> Long = System::currentTimeMillis
) {

    fun observarActivos(): Flow<List<DispositivoEntity>> = dao.observarActivos()

    fun observar(id: Long): Flow<DispositivoEntity?> = dao.observar(id)

    /** Incluye archivados. Lo usa el filtro del historial para mostrar el nombre actual (TT-14). */
    fun observarTodos(): Flow<List<DispositivoEntity>> = dao.observarTodos()

    suspend fun obtener(id: Long): DispositivoEntity? = dao.obtener(id)

    /** Para la validación "nombre no repetido entre activos". `excluirId` = id en edición, 0 al crear. */
    suspend fun existeNombreActivo(nombre: String, excluirId: Long = 0): Boolean =
        dao.existeNombreActivo(nombre, excluirId)

    /** Crea un dispositivo y devuelve su id. */
    suspend fun crear(
        perfilId: Long,
        nombre: String,
        sistemaOperativo: SistemaOperativo,
        imagenRuta: String? = null
    ): Long {
        val ahora = reloj()
        return dao.insertar(
            DispositivoEntity(
                perfilId = perfilId,
                nombre = nombre.trim(),
                sistemaOperativo = sistemaOperativo,
                imagenRuta = imagenRuta,
                creadoEn = ahora,
                actualizadoEn = ahora
            )
        )
    }

    /** Edita nombre y sistema operativo. Las evaluaciones anteriores conservan su copia. */
    suspend fun actualizar(id: Long, nombre: String, sistemaOperativo: SistemaOperativo): Boolean {
        val actual = dao.obtener(id) ?: return false
        dao.actualizar(
            actual.copy(
                nombre = nombre.trim(),
                sistemaOperativo = sistemaOperativo,
                actualizadoEn = reloj()
            )
        )
        return true
    }

    /** Guarda o quita (null) la ruta de la imagen. El archivo lo maneja TT-18/19. */
    suspend fun actualizarImagen(id: Long, ruta: String?) =
        dao.actualizarImagen(id, ruta, reloj())

    /**
     * Regla de TT-04 (4.2): sin evaluaciones se borra; con evaluaciones se archiva
     * para que el historial siga visible.
     */
    suspend fun eliminar(id: Long): ResultadoEliminacion {
        val dispositivo = dao.obtener(id) ?: return ResultadoEliminacion.NoEncontrado
        val archivado = dao.eliminarOArchivar(id, reloj())
        return if (archivado) {
            ResultadoEliminacion.Archivado
        } else {
            ResultadoEliminacion.Eliminado(dispositivo.imagenRuta)
        }
    }
}
