package cl.duoc.tiotecpreventivo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AzulClaro,
    onPrimary = Color(0xFF003258),
    secondary = GrisAzuladoClaro,
    onSecondary = Color(0xFF243349),
    tertiary = VerdeClaro,
    onTertiary = Color(0xFF073730),
    background = FondoOscuro,
    onBackground = TextoClaro,
    surface = SuperficieOscura,
    onSurface = TextoClaro
)

private val LightColorScheme = lightColorScheme(
    primary = AzulPrincipal,
    onPrimary = Color.White,
    secondary = GrisAzulado,
    onSecondary = Color.White,
    tertiary = VerdePrincipal,
    onTertiary = Color.White,
    background = FondoClaro,
    onBackground = TextoOscuro,
    surface = Color.White,
    onSurface = TextoOscuro
)

@Composable
fun TioTecPreventivoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}