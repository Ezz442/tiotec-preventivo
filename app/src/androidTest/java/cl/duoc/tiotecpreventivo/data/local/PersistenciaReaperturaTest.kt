package cl.duoc.tiotecpreventivo.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.data.catalogo.CatalogoLocal
import cl.duoc.tiotecpreventivo.data.catalogo.CatalogoRepositoryLocal
import cl.duoc.tiotecpreventivo.data.catalogo.ValorRespuesta
import cl.duoc.tiotecpreventivo.data.local.entity.ExperienciaDigital
import cl.duoc.tiotecpreventivo.data.local.entity.NivelRiesgo
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo
import cl.duoc.tiotecpreventivo.repository.DispositivoRepository
import cl.duoc.tiotecpreventivo.repository.EvaluacionRepository
import cl.duoc.tiotecpreventivo.repository.PerfilRepository
import cl.duoc.tiotecpreventivo.repository.ResultadoCalculado
import cl.duoc.tiotecpreventivo.repository.SolicitudRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TT-07: los datos guardados en el archivo SQLite siguen ahí después de cerrar
 * y volver a abrir la base, como cuando se cierra y se reabre la app.
 */
@RunWith(AndroidJUnit4::class)
class PersistenciaReaperturaTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val nombreBase = "tiotec_prueba_reapertura.db"

    @Before
    @After
    fun limpiar() {
        context.deleteDatabase(nombreBase)
    }

    @Test
    fun datosSobrevivenAlCerrarYReabrir(): Unit = runBlocking {
        // Primera "apertura de la app": se guardan datos.
        val primera = TioTecDatabase.construir(context, nombreBase)
        val perfilId = PerfilRepository(primera.perfilDao())
            .guardar("Tío Prueba", ExperienciaDigital.INTERMEDIO)
        val dispositivoId = DispositivoRepository(primera.dispositivoDao())
            .crear(perfilId, "Teléfono de prueba", SistemaOperativo.ANDROID)
        EvaluacionRepository(
            primera.evaluacionDao(), primera.dispositivoDao(), CatalogoRepositoryLocal()
        ).guardarEvaluacion(
            dispositivoId,
            CatalogoLocal.preguntas.associate { it.id to ValorRespuesta.NO },
            Categoria.entries.map { ResultadoCalculado(it, 6, NivelRiesgo.RIESGO_ALTO, "Prueba") }
        )
        SolicitudRepository(primera.solicitudDao()).crear(perfilId, Categoria.RECUPERACION)
        primera.close()

        // Segunda "apertura": se leen desde el mismo archivo.
        val segunda = TioTecDatabase.construir(context, nombreBase)
        try {
            assertEquals("Tío Prueba", segunda.perfilDao().obtener()?.alias)
            assertEquals(
                listOf("Teléfono de prueba"),
                segunda.dispositivoDao().observarActivos().first().map { it.nombre }
            )
            assertEquals(1, segunda.evaluacionDao().observarHistorial().first().size)
            assertEquals(
                CatalogoLocal.recomendaciones.size,
                segunda.accionDao().contarPendientes().first()
            )
            assertEquals(
                listOf("DEMO-001"),
                segunda.solicitudDao().observarTodas().first().map { it.codigo }
            )
        } finally {
            segunda.close()
        }
    }
}
