package cl.duoc.tiotecpreventivo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.duoc.tiotecpreventivo.ui.screens.InicioScreen
import cl.duoc.tiotecpreventivo.ui.theme.TioTecPreventivoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TioTecPreventivoTheme {
                InicioScreen()
            }
        }
    }
}