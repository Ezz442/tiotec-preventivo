package cl.duoc.tiotecpreventivo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.R
import cl.duoc.tiotecpreventivo.ui.theme.TioTecPreventivoTheme

/*
 * Animaciones compartidas (TT-25). Solo muestran el estado que ya calculó el ViewModel:
 * no deciden nada, no esperan tiempos artificiales y no deshabilitan controles.
 */

/** Guarda el último texto no nulo sin provocar recomposiciones. */
private class UltimoTexto(var valor: String)

/**
 * Error con ícono + texto que aparece y desaparece con AnimatedVisibility.
 * Mientras se oculta sigue mostrando el último texto, para que no quede una fila vacía.
 * `liveRegion` hace que TalkBack lea el error apenas aparece.
 */
@Composable
fun MensajeError(
    texto: String?,
    modifier: Modifier = Modifier,
    estilo: TextStyle = MaterialTheme.typography.bodyMedium
) {
    val ultimo = remember { UltimoTexto(texto.orEmpty()) }
    if (texto != null) ultimo.valor = texto
    // Se lee en una variable local para que el contenido se actualice si cambia el texto.
    val mostrado = texto ?: ultimo.valor

    AnimatedVisibility(
        visible = texto != null,
        modifier = modifier,
        enter = fadeIn(tween(Movimiento.DURACION_CORTA)) +
            expandVertically(tween(Movimiento.DURACION_CORTA)),
        exit = fadeOut(tween(Movimiento.DURACION_CORTA)) +
            shrinkVertically(tween(Movimiento.DURACION_CORTA))
    ) {
        Row(
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_riesgo_alto),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Text(text = mostrado, color = MaterialTheme.colorScheme.error, style = estilo)
        }
    }
}

/** Mensaje informativo (perfil guardado, fecha de un recordatorio) que aparece y se va suavemente. */
@Composable
fun MensajeInformativo(
    texto: String?,
    modifier: Modifier = Modifier,
    estilo: TextStyle = MaterialTheme.typography.bodyMedium
) {
    val ultimo = remember { UltimoTexto(texto.orEmpty()) }
    if (texto != null) ultimo.valor = texto
    // Se lee en una variable local para que el contenido se actualice si cambia el texto.
    val mostrado = texto ?: ultimo.valor

    AnimatedVisibility(
        visible = texto != null,
        modifier = modifier,
        enter = fadeIn(tween(Movimiento.DURACION_CORTA)) +
            expandVertically(tween(Movimiento.DURACION_CORTA)),
        exit = fadeOut(tween(Movimiento.DURACION_CORTA)) +
            shrinkVertically(tween(Movimiento.DURACION_CORTA))
    ) {
        Text(
            text = mostrado,
            color = MaterialTheme.colorScheme.primary,
            style = estilo,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
        )
    }
}

/**
 * Opción de radio con fondo, borde y texto que cambian con animateColorAsState y
 * animateDpAsState al seleccionarla. Toda la fila se puede tocar.
 */
@Composable
fun OpcionSeleccionable(
    texto: String,
    seleccionada: Boolean,
    habilitada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val forma = MaterialTheme.shapes.medium
    val colores = MaterialTheme.colorScheme
    val animacion = tween<Color>(Movimiento.DURACION_CORTA)

    val fondo by animateColorAsState(
        targetValue = if (seleccionada) colores.secondaryContainer else Color.Transparent,
        animationSpec = animacion,
        label = "Fondo de la opción"
    )
    val colorBorde by animateColorAsState(
        targetValue = if (seleccionada) colores.primary else colores.outlineVariant,
        animationSpec = animacion,
        label = "Borde de la opción"
    )
    val colorTexto by animateColorAsState(
        targetValue = if (seleccionada) colores.onSecondaryContainer else colores.onSurface,
        animationSpec = animacion,
        label = "Texto de la opción"
    )
    val grosorBorde by animateDpAsState(
        targetValue = if (seleccionada) 2.dp else 1.dp,
        animationSpec = tween(Movimiento.DURACION_CORTA),
        label = "Grosor del borde"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(forma)
            .background(fondo, forma)
            .border(grosorBorde, colorBorde, forma)
            .selectable(
                selected = seleccionada,
                enabled = habilitada,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(selected = seleccionada, onClick = null, enabled = habilitada)
        Text(text = texto, color = colorTexto)
    }
}

/** Grupo de [OpcionSeleccionable] con la semántica de grupo de radio. */
@Composable
fun <T> GrupoOpciones(
    opciones: List<T>,
    seleccionada: T?,
    etiqueta: (T) -> String,
    habilitado: Boolean,
    onSeleccionar: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        opciones.forEach { opcion ->
            OpcionSeleccionable(
                texto = etiqueta(opcion),
                seleccionada = opcion == seleccionada,
                habilitada = habilitado,
                onClick = { onSeleccionar(opcion) }
            )
        }
    }
}

/**
 * Barra de progreso que avanza suavemente hacia [fraccion].
 * Con [desdeCero] parte vacía al aparecer (resultados); si no, solo anima los cambios.
 * La barra es decorativa: el texto de al lado ya dice el valor.
 */
@Composable
fun BarraAnimada(
    fraccion: Float,
    modifier: Modifier = Modifier,
    color: Color = ProgressIndicatorDefaults.linearColor,
    desdeCero: Boolean = false,
    duracion: Int = Movimiento.DURACION_MEDIA
) {
    var iniciada by remember { mutableStateOf(!desdeCero) }
    LaunchedEffect(Unit) { iniciada = true }

    val progreso by animateFloatAsState(
        targetValue = if (iniciada) fraccion.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(duracion),
        label = "Progreso"
    )

    LinearProgressIndicator(
        progress = { progreso },
        color = color,
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics {}
    )
}

/**
 * Contenido de un botón que guarda: muestra un indicador pequeño solo mientras
 * [cargando] es verdadero, es decir, mientras la operación real está en curso.
 */
@Composable
fun TextoConCarga(
    texto: String,
    cargando: Boolean,
    textoCargando: String = "Guardando…"
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AnimatedVisibility(
            visible = cargando,
            enter = fadeIn(tween(Movimiento.DURACION_CORTA)) +
                expandHorizontally(tween(Movimiento.DURACION_CORTA)),
            exit = fadeOut(tween(Movimiento.DURACION_CORTA)) +
                shrinkHorizontally(tween(Movimiento.DURACION_CORTA))
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(16.dp),
                strokeWidth = 2.dp,
                color = LocalContentColor.current
            )
        }
        Text(if (cargando) textoCargando else texto)
    }
}

@Preview(showBackground = true, name = "Animaciones · estados finales")
@Composable
fun AnimacionesPreview() {
    TioTecPreventivoTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GrupoOpciones(
                opciones = listOf("Sí", "No", "No lo sé"),
                seleccionada = "No",
                etiqueta = { it },
                habilitado = true,
                onSeleccionar = {}
            )
            MensajeError("Selecciona una respuesta para continuar.")
            BarraAnimada(fraccion = 0.6f)
            TextoConCarga(texto = "Guardar", cargando = true)
        }
    }
}
