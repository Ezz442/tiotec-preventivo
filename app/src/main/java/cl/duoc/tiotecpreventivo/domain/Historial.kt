package cl.duoc.tiotecpreventivo.domain

import cl.duoc.tiotecpreventivo.data.local.entity.EvaluacionEntity

/**
 * Opción del filtro por dispositivo del historial (TT-14).
 * @param nombre nombre actual del dispositivo; si ya no existe, el de su evaluación más reciente.
 */
data class FiltroDispositivo(
    val dispositivoId: Long,
    val nombre: String,
    val cantidad: Int
)

/**
 * Reglas del historial, sin Android (TT-14).
 *
 * Trabaja sobre las copias guardadas en cada evaluación (nombre y sistema operativo del
 * dispositivo), así que editar o archivar un dispositivo no cambia lo que ya se evaluó.
 */
object HistorialLogica {

    /** Más reciente primero; si dos tienen la misma fecha, la de mayor id. */
    fun ordenar(evaluaciones: List<EvaluacionEntity>): List<EvaluacionEntity> =
        evaluaciones.sortedWith(
            compareByDescending<EvaluacionEntity> { it.fecha }.thenByDescending { it.id }
        )

    /**
     * Un filtro por cada dispositivo que tiene evaluaciones, ordenados por nombre.
     * @param nombresActuales dispositivoId → nombre actual (incluye archivados). El filtro
     *   usa el nombre actual, aunque las tarjetas sigan mostrando el que tenía al evaluarse.
     */
    fun filtros(
        evaluaciones: List<EvaluacionEntity>,
        nombresActuales: Map<Long, String> = emptyMap()
    ): List<FiltroDispositivo> =
        evaluaciones
            .groupBy { it.dispositivoId }
            .map { (dispositivoId, delDispositivo) ->
                FiltroDispositivo(
                    dispositivoId = dispositivoId,
                    nombre = nombresActuales[dispositivoId]
                        ?: ordenar(delDispositivo).first().nombreDispositivo,
                    cantidad = delDispositivo.size
                )
            }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.nombre })

    /** `dispositivoId = null` devuelve todas. */
    fun filtrar(evaluaciones: List<EvaluacionEntity>, dispositivoId: Long?): List<EvaluacionEntity> =
        ordenar(
            if (dispositivoId == null) evaluaciones
            else evaluaciones.filter { it.dispositivoId == dispositivoId }
        )
}
