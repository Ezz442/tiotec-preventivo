package cl.duoc.tiotecpreventivo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.viewmodel.EvaluacionUiState

@Composable
fun RevisionEvaluacionScreen(
    uiState: EvaluacionUiState,
    onEditarRespuesta: (Int) -> Unit,
    onFinalizar: () -> Unit,
    onVolver: () -> Unit,
    onSalir: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habilitado =
        !uiState.cargando && !uiState.guardando && uiState.errorCarga == null && uiState.evaluacionGuardadaId == null

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Revisa tus respuestas", style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    "Puedes corregirlas antes de finalizar. " + "La evaluación todavía no está guardada."
                )
            }
        }

        if (uiState.cargando) {
            item {
                CircularProgressIndicator()
            }
        }

        uiState.errorCarga?.let { error ->
            item {
                Text(
                    text = error, color = MaterialTheme.colorScheme.error
                )
            }
        }

        itemsIndexed(
            items = uiState.preguntas, key = { _, pregunta -> pregunta.id }) { indice, pregunta ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = pregunta.texto, style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Respuesta: " + (uiState.respuestas[pregunta.id]?.etiqueta
                            ?: "Sin responder")
                    )

                    TextButton(
                        onClick = { onEditarRespuesta(indice) }, enabled = habilitado
                    ) {
                        Text("Cambiar respuesta")
                    }
                }
            }
        }

        uiState.errorGuardado?.let { error ->
            item {
                Text(
                    text = error, color = MaterialTheme.colorScheme.error
                )
            }
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onFinalizar, enabled = habilitado, modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (uiState.guardando) "Guardando…"
                        else "Finalizar evaluación"
                    )
                }

                OutlinedButton(
                    onClick = onVolver,
                    enabled = !uiState.guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver a las preguntas")
                }

                TextButton(
                    onClick = onSalir, enabled = !uiState.guardando
                ) {
                    Text("Salir sin guardar")
                }
            }
        }
    }
}