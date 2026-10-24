package cl.duoc.tiotecpreventivo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/*
 * TT-27: esquemas completos. Además de primary/secondary/tertiary se definen los
 * contenedores (selecciones y tarjetas destacadas), surfaceVariant/onSurfaceVariant,
 * los contornos y los surfaceContainer* que usan tarjetas, barras y diálogos.
 * Así ningún color queda con el valor por defecto morado de Material.
 */

internal val DarkColorScheme = darkColorScheme(
    primary = AzulClaro,
    onPrimary = SobreAzulOscuro,
    primaryContainer = ContenedorAzulOscuro,
    onPrimaryContainer = SobreContenedorAzulOscuro,
    inversePrimary = AzulPrincipal,
    secondary = GrisAzuladoClaro,
    onSecondary = SobreGrisOscuro,
    secondaryContainer = ContenedorGrisOscuro,
    onSecondaryContainer = SobreContenedorGrisOscuro,
    tertiary = VerdeClaro,
    onTertiary = SobreVerdeOscuro,
    tertiaryContainer = ContenedorVerdeOscuro,
    onTertiaryContainer = SobreContenedorVerdeOscuro,
    error = ErrorOscuro,
    onError = SobreErrorOscuro,
    errorContainer = ContenedorErrorOscuro,
    onErrorContainer = SobreContenedorErrorOscuro,
    background = FondoOscuro,
    onBackground = TextoClaro,
    surface = SuperficieOscura,
    onSurface = TextoClaro,
    surfaceVariant = VarianteSuperficieOscura,
    onSurfaceVariant = SobreVarianteOscura,
    surfaceTint = AzulClaro,
    inverseSurface = TextoClaro,
    inverseOnSurface = SuperficieOscura,
    outline = ContornoOscuro,
    outlineVariant = ContornoSuaveOscuro,
    surfaceDim = FondoOscuro,
    surfaceBright = SuperficieOscuraBrillante,
    surfaceContainerLowest = SuperficieOscuraMasBaja,
    surfaceContainerLow = SuperficieOscuraBaja,
    surfaceContainer = SuperficieOscuraMedia,
    surfaceContainerHigh = SuperficieOscuraAlta,
    surfaceContainerHighest = SuperficieOscuraMasAlta
)

internal val LightColorScheme = lightColorScheme(
    primary = AzulPrincipal,
    onPrimary = Color.White,
    primaryContainer = ContenedorAzulClaro,
    onPrimaryContainer = SobreContenedorAzulClaro,
    inversePrimary = AzulClaro,
    secondary = GrisAzulado,
    onSecondary = Color.White,
    secondaryContainer = ContenedorGrisClaro,
    onSecondaryContainer = SobreContenedorGrisClaro,
    tertiary = VerdePrincipal,
    onTertiary = Color.White,
    tertiaryContainer = ContenedorVerdeClaro,
    onTertiaryContainer = SobreContenedorVerdeClaro,
    error = ErrorClaro,
    onError = Color.White,
    errorContainer = ContenedorErrorClaro,
    onErrorContainer = SobreContenedorErrorClaro,
    background = FondoClaro,
    onBackground = TextoOscuro,
    surface = Color.White,
    onSurface = TextoOscuro,
    surfaceVariant = VarianteSuperficieClara,
    onSurfaceVariant = SobreVarianteClara,
    surfaceTint = AzulPrincipal,
    inverseSurface = TextoOscuro,
    inverseOnSurface = FondoClaro,
    outline = ContornoClaro,
    outlineVariant = ContornoSuaveClaro,
    surfaceDim = SuperficieClaraTenue,
    surfaceBright = Color.White,
    surfaceContainerLowest = SuperficieClaraMasBaja,
    surfaceContainerLow = SuperficieClaraBaja,
    surfaceContainer = SuperficieClaraMedia,
    surfaceContainerHigh = SuperficieClaraAlta,
    surfaceContainerHighest = SuperficieClaraMasAlta
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
