package cl.duoc.tiotecpreventivo.repository

import cl.duoc.tiotecpreventivo.data.local.dao.PerfilDao
import cl.duoc.tiotecpreventivo.data.local.entity.ExperienciaDigital
import cl.duoc.tiotecpreventivo.data.local.entity.PerfilEntity
import kotlinx.coroutines.flow.Flow

/**
 * Perfil ficticio único. Las validaciones de formulario (alias de 2 a 30 caracteres)
 * se hacen en el ViewModel (TT-26); aquí solo se limpia y se guarda.
 */
class PerfilRepository(
    private val dao: PerfilDao,
    private val reloj: () -> Long = System::currentTimeMillis
) {

    fun observarPerfil(): Flow<PerfilEntity?> = dao.observar()

    suspend fun obtenerPerfil(): PerfilEntity? = dao.obtener()

    /** Crea el perfil si no existe o actualiza el existente. Devuelve su id. */
    suspend fun guardar(alias: String, experiencia: ExperienciaDigital): Long {
        val ahora = reloj()
        val existente = dao.obtener()
        return if (existente == null) {
            dao.insertar(
                PerfilEntity(
                    alias = alias.trim(),
                    experienciaDigital = experiencia,
                    creadoEn = ahora,
                    actualizadoEn = ahora
                )
            )
        } else {
            dao.actualizar(
                existente.copy(
                    alias = alias.trim(),
                    experienciaDigital = experiencia,
                    actualizadoEn = ahora
                )
            )
            existente.id
        }
    }
}
