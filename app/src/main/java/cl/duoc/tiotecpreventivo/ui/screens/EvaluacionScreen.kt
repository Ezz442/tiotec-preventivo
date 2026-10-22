package cl.duoc.tiotecpreventivo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.data.catalogo.ValorRespuesta
import cl.duoc.tiotecpreventivo.ui.theme.TioTecPreventivoTheme
import cl.duoc.tiotecpreventivo.viewmodel.EvaluacionUiState

@Composable
fun EvaluacionScreen(
    uiState: EvaluacionUiState,
    onRespuestaChange: (ValorRespuesta) -> Unit,
    onAnterior: () -> Unit,
    onSiguiente: () -> Unit,
    onRevisar: () -> Unit,
    onSalir: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when {
            uiState.cargando -> {
                CircularProgressIndicator()
                Text("Cargando evaluación…")
            }

            uiState.errorCarga != null -> {
                Text(
                    text = uiState.errorCarga, color = MaterialTheme.colorScheme.error
                )

                Button(onClick = onReintentar) {
                    Text("Reintentar")
                }
            }

            else -> {
                val pregunta = uiState.preguntas.getOrNull(
                    uiState.indiceActual
                )

                if (pregunta != null) {
                    val total = uiState.preguntas.size
                    val respondidas = uiState.respuestas.size
                    val habilitado = !uiState.guardando && uiState.evaluacionGuardadaId == null

                    Text(
                        text = "Dispositivo: ${uiState.dispositivo?.nombre.orEmpty()}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        "Pregunta ${uiState.indiceActual + 1} de $total"
                    )

                    LinearProgressIndicator(
                        progress = { respondidas.toFloat() / total },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Respondidas: $respondidas de $total")

                    Text(
                        text = pregunta.categoria.nombre,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = pregunta.texto, style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = pregunta.ayuda, style = MaterialTheme.typography.bodyLarge
                    )

                    Column(
                        modifier = Modifier.selectableGroup()
                    ) {
                        ValorRespuesta.entries.forEach { respuesta ->
                            val seleccionada = uiState.respuestas[pregunta.id] == respuesta

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = seleccionada,
                                        enabled = habilitado,
                                        role = Role.RadioButton,
                                        onClick = {
                                            onRespuestaChange(respuesta)
                                        })
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RadioButton(
                                    selected = seleccionada, onClick = null, enabled = habilitado
                                )

                                Text(respuesta.etiqueta)
                            }
                        }
                    }

                    uiState.errorPregunta?.let { error ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(
                                    android.R.drawable.ic_dialog_alert
                                ), contentDescription = null, tint = MaterialTheme.colorScheme.error
                            )

                            Text(
                                text = error, color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onAnterior,
                        enabled = habilitado && uiState.indiceActual > 0,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Anterior")
                    }

                    val ultimaPregunta = uiState.indiceActual == uiState.preguntas.lastIndex

                    Button(
                        onClick = if (ultimaPregunta) onRevisar else onSiguiente,
                        enabled = habilitado,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (ultimaPregunta) "Revisar respuestas"
                            else "Siguiente"
                        )
                    }
                } else {
                    Text("No hay una pregunta disponible.")
                }
            }
        }

        TextButton(
            onClick = onSalir, enabled = !uiState.guardando
        ) {
            Text("Salir de la evaluación")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EvaluacionPreview() {
    TioTecPreventivoTheme {
        EvaluacionScreen(
            uiState = EvaluacionUiState(),
            onRespuestaChange = {},
            onAnterior = {},
            onSiguiente = {},
            onRevisar = {},
            onSalir = {},
            onReintentar = {})
    }
}