package cl.duoc.tiotecpreventivo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import cl.duoc.tiotecpreventivo.ui.navigation.TioTecApp
import cl.duoc.tiotecpreventivo.ui.theme.TioTecPreventivoTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)

            TioTecPreventivoTheme {
                TioTecApp(
                    windowWidthSizeClass = windowSizeClass.widthSizeClass
                )
            }
        }
    }
}