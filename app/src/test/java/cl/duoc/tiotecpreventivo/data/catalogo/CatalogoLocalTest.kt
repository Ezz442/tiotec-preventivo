package cl.duoc.tiotecpreventivo.data.catalogo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * TT-06: comprueba que el catálogo ficticio es coherente y que la app no depende de la red.
 */
class CatalogoLocalTest {

    private val repositorio = CatalogoRepositoryLocal()

    // ---------- Integridad de los datos ----------

    @Test
    fun idsSonUnicos() {
        val ids = CatalogoLocal.preguntas.map { it.id } +
            CatalogoLocal.recomendaciones.map { it.id } +
            CatalogoLocal.contenidos.map { it.id }
        assertEquals("Hay IDs repetidos", ids.size, ids.toSet().size)
    }

    @Test
    fun idsSiguenLaConvencion() {
        val prefijos = mapOf(
            Categoria.RESPALDOS to "RES",
            Categoria.ACTUALIZACIONES to "ACT",
            Categoria.AUTENTICACION to "AUT",
            Categoria.RECUPERACION to "RCU"
        )
        CatalogoLocal.preguntas.forEach {
            assertTrue(it.id, it.id.matches(Regex("${prefijos[it.categoria]}-\\d{2}")))
        }
        CatalogoLocal.contenidos.forEach {
            assertTrue(it.id, it.id.matches(Regex("CON-${prefijos[it.categoria]}-\\d{2}")))
        }
        CatalogoLocal.recomendaciones.forEach {
            assertEquals(it.id, "REC-${it.preguntaId}", it.id)
        }
    }

    @Test
    fun cadaCategoriaTienePreguntasYContenidos() {
        Categoria.entries.forEach { categoria ->
            assertTrue("$categoria sin preguntas", repositorio.preguntasDe(categoria).isNotEmpty())
            assertTrue("$categoria sin contenidos", repositorio.contenidos(categoria).isNotEmpty())
        }
    }

    @Test
    fun cadaPreguntaTienePuntajeParaTodasLasRespuestas() {
        CatalogoLocal.preguntas.forEach { pregunta ->
            assertEquals(pregunta.id, ValorRespuesta.entries.toSet(), pregunta.puntajes.keys)
            assertEquals(
                "En ${pregunta.id} 'Sí' debe ser la buena práctica",
                0, pregunta.puntajes.getValue(ValorRespuesta.SI)
            )
        }
    }

    @Test
    fun puntajesSubenDeSiANoLoSeANo() {
        CatalogoLocal.preguntas.forEach { pregunta ->
            val p = pregunta.puntajes
            assertTrue(pregunta.id, p.getValue(ValorRespuesta.SI) < p.getValue(ValorRespuesta.NO_SE))
            assertTrue(pregunta.id, p.getValue(ValorRespuesta.NO_SE) < p.getValue(ValorRespuesta.NO))
        }
    }

    @Test
    fun practicasClavePesanMasYTienenRecomendacionDePrioridadAlta() {
        Categoria.entries.forEach { categoria ->
            assertTrue("$categoria sin práctica clave", repositorio.preguntasDe(categoria).any { it.clave })
        }
        CatalogoLocal.preguntas.forEach { pregunta ->
            val esperado = if (pregunta.clave) CatalogoLocal.PUNTAJES_CLAVE else CatalogoLocal.PUNTAJES_NORMAL
            assertEquals(pregunta.id, esperado, pregunta.puntajes)

            val prioridad = CatalogoLocal.recomendaciones.single { it.preguntaId == pregunta.id }.prioridad
            assertEquals(
                "${pregunta.id}: clave ⇔ recomendación de prioridad alta",
                pregunta.clave, prioridad == Prioridad.ALTA
            )
        }
    }

    @Test
    fun ordenDePreguntasEsUnicoDentroDeCadaCategoria() {
        Categoria.entries.forEach { categoria ->
            val ordenes = repositorio.preguntasDe(categoria).map { it.orden }
            assertEquals("$categoria tiene órdenes repetidos", ordenes.size, ordenes.toSet().size)
        }
    }

    @Test
    fun cadaPreguntaTieneUnaRecomendacion() {
        val preguntasConRecomendacion = CatalogoLocal.recomendaciones.map { it.preguntaId }.toSet()
        CatalogoLocal.preguntas.forEach {
            assertTrue("${it.id} sin recomendación", it.id in preguntasConRecomendacion)
        }
    }

    @Test
    fun recomendacionesApuntanAPreguntasYContenidosExistentes() {
        CatalogoLocal.recomendaciones.forEach { recomendacion ->
            val pregunta = repositorio.pregunta(recomendacion.preguntaId)
            assertNotNull("${recomendacion.id}: pregunta inexistente", pregunta)
            assertFalse("${recomendacion.id}: no se activa con ninguna respuesta",
                recomendacion.valoresQueActivan.isEmpty())
            assertFalse("${recomendacion.id}: no debe activarse con 'Sí'",
                ValorRespuesta.SI in recomendacion.valoresQueActivan)
            assertTrue(recomendacion.id, recomendacion.diasParaRevision > 0)

            recomendacion.contenidoId?.let { contenidoId ->
                val contenido = repositorio.contenido(contenidoId)
                assertNotNull("${recomendacion.id}: contenido $contenidoId inexistente", contenido)
                assertEquals("${recomendacion.id}: contenido de otra categoría",
                    pregunta!!.categoria, contenido!!.categoria)
            }
        }
    }

    @Test
    fun textosNoEstanVacios() {
        CatalogoLocal.preguntas.forEach {
            assertTrue(it.id, it.texto.isNotBlank() && it.ayuda.isNotBlank())
        }
        CatalogoLocal.recomendaciones.forEach {
            assertTrue(it.id, it.titulo.isNotBlank() && it.descripcion.isNotBlank())
        }
        CatalogoLocal.contenidos.forEach {
            assertTrue(it.id, it.titulo.isNotBlank() && it.resumen.isNotBlank() && it.cuerpo.isNotBlank())
        }
    }

    // ---------- Comportamiento del repositorio ----------

    @Test
    fun preguntasVienenOrdenadasPorCategoriaYOrden() {
        val preguntas = repositorio.preguntas()
        val esperado = preguntas.sortedWith(
            compareBy<Pregunta> { it.categoria.ordinal }.thenBy { it.orden }
        )
        assertEquals(esperado, preguntas)
        assertEquals(CatalogoLocal.preguntas.size, preguntas.size)
    }

    @Test
    fun todasLasRespuestasSiNoGeneranRecomendaciones() {
        val respuestas = CatalogoLocal.preguntas.associate { it.id to ValorRespuesta.SI }
        assertTrue(repositorio.recomendacionesPara(respuestas).isEmpty())
    }

    @Test
    fun respuestasNoGeneranTodasLasRecomendacionesOrdenadasPorPrioridad() {
        val respuestas = CatalogoLocal.preguntas.associate { it.id to ValorRespuesta.NO }
        val recomendaciones = repositorio.recomendacionesPara(respuestas)

        assertEquals(CatalogoLocal.recomendaciones.size, recomendaciones.size)
        val prioridades = recomendaciones.map { it.prioridad.ordinal }
        assertEquals("No vienen ALTA → MEDIA → BAJA", prioridades.sorted(), prioridades)
    }

    @Test
    fun noLoSeTambienActivaRecomendacion() {
        val recomendaciones = repositorio.recomendacionesPara(mapOf("AUT-02" to ValorRespuesta.NO_SE))
        assertEquals(listOf("REC-AUT-02"), recomendaciones.map { it.id })
    }

    @Test
    fun respuestasDePreguntasDesconocidasSeIgnoran() {
        val recomendaciones = repositorio.recomendacionesPara(mapOf("XXX-99" to ValorRespuesta.NO))
        assertTrue(recomendaciones.isEmpty())
    }

    @Test
    fun busquedasPorIdInexistenteDevuelvenNull() {
        assertNull(repositorio.pregunta("XXX-99"))
        assertNull(repositorio.contenido("CON-XXX-99"))
    }

    @Test
    fun contenidosSinFiltroDevuelvenTodos() {
        assertEquals(CatalogoLocal.contenidos, repositorio.contenidos())
        assertEquals(
            CatalogoLocal.contenidos.size,
            Categoria.entries.sumOf { repositorio.contenidos(it).size }
        )
    }

    @Test
    fun reabrirLaAppNoDuplicaElCatalogo() {
        // Simula dos aperturas: cada una crea su propio repositorio.
        val primeraApertura = CatalogoRepositoryLocal()
        val segundaApertura = CatalogoRepositoryLocal()
        assertEquals(primeraApertura.preguntas(), segundaApertura.preguntas())
        assertEquals(primeraApertura.contenidos(), segundaApertura.contenidos())
        assertEquals(CatalogoLocal.preguntas.size, segundaApertura.preguntas().size)
    }

    // ---------- Uso sin conexión ----------

    @Test
    fun manifiestoNoPideAccesoAInternet() {
        // Las pruebas unitarias se ejecutan desde la carpeta del módulo app.
        val manifiesto = File("src/main/AndroidManifest.xml")
        assertTrue("No se encontró el manifiesto", manifiesto.exists())
        assertFalse(
            "La app no debe declarar el permiso INTERNET en EP2",
            manifiesto.readText().contains("android.permission.INTERNET")
        )
    }
}
