package cl.duoc.tiotecpreventivo.repository

import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.data.catalogo.ValorRespuesta
import cl.duoc.tiotecpreventivo.data.local.dao.DispositivoDao
import cl.duoc.tiotecpreventivo.data.local.dao.EvaluacionDao
import cl.duoc.tiotecpreventivo.data.local.entity.AccionEntity
import cl.duoc.tiotecpreventivo.data.local.entity.EvaluacionConDetalle
import cl.duoc.tiotecpreventivo.data.local.entity.EvaluacionEntity
import cl.duoc.tiotecpreventivo.data.local.entity.NivelRiesgo
import cl.duoc.tiotecpreventivo.data.local.entity.RespuestaEntity
import cl.duoc.tiotecpreventivo.data.local.entity.ResultadoCategoriaEntity
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

/** Resultado de una categoría ya calculado por las reglas de riesgo (TT-10 / TT-12). */
data class ResultadoCalculado(
    val categoria: Categoria,
    val puntaje: Int,
    val nivel: NivelRiesgo,
    val explicacion: String
)

/**
 * Guarda y lee evaluaciones (TT-04, 4.3 y sección 5).
 * El cálculo de puntajes y niveles es de TT-10/TT-12: este repositorio recibe los
 * resultados ya calculados y se encarga de persistir todo en una sola transacción.
 */
class EvaluacionRepository(
    private val evaluacionDao: EvaluacionDao,
    private val dispositivoDao: DispositivoDao,
    private val catalogo: CatalogoRepository,
    private val reloj: () -> Long = System::currentTimeMillis
) {

    fun observarHistorial(): Flow<List<EvaluacionEntity>> = evaluacionDao.observarHistorial()

    fun observarUltima(): Flow<EvaluacionEntity?> = evaluacionDao.observarUltima()

    suspend fun obtenerDetalle(id: Long): EvaluacionConDetalle? = evaluacionDao.obtenerDetalle(id)

    /**
     * Guarda la evaluación finalizada con sus respuestas, resultados y las acciones de
     * las recomendaciones activadas. Si algo falla, no se guarda nada (transacción) y
     * la pantalla puede reintentar con las mismas respuestas.
     *
     * @param respuestas preguntaId → respuesta; deben estar todas las preguntas del catálogo.
     * @return id de la evaluación creada.
     * @throws IllegalArgumentException si el dispositivo no existe o está archivado,
     *   faltan respuestas o no hay resultados.
     */
    suspend fun guardarEvaluacion(
        dispositivoId: Long,
        respuestas: Map<String, ValorRespuesta>,
        resultados: List<ResultadoCalculado>
    ): Long {
        val dispositivo = requireNotNull(dispositivoDao.obtener(dispositivoId)) {
            "El dispositivo $dispositivoId no existe"
        }
        require(!dispositivo.archivado) { "No se puede evaluar un dispositivo archivado" }

        val preguntas = catalogo.preguntas()
        val faltantes = preguntas.map { it.id }.filterNot { it in respuestas }
        require(faltantes.isEmpty()) { "Faltan respuestas: $faltantes" }
        require(resultados.isNotEmpty()) { "La evaluación necesita resultados por categoría" }

        val ahora = reloj()

        val entidadesRespuesta = preguntas.map { pregunta ->
            val valor = respuestas.getValue(pregunta.id)
            RespuestaEntity(
                preguntaId = pregunta.id,
                categoria = pregunta.categoria,
                textoPregunta = pregunta.texto,
                valor = valor,
                puntaje = pregunta.puntajes.getValue(valor)
            )
        }

        val entidadesResultado = resultados.map {
            ResultadoCategoriaEntity(
                categoria = it.categoria,
                puntaje = it.puntaje,
                nivel = it.nivel,
                explicacion = it.explicacion
            )
        }

        val acciones = catalogo.recomendacionesPara(respuestas).map { recomendacion ->
            AccionEntity(
                recomendacionId = recomendacion.id,
                titulo = recomendacion.titulo,
                descripcion = recomendacion.descripcion,
                prioridad = recomendacion.prioridad,
                fechaRevision = ahora + TimeUnit.DAYS.toMillis(recomendacion.diasParaRevision.toLong()),
                actualizadoEn = ahora
            )
        }

        val evaluacion = EvaluacionEntity(
            dispositivoId = dispositivo.id,
            nombreDispositivo = dispositivo.nombre,
            sistemaOperativoDispositivo = dispositivo.sistemaOperativo,
            fecha = ahora,
            nivelGlobal = resultados.maxOf { it.nivel }
        )

        return evaluacionDao.guardarCompleta(evaluacion, entidadesRespuesta, entidadesResultado, acciones)
    }
}
