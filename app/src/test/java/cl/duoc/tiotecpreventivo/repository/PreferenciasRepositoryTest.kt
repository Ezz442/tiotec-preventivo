package cl.duoc.tiotecpreventivo.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import cl.duoc.tiotecpreventivo.data.preferences.Preferencias
import cl.duoc.tiotecpreventivo.data.preferences.Tema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * TT-08: las preferencias se guardan con DataStore y se recuperan al reabrir.
 * Corre en la JVM (sin emulador): usa un archivo temporal en lugar del almacenamiento del teléfono.
 */
class PreferenciasRepositoryTest {

    @get:Rule
    val carpeta = TemporaryFolder()

    private val abiertos = mutableListOf<CoroutineScope>()

    private val archivo: File by lazy { File(carpeta.root, "prueba.preferences_pb") }

    /** Simula abrir la app: crea un DataStore nuevo sobre el mismo archivo. */
    private fun abrir(): Pair<PreferenciasRepository, CoroutineScope> {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        abiertos += scope
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { archivo }
        )
        return PreferenciasRepository(dataStore) to scope
    }

    /** Simula cerrar la app: detiene el DataStore para poder abrir otro sobre el mismo archivo. */
    private suspend fun cerrar(scope: CoroutineScope) {
        scope.coroutineContext[kotlinx.coroutines.Job]!!.cancelAndJoin()
        abiertos -= scope
    }

    @After
    fun cerrarTodo() = runBlocking {
        abiertos.toList().forEach { cerrar(it) }
    }

    @Test
    fun sinDatosGuardados_usaLosValoresPorDefecto(): Unit = runBlocking {
        val (repositorio, _) = abrir()
        assertEquals(Preferencias(), repositorio.preferencias.first())
    }

    @Test
    fun guardaYLeeCadaPreferencia(): Unit = runBlocking {
        val (repositorio, _) = abrir()

        repositorio.setTema(Tema.OSCURO)
        repositorio.setAvisosActivos(false)
        repositorio.marcarPrimeraEjecucionCompleta()

        val leidas = repositorio.preferencias.first()
        assertEquals(Tema.OSCURO, leidas.tema)
        assertFalse(leidas.avisosActivos)
        assertFalse(leidas.primeraEjecucion)
    }

    @Test
    fun preferenciasSobrevivenAlCerrarYReabrir(): Unit = runBlocking {
        // Primera apertura: el usuario cambia sus ajustes y cierra la app.
        val (primera, scopePrimera) = abrir()
        primera.setTema(Tema.CLARO)
        primera.setAvisosActivos(false)
        primera.marcarPrimeraEjecucionCompleta()
        cerrar(scopePrimera)

        // Segunda apertura: se leen desde el mismo archivo.
        val (segunda, _) = abrir()
        assertEquals(
            Preferencias(tema = Tema.CLARO, primeraEjecucion = false, avisosActivos = false),
            segunda.preferencias.first()
        )
    }

    @Test
    fun cambiarUnaPreferenciaNoAlteraLasDemas(): Unit = runBlocking {
        val (repositorio, _) = abrir()
        repositorio.setAvisosActivos(false)
        repositorio.setTema(Tema.OSCURO)

        val leidas = repositorio.preferencias.first()
        assertFalse(leidas.avisosActivos)
        assertTrue("No se marcó la bienvenida como vista", leidas.primeraEjecucion)
    }

    @Test
    fun temaDesconocido_vuelveAlDelSistema(): Unit = runBlocking {
        val (_, scope) = abrir()
        cerrar(scope)

        // Escribe un valor que no existe en el enum, como si viniera de otra versión.
        val scopeCrudo = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val crudo = PreferenceDataStoreFactory.create(scope = scopeCrudo, produceFile = { archivo })
        crudo.edit { it[stringPreferencesKey("tema")] = "AZUL" }
        scopeCrudo.coroutineContext[kotlinx.coroutines.Job]!!.cancelAndJoin()

        val (repositorio, _) = abrir()
        assertEquals(Tema.SISTEMA, repositorio.preferencias.first().tema)
    }

    @Test
    fun temaDecideElModoOscuro() {
        assertTrue(Tema.SISTEMA.usarModoOscuro(sistemaEnModoOscuro = true))
        assertFalse(Tema.SISTEMA.usarModoOscuro(sistemaEnModoOscuro = false))
        assertFalse(Tema.CLARO.usarModoOscuro(sistemaEnModoOscuro = true))
        assertTrue(Tema.OSCURO.usarModoOscuro(sistemaEnModoOscuro = false))
    }
}
