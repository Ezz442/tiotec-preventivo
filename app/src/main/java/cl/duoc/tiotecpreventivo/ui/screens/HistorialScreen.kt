package cl.duoc.tiotecpreventivo.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.data.local.entity.EvaluacionEntity
import cl.duoc.tiotecpreventivo.data.local.entity.NivelRiesgo
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo
import cl.duoc.tiotecpreventivo.domain.HistorialLogica
import cl.duoc.tiotecpreventivo.ui.components.NivelRiesgoIndicador
import cl.duoc.tiotecpreventivo.ui.theme.TioTecPreventivoTheme
import cl.duoc.tiotecpreventivo.viewmodel.HistorialUiState
import java.text.DateFormat
import java.util.Date
import java.util.Locale

/** Historial preventivo local (TT-14). El detalle reutiliza la pantalla de resultados (TT-12). */
@Composable
fun HistorialScreen(
    uiState: HistorialUiState,
    onSeleccionarDispositivo: (Long?) -> Unit,
    onAbrirEvaluacion: (evaluacionId: Long) -> Unit,
    onIrADispositivos: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val ancho = Modifier
            .widthIn(max = 720.dp)
            .fillMaxWidth()

        item {
            Column(ancho, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Historial de evaluaciones",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = "Cada evaluación conserva sus respuestas y resultados tal como se guardaron.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        when {
            uiState.cargando -> item {
                Column(ancho, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator()
                    Text("Cargando historial…")
                }
            }

            uiState.error != null -> item {
                Column(ancho, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(uiState.error, color = MaterialTheme.colorScheme.error)
                    Button(onClick = onReintentar) { Text("Reintentar") }
                }
            }

            uiState.todas.isEmpty() -> item {
                Column(ancho, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Todavía no tienes evaluaciones guardadas.")
                    Text(
                        "Evalúa uno de tus dispositivos y el resultado aparecerá aquí.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = onIrADispositivos) { Text("Ir a Mis dispositivos") }
                }
            }

            else -> {
                if (uiState.filtros.size > 1) {
                    item {
                        Row(
                            modifier = ancho.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.dispositivoSeleccionado == null,
                                onClick = { onSeleccionarDispositivo(null) },
                                label = { Text("Todos (${uiState.todas.size})") }
                            )
                            uiState.filtros.forEach { filtro ->
                                FilterChip(
                                    selected = uiState.dispositivoSeleccionado == filtro.dispositivoId,
                                    onClick = { onSeleccionarDispositivo(filtro.dispositivoId) },
                                    label = { Text("${filtro.nombre} (${filtro.cantidad})") }
                                )
                            }
                        }
                    }
                }

                items(uiState.visibles, key = { it.id }) { evaluacion ->
                    TarjetaEvaluacion(evaluacion, onAbrirEvaluacion, ancho)
                }
            }
        }
    }
}

@Composable
private fun TarjetaEvaluacion(
    evaluacion: EvaluacionEntity,
    onAbrir: (Long) -> Unit,
    modifier: Modifier
) {
    Card(modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "${evaluacion.nombreDispositivo} · ${evaluacion.sistemaOperativoDispositivo.etiqueta}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(formatearFechaHistorial(evaluacion.fecha), style = MaterialTheme.typography.bodySmall)
            NivelRiesgoIndicador(evaluacion.nivelGlobal)
            TextButton(onClick = { onAbrir(evaluacion.id) }) {
                Text("Ver detalle")
            }
        }
    }
}

private fun formatearFechaHistorial(fecha: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.forLanguageTag("es-CL"))
        .format(Date(fecha))

// ---------- Previews ----------

private val evaluacionesDeEjemplo = listOf(
    EvaluacionEntity(3, 1, "Teléfono de prueba", SistemaOperativo.ANDROID, 1_761_200_000_000, NivelRiesgo.RIESGO_MEDIO),
    EvaluacionEntity(2, 2, "Computador de prueba", SistemaOperativo.WINDOWS, 1_761_100_000_000, NivelRiesgo.RIESGO_BAJO),
    EvaluacionEntity(1, 1, "Teléfono antiguo", SistemaOperativo.ANDROID, 1_761_000_000_000, NivelRiesgo.RIESGO_ALTO)
)

@Preview(showBackground = true, name = "Historial con evaluaciones")
@Composable
fun HistorialPreview() {
    TioTecPreventivoTheme {
        HistorialScreen(
            uiState = HistorialUiState(
                cargando = false,
                todas = evaluacionesDeEjemplo,
                filtros = HistorialLogica.filtros(evaluacionesDeEjemplo)
            ),
            onSeleccionarDispositivo = {}, onAbrirEvaluacion = {}, onIrADispositivos = {}, onReintentar = {}
        )
    }
}

@Preview(showBackground = true, name = "Historial vacío")
@Composable
fun HistorialVacioPreview() {
    TioTecPreventivoTheme {
        HistorialScreen(
            uiState = HistorialUiState(cargando = false),
            onSeleccionarDispositivo = {}, onAbrirEvaluacion = {}, onIrADispositivos = {}, onReintentar = {}
        )
    }
}

@Preview(showBackground = true, name = "Historial con error")
@Composable
fun HistorialErrorPreview() {
    TioTecPreventivoTheme {
        HistorialScreen(
            uiState = HistorialUiState(cargando = false, error = "No pudimos cargar el historial."),
            onSeleccionarDispositivo = {}, onAbrirEvaluacion = {}, onIrADispositivos = {}, onReintentar = {}
        )
    }
}
