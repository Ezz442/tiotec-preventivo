package cl.duoc.tiotecpreventivo.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.R
import cl.duoc.tiotecpreventivo.data.local.entity.NivelRiesgo
import cl.duoc.tiotecpreventivo.ui.theme.RiesgoMedioClaro
import cl.duoc.tiotecpreventivo.ui.theme.RiesgoMedioOscuro
import cl.duoc.tiotecpreventivo.ui.theme.TioTecPreventivoTheme

/** Ícono de cada nivel: el riesgo se entiende por texto + ícono, no solo por el color (TT-12). */
@DrawableRes
fun iconoNivel(nivel: NivelRiesgo): Int = when (nivel) {
    NivelRiesgo.RIESGO_BAJO -> R.drawable.ic_riesgo_bajo
    NivelRiesgo.RIESGO_MEDIO -> R.drawable.ic_riesgo_medio
    NivelRiesgo.RIESGO_ALTO -> R.drawable.ic_riesgo_alto
}

@Composable
@ReadOnlyComposable
fun colorNivel(nivel: NivelRiesgo): Color {
    val esOscuro = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return when (nivel) {
        NivelRiesgo.RIESGO_BAJO -> MaterialTheme.colorScheme.tertiary
        // TT-27: el color de advertencia vive en el tema (Color.kt), no en la pantalla.
        NivelRiesgo.RIESGO_MEDIO -> if (esOscuro) RiesgoMedioOscuro else RiesgoMedioClaro
        NivelRiesgo.RIESGO_ALTO -> MaterialTheme.colorScheme.error
    }
}

/** Ícono + etiqueta del nivel ("Riesgo medio"). */
@Composable
fun NivelRiesgoIndicador(
    nivel: NivelRiesgo,
    modifier: Modifier = Modifier,
    tamanoIcono: Dp = 24.dp,
    estilo: TextStyle = MaterialTheme.typography.titleMedium
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconoNivel(nivel)),
            // El texto de al lado ya dice el nivel; el lector de pantalla no lo repite.
            contentDescription = null,
            tint = colorNivel(nivel),
            modifier = Modifier.size(tamanoIcono)
        )

        Text(
            text = nivel.etiqueta,
            style = estilo,
            fontWeight = FontWeight.SemiBold,
            color = colorNivel(nivel)
        )
    }
}

@Preview(showBackground = true, name = "Niveles claro")
@Composable
fun NivelRiesgoIndicadorPreview() {
    TioTecPreventivoTheme(darkTheme = false) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            NivelRiesgo.entries.forEach { NivelRiesgoIndicador(it) }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF101820, name = "Niveles oscuro")
@Composable
fun NivelRiesgoIndicadorOscuroPreview() {
    TioTecPreventivoTheme(darkTheme = true) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            NivelRiesgo.entries.forEach { NivelRiesgoIndicador(it) }
        }
    }
}
