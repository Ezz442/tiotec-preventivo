package cl.duoc.tiotecpreventivo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cl.duoc.tiotecpreventivo.ui.navigation.TioTecApp
import cl.duoc.tiotecpreventivo.ui.theme.TioTecPreventivoTheme
import cl.duoc.tiotecpreventivo.viewmodel.PreferenciasViewModel

class MainActivity : ComponentActivity() {

    /** Una sola instancia por pantalla: decide el tema y la usa también Ajustes (TT-08). */
    private val preferenciasViewModel: PreferenciasViewModel by viewModels {
        PreferenciasViewModel.Factory(
            (application as TioTecApplication).contenedor.preferenciasRepository
        )
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val preferencias by preferenciasViewModel.uiState.collectAsState()

            // Se espera a leer DataStore para no mostrar un instante el tema equivocado.
            if (!preferencias.cargando) {
                val modoOscuro = preferencias.tema.usarModoOscuro(isSystemInDarkTheme())

                // La barra de estado sigue el tema elegido, no solo el del teléfono.
                DisposableEffect(modoOscuro) {
                    val estilo = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { modoOscuro }
                    enableEdgeToEdge(statusBarStyle = estilo, navigationBarStyle = estilo)
                    onDispose {}
                }

                TioTecPreventivoTheme(darkTheme = modoOscuro) {
                    TioTecApp(
                        windowWidthSizeClass = windowSizeClass.widthSizeClass,
                        preferenciasViewModel = preferenciasViewModel
                    )
                }
            }
        }
    }
}
