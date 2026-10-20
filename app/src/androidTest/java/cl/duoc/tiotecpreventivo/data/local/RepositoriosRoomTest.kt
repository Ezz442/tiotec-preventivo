package cl.duoc.tiotecpreventivo.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.data.catalogo.CatalogoLocal
import cl.duoc.tiotecpreventivo.data.catalogo.CatalogoRepositoryLocal
import cl.duoc.tiotecpreventivo.data.catalogo.Prioridad
import cl.duoc.tiotecpreventivo.data.catalogo.ValorRespuesta
import cl.duoc.tiotecpreventivo.data.local.entity.AccionEntity
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoAccion
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoRecordatorio
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoSolicitud
import cl.duoc.tiotecpreventivo.data.local.entity.EvaluacionEntity
import cl.duoc.tiotecpreventivo.data.local.entity.ExperienciaDigital
import cl.duoc.tiotecpreventivo.data.local.entity.NivelRiesgo
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo
import cl.duoc.tiotecpreventivo.repository.AccionRepository
import cl.duoc.tiotecpreventivo.repository.DispositivoRepository
import cl.duoc.tiotecpreventivo.repository.EvaluacionRepository
import cl.duoc.tiotecpreventivo.repository.PerfilRepository
import cl.duoc.tiotecpreventivo.repository.RecordatorioRepository
import cl.duoc.tiotecpreventivo.repository.ResultadoCalculado
import cl.duoc.tiotecpreventivo.repository.ResultadoEliminacion
import cl.duoc.tiotecpreventivo.repository.SolicitudRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TT-07: CRUD, reglas y transacciones sobre una base Room en memoria.
 * Ejecutar con un emulador encendido: clic derecho → Run 'RepositoriosRoomTest'.
 */
@RunWith(AndroidJUnit4::class)
class RepositoriosRoomTest {

    private lateinit var db: TioTecDatabase
    private var ahora = 1_000_000L
    private val reloj = { ahora }

    private lateinit var perfiles: PerfilRepository
    private lateinit var dispositivos: DispositivoRepository
    private lateinit var evaluaciones: EvaluacionRepository
    private lateinit var acciones: AccionRepository
    private lateinit var recordatorios: RecordatorioRepository
    private lateinit var solicitudes: SolicitudRepository

    @Before
    fun crearBase() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TioTecDatabase::class.java
        ).build()
        perfiles = PerfilRepository(db.perfilDao(), reloj)
        dispositivos = DispositivoRepository(db.dispositivoDao(), reloj)
        evaluaciones = EvaluacionRepository(
            db.evaluacionDao(), db.dispositivoDao(), CatalogoRepositoryLocal(), reloj
        )
        acciones = AccionRepository(db.accionDao(), reloj)
        recordatorios = RecordatorioRepository(db.recordatorioDao(), reloj)
        solicitudes = SolicitudRepository(db.solicitudDao(), reloj)
    }

    @After
    fun cerrarBase() = db.close()

    // ---------- Ayudas ----------

    private fun todasLasRespuestas(valor: ValorRespuesta) =
        CatalogoLocal.preguntas.associate { it.id to valor }

    private fun resultados(nivel: NivelRiesgo) = Categoria.entries.map {
        ResultadoCalculado(it, puntaje = 0, nivel = nivel, explicacion = "Prueba")
    }

    private suspend fun crearPerfilYDispositivo(nombre: String = "Teléfono de prueba"): Pair<Long, Long> {
        val perfilId = perfiles.guardar("Tío Prueba", ExperienciaDigital.PRINCIPIANTE)
        val dispositivoId = dispositivos.crear(perfilId, nombre, SistemaOperativo.ANDROID)
        return perfilId to dispositivoId
    }

    // ---------- Perfil ----------

    @Test
    fun perfil_seCreaUnaVezYLuegoSeActualiza(): Unit = runBlocking {
        val id1 = perfiles.guardar("  Tío Prueba ", ExperienciaDigital.PRINCIPIANTE)
        val id2 = perfiles.guardar("Tía Demo", ExperienciaDigital.AVANZADO)

        assertEquals(id1, id2)
        val perfil = perfiles.observarPerfil().first()
        assertEquals("Tía Demo", perfil?.alias)
        assertEquals(ExperienciaDigital.AVANZADO, perfil?.experienciaDigital)
    }

    // ---------- Dispositivos: CRUD ----------

    @Test
    fun dispositivo_crudCompleto(): Unit = runBlocking {
        val (perfilId, id) = crearPerfilYDispositivo()
        dispositivos.crear(perfilId, "Computador de prueba", SistemaOperativo.WINDOWS)

        // Leer: ordenados por nombre
        assertEquals(
            listOf("Computador de prueba", "Teléfono de prueba"),
            dispositivos.observarActivos().first().map { it.nombre }
        )

        // Nombre repetido entre activos, sin importar mayúsculas ni espacios
        assertTrue(dispositivos.existeNombreActivo("  teléfono DE prueba "))
        assertFalse(dispositivos.existeNombreActivo("Teléfono de prueba", excluirId = id))

        // Actualizar
        assertTrue(dispositivos.actualizar(id, "Tablet de prueba", SistemaOperativo.IOS))
        val editado = dispositivos.obtener(id)!!
        assertEquals("Tablet de prueba", editado.nombre)
        assertEquals(SistemaOperativo.IOS, editado.sistemaOperativo)

        // Eliminar sin evaluaciones: se borra y entrega la imagen para borrar el archivo
        dispositivos.actualizarImagen(id, "imagenes/disp_1.jpg")
        val resultado = dispositivos.eliminar(id)
        assertEquals(ResultadoEliminacion.Eliminado("imagenes/disp_1.jpg"), resultado)
        assertNull(dispositivos.obtener(id))
        assertEquals(ResultadoEliminacion.NoEncontrado, dispositivos.eliminar(id))
    }

    @Test
    fun dispositivo_conEvaluaciones_seArchivaYConservaElHistorial(): Unit = runBlocking {
        val (_, id) = crearPerfilYDispositivo("Teléfono de la abuela")
        evaluaciones.guardarEvaluacion(id, todasLasRespuestas(ValorRespuesta.NO), resultados(NivelRiesgo.RIESGO_ALTO))
        ahora += 1
        recordatorios.crearProximaEvaluacion(id, "Volver a evaluar", fechaHora = ahora + 60_000)

        // Editar el dispositivo no cambia la copia guardada en la evaluación
        dispositivos.actualizar(id, "Nombre nuevo", SistemaOperativo.OTRO)

        assertEquals(ResultadoEliminacion.Archivado, dispositivos.eliminar(id))

        assertTrue("No debe aparecer en Mis dispositivos", dispositivos.observarActivos().first().isEmpty())
        assertTrue("El registro sigue existiendo", dispositivos.obtener(id)!!.archivado)

        val historial = evaluaciones.observarHistorial().first()
        assertEquals(1, historial.size)
        assertEquals("Teléfono de la abuela", historial.first().nombreDispositivo)
        assertEquals(SistemaOperativo.ANDROID, historial.first().sistemaOperativoDispositivo)

        assertTrue("Sus recordatorios se cancelan", recordatorios.observarActivos().first().isEmpty())

        // Un dispositivo archivado no se puede volver a evaluar
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                evaluaciones.guardarEvaluacion(id, todasLasRespuestas(ValorRespuesta.SI), resultados(NivelRiesgo.RIESGO_BAJO))
            }
        }
    }

    @Test
    fun archivar_cancelaRecordatoriosDeAmbosTiposYConservaElHistorial(): Unit = runBlocking {
        val (perfilId, id) = crearPerfilYDispositivo("Teléfono de la abuela")
        val otroId = dispositivos.crear(perfilId, "Computador de prueba", SistemaOperativo.WINDOWS)

        val evaluacionId = evaluaciones.guardarEvaluacion(
            id, todasLasRespuestas(ValorRespuesta.NO), resultados(NivelRiesgo.RIESGO_ALTO)
        )
        val otraEvaluacionId = evaluaciones.guardarEvaluacion(
            otroId, todasLasRespuestas(ValorRespuesta.NO), resultados(NivelRiesgo.RIESGO_ALTO)
        )
        val accionesDelDispositivo = evaluaciones.obtenerDetalle(evaluacionId)!!.acciones
        val accionDelOtro = evaluaciones.obtenerDetalle(otraEvaluacionId)!!.acciones.first()

        val futuro = ahora + 3_600_000
        // Del dispositivo que se archivará: ambos tipos.
        val deAccion = recordatorios.crearParaAccion(accionesDelDispositivo[0].id, "Revisar acción", futuro)
        val deProximaEvaluacion = recordatorios.crearProximaEvaluacion(id, "Volver a evaluar", futuro)
        // Ya enviado: debe quedar como ENVIADO, no como CANCELADO.
        val yaEnviado = recordatorios.crearParaAccion(accionesDelDispositivo[1].id, "Ya avisado", futuro)
        recordatorios.marcarEnviado(yaEnviado)
        // Del otro dispositivo: no se deben tocar.
        val otroDeAccion = recordatorios.crearParaAccion(accionDelOtro.id, "Acción del otro", futuro)
        val otroDeProxima = recordatorios.crearProximaEvaluacion(otroId, "Evaluar el otro", futuro)

        assertEquals(ResultadoEliminacion.Archivado, dispositivos.eliminar(id))

        // Ambos tipos de recordatorio del dispositivo archivado quedan cancelados.
        assertEquals(EstadoRecordatorio.CANCELADO, recordatorios.obtener(deAccion)!!.estado)
        assertEquals(EstadoRecordatorio.CANCELADO, recordatorios.obtener(deProximaEvaluacion)!!.estado)
        assertEquals(EstadoRecordatorio.ENVIADO, recordatorios.obtener(yaEnviado)!!.estado)

        // Los del otro dispositivo siguen programados.
        assertEquals(EstadoRecordatorio.PROGRAMADO, recordatorios.obtener(otroDeAccion)!!.estado)
        assertEquals(EstadoRecordatorio.PROGRAMADO, recordatorios.obtener(otroDeProxima)!!.estado)
        assertEquals(
            setOf(otroDeAccion, otroDeProxima),
            recordatorios.observarActivos().first().map { it.id }.toSet()
        )

        // La evaluación y su historial se conservan completos.
        val detalle = evaluaciones.obtenerDetalle(evaluacionId)
        assertNotNull("La evaluación debe seguir existiendo", detalle)
        assertEquals("Teléfono de la abuela", detalle!!.evaluacion.nombreDispositivo)
        assertEquals(CatalogoLocal.preguntas.size, detalle.respuestas.size)
        assertEquals(Categoria.entries.size, detalle.resultados.size)
        assertEquals(accionesDelDispositivo.map { it.id }, detalle.acciones.map { it.id })
        assertEquals(2, evaluaciones.observarHistorial().first().size)
    }

    // ---------- Evaluaciones ----------

    @Test
    fun evaluacion_guardaRespuestasResultadosYAcciones(): Unit = runBlocking {
        val (_, id) = crearPerfilYDispositivo()
        val resultadosMixtos = listOf(
            ResultadoCalculado(Categoria.RESPALDOS, 2, NivelRiesgo.RIESGO_BAJO, "a"),
            ResultadoCalculado(Categoria.ACTUALIZACIONES, 4, NivelRiesgo.RIESGO_MEDIO, "b"),
            ResultadoCalculado(Categoria.AUTENTICACION, 6, NivelRiesgo.RIESGO_ALTO, "c"),
            ResultadoCalculado(Categoria.RECUPERACION, 0, NivelRiesgo.RIESGO_BAJO, "d")
        )

        val evaluacionId = evaluaciones.guardarEvaluacion(id, todasLasRespuestas(ValorRespuesta.NO), resultadosMixtos)
        val detalle = evaluaciones.obtenerDetalle(evaluacionId)!!

        assertEquals(NivelRiesgo.RIESGO_ALTO, detalle.evaluacion.nivelGlobal)
        assertEquals(CatalogoLocal.preguntas.size, detalle.respuestas.size)
        assertTrue(detalle.respuestas.all { it.puntaje == 2 && it.textoPregunta.isNotBlank() })
        assertEquals(4, detalle.resultados.size)
        assertEquals(CatalogoLocal.recomendaciones.size, detalle.acciones.size)
        assertEquals(CatalogoLocal.recomendaciones.size, acciones.contarPendientes().first())

        // Abrir el detalle otra vez no crea otra evaluación
        evaluaciones.obtenerDetalle(evaluacionId)
        assertEquals(1, evaluaciones.observarHistorial().first().size)
    }

    @Test
    fun evaluacion_sinRiesgo_noCreaAcciones(): Unit = runBlocking {
        val (_, id) = crearPerfilYDispositivo()
        val evaluacionId = evaluaciones.guardarEvaluacion(id, todasLasRespuestas(ValorRespuesta.SI), resultados(NivelRiesgo.RIESGO_BAJO))
        assertTrue(evaluaciones.obtenerDetalle(evaluacionId)!!.acciones.isEmpty())
    }

    @Test
    fun evaluacion_conRespuestasFaltantes_seRechaza() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                val (_, id) = crearPerfilYDispositivo()
                evaluaciones.guardarEvaluacion(id, mapOf("AUT-01" to ValorRespuesta.NO), resultados(NivelRiesgo.RIESGO_ALTO))
            }
        }
    }

    @Test
    fun evaluacion_siFallaLaTransaccion_noQuedaNadaGuardado(): Unit = runBlocking {
        val (_, id) = crearPerfilYDispositivo()
        val accionRepetida = AccionEntity(
            recomendacionId = "REC-AUT-01", titulo = "x", descripcion = "x",
            prioridad = Prioridad.ALTA, fechaRevision = ahora, actualizadoEn = ahora
        )
        val evaluacion = EvaluacionEntity(
            dispositivoId = id, nombreDispositivo = "x",
            sistemaOperativoDispositivo = SistemaOperativo.ANDROID,
            fecha = ahora, nivelGlobal = NivelRiesgo.RIESGO_ALTO
        )

        var fallo = false
        try {
            // La misma recomendación dos veces viola el índice único → la transacción se revierte.
            db.evaluacionDao().guardarCompleta(evaluacion, emptyList(), emptyList(), listOf(accionRepetida, accionRepetida))
        } catch (e: Exception) {
            fallo = true
        }

        assertTrue("Debió fallar por la acción repetida", fallo)
        assertTrue("No debe quedar la evaluación a medias", evaluaciones.observarHistorial().first().isEmpty())
        assertEquals(0, acciones.contarPendientes().first())
    }

    @Test
    fun historial_muestraLaMasRecientePrimero(): Unit = runBlocking {
        val (_, id) = crearPerfilYDispositivo()
        val primera = evaluaciones.guardarEvaluacion(id, todasLasRespuestas(ValorRespuesta.NO), resultados(NivelRiesgo.RIESGO_ALTO))
        ahora += 86_400_000
        val segunda = evaluaciones.guardarEvaluacion(id, todasLasRespuestas(ValorRespuesta.SI), resultados(NivelRiesgo.RIESGO_BAJO))

        assertEquals(listOf(segunda, primera), evaluaciones.observarHistorial().first().map { it.id })
        assertEquals(segunda, evaluaciones.observarUltima().first()?.id)
    }

    // ---------- Acciones ----------

    @Test
    fun acciones_ordenadasPorPrioridadYCambioDeEstado(): Unit = runBlocking {
        val (_, id) = crearPerfilYDispositivo()
        evaluaciones.guardarEvaluacion(id, todasLasRespuestas(ValorRespuesta.NO), resultados(NivelRiesgo.RIESGO_ALTO))

        val pendientes = acciones.observarPorEstado(EstadoAccion.PENDIENTE).first()
        val prioridades = pendientes.map { it.prioridad.ordinal }
        assertEquals("Deben venir ALTA → MEDIA → BAJA", prioridades.sorted(), prioridades)

        val accion = pendientes.first()
        ahora += 5_000
        assertTrue(acciones.cambiarEstado(accion.id, EstadoAccion.COMPLETADA))
        assertEquals(ahora, acciones.obtener(accion.id)!!.completadaEn)
        assertEquals(pendientes.size - 1, acciones.contarPendientes().first())

        assertTrue(acciones.cambiarEstado(accion.id, EstadoAccion.PENDIENTE))
        assertNull("Al volver a pendiente se borra la fecha", acciones.obtener(accion.id)!!.completadaEn)
    }

    // ---------- Recordatorios ----------

    @Test
    fun recordatorios_crearModificarYCancelar(): Unit = runBlocking {
        val (_, id) = crearPerfilYDispositivo()
        val evaluacionId = evaluaciones.guardarEvaluacion(id, todasLasRespuestas(ValorRespuesta.NO), resultados(NivelRiesgo.RIESGO_ALTO))
        val accionId = evaluaciones.obtenerDetalle(evaluacionId)!!.acciones.first().id

        val recordatorioId = recordatorios.crearParaAccion(accionId, "Revisar acción", ahora + 3_600_000)
        assertTrue(recordatorios.modificar(recordatorioId, "Revisar mañana", ahora + 86_400_000))
        assertEquals("Revisar mañana", recordatorios.obtener(recordatorioId)!!.titulo)
        assertEquals(1, recordatorios.observarActivos().first().size)

        assertTrue(recordatorios.cancelar(recordatorioId))
        assertTrue(recordatorios.observarActivos().first().isEmpty())
        assertFalse("Un cancelado no se modifica", recordatorios.modificar(recordatorioId, "x", ahora + 10))
    }

    @Test
    fun recordatorio_enElPasado_seRechaza() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                val (_, id) = crearPerfilYDispositivo()
                recordatorios.crearProximaEvaluacion(id, "Tarde", fechaHora = ahora - 1)
            }
        }
    }

    // ---------- Solicitudes ----------

    @Test
    fun solicitudes_codigoCorrelativoEHistorialDeEstados(): Unit = runBlocking {
        val (perfilId, _) = crearPerfilYDispositivo()
        val primera = solicitudes.crear(perfilId, Categoria.RESPALDOS)
        val segunda = solicitudes.crear(perfilId, Categoria.AUTENTICACION)

        assertEquals("DEMO-001", solicitudes.obtenerConHistorial(primera)!!.solicitud.codigo)
        assertEquals("DEMO-002", solicitudes.obtenerConHistorial(segunda)!!.solicitud.codigo)

        ahora += 1_000
        assertTrue(solicitudes.cambiarEstado(primera, EstadoSolicitud.EN_REVISION))
        val conHistorial = solicitudes.obtenerConHistorial(primera)
        assertNotNull(conHistorial)
        assertEquals(EstadoSolicitud.EN_REVISION, conHistorial!!.solicitud.estado)
        assertEquals(
            listOf(EstadoSolicitud.PENDIENTE, EstadoSolicitud.EN_REVISION),
            conHistorial.historial.sortedBy { it.fecha }.map { it.estado }
        )
        assertFalse(solicitudes.cambiarEstado(999, EstadoSolicitud.RESUELTA))
    }
}
