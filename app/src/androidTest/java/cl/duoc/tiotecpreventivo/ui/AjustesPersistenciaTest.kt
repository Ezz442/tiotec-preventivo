package cl.duoc.tiotecpreventivo.ui

import androidx.compose.ui.test.and
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.tiotecpreventivo.MainActivity
import cl.duoc.tiotecpreventivo.TioTecApplication
import cl.duoc.tiotecpreventivo.data.preferences.Tema
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TT-08: Ajustes se abre desde Perfil y el tema elegido se conserva al cerrar y
 * volver a abrir la app. Requiere un emulador encendido.
 */
@RunWith(AndroidJUnit4::class)
class AjustesPersistenciaTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val preferencias =
        ApplicationProvider.getApplicationContext<TioTecApplication>().contenedor.preferenciasRepository

    @Before
    @After
    fun volverAlTemaDelSistema() = runBlocking {
        preferencias.setTema(Tema.SISTEMA)
    }

    /** Espera a que la app termine de leer DataStore y dibuje la navegación. */
    private fun esperarTexto(texto: String) {
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty()
        }
    }

    /** DataStore guarda en segundo plano: se espera a que la pantalla refleje el valor. */
    private fun esperarSeleccionado(texto: String) {
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodes(hasText(texto) and isSelected()).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun abrirAjustesDesdePerfil() {
        esperarTexto("Perfil")
        // En tablet "Perfil" aparece en la barra y en el menú lateral: basta con el primero.
        compose.onAllNodesWithText("Perfil").onFirst().performClick()
        esperarTexto("Ajustes")
        compose.onNodeWithText("Ajustes").performClick()
        esperarTexto("Apariencia")
    }

    @Test
    fun temaElegidoSeConservaAlCerrarYAbrirLaApp() {
        // Primera apertura: el usuario elige el tema oscuro y cierra la app.
        ActivityScenario.launch(MainActivity::class.java).use {
            abrirAjustesDesdePerfil()
            esperarSeleccionado(Tema.SISTEMA.etiqueta)

            compose.onNodeWithText(Tema.OSCURO.etiqueta).performClick()
            esperarSeleccionado(Tema.OSCURO.etiqueta)
        }

        // Segunda apertura: la selección sigue ahí.
        ActivityScenario.launch(MainActivity::class.java).use {
            abrirAjustesDesdePerfil()
            esperarSeleccionado(Tema.OSCURO.etiqueta)
            compose.onNodeWithText(Tema.OSCURO.etiqueta).assertIsSelected()
            compose.onNodeWithText(Tema.SISTEMA.etiqueta).assertIsNotSelected()
        }
    }
}
