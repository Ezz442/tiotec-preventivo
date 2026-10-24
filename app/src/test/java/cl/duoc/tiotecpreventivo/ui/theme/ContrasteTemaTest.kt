package cl.duoc.tiotecpreventivo.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * TT-27: el texto de la app se lee bien en claro y en oscuro.
 * WCAG AA pide 4,5:1 para texto normal y 3:1 para bordes y elementos gráficos.
 */
class ContrasteTemaTest {

    private fun canal(c: Float): Double =
        if (c <= 0.03928f) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)

    private fun luminancia(color: Color): Double =
        0.2126 * canal(color.red) + 0.7152 * canal(color.green) + 0.0722 * canal(color.blue)

    private fun contraste(a: Color, b: Color): Double {
        val la = luminancia(a)
        val lb = luminancia(b)
        return (max(la, lb) + 0.05) / (min(la, lb) + 0.05)
    }

    /** Pares texto/fondo que aparecen en las pantallas. */
    private fun paresDeTexto(c: ColorScheme, riesgoMedio: Color) = mapOf(
        "onPrimary sobre primary (botones)" to (c.onPrimary to c.primary),
        "onPrimaryContainer sobre primaryContainer" to (c.onPrimaryContainer to c.primaryContainer),
        "onSecondaryContainer sobre secondaryContainer (opción elegida, chip)" to
            (c.onSecondaryContainer to c.secondaryContainer),
        "onTertiaryContainer sobre tertiaryContainer" to (c.onTertiaryContainer to c.tertiaryContainer),
        "onErrorContainer sobre errorContainer" to (c.onErrorContainer to c.errorContainer),
        "onBackground sobre background" to (c.onBackground to c.background),
        "onSurface sobre surface" to (c.onSurface to c.surface),
        "onSurface sobre surfaceContainerHighest (tarjetas)" to (c.onSurface to c.surfaceContainerHighest),
        "onSurfaceVariant sobre surface (textos de ayuda)" to (c.onSurfaceVariant to c.surface),
        "onSurfaceVariant sobre surfaceVariant" to (c.onSurfaceVariant to c.surfaceVariant),
        "error sobre surface (errores por campo)" to (c.error to c.surface),
        "error sobre tarjeta" to (c.error to c.surfaceContainerHighest),
        "primary sobre surface (mensajes y enlaces)" to (c.primary to c.surface),
        "riesgo bajo (tertiary) sobre tarjeta" to (c.tertiary to c.surfaceContainerHighest),
        "riesgo medio sobre tarjeta" to (riesgoMedio to c.surfaceContainerHighest),
        "riesgo alto (error) sobre tarjeta" to (c.error to c.surfaceContainerHighest)
    )

    private fun comprobar(nombre: String, c: ColorScheme, riesgoMedio: Color) {
        paresDeTexto(c, riesgoMedio).forEach { (par, colores) ->
            val valor = contraste(colores.first, colores.second)
            assertTrue("$nombre · $par: ${"%.2f".format(valor)}:1 (mínimo 4,5:1)", valor >= 4.5)
        }
        val borde = contraste(c.outline, c.surface)
        assertTrue("$nombre · outline sobre surface: ${"%.2f".format(borde)}:1 (mínimo 3:1)", borde >= 3.0)
    }

    @Test
    fun temaClaroCumpleContrasteAA() = comprobar("Claro", LightColorScheme, RiesgoMedioClaro)

    @Test
    fun temaOscuroCumpleContrasteAA() = comprobar("Oscuro", DarkColorScheme, RiesgoMedioOscuro)

    @Test
    fun losContenedoresNoQuedanConElMoradoPorDefecto() {
        // Material 3 trae un morado por defecto (#EADDFF / #4F378B) si no se definen.
        val morados = setOf(Color(0xFFEADDFF), Color(0xFF4F378B), Color(0xFFE8DEF8), Color(0xFF4A4458))
        listOf(LightColorScheme, DarkColorScheme).forEach { c ->
            listOf(c.primaryContainer, c.secondaryContainer, c.surfaceVariant, c.surfaceContainerHighest)
                .forEach { assertTrue("Color por defecto sin reemplazar: $it", it !in morados) }
        }
    }
}
