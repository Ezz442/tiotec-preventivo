package cl.duoc.tiotecpreventivo.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.data.catalogo.ValorRespuesta
import cl.duoc.tiotecpreventivo.ui.components.BarraAnimada
import cl.duoc.tiotecpreventivo.ui.components.GrupoOpciones
import cl.duoc.tiotecpreventivo.ui.components.MensajeError
import cl.duoc.tiotecpreventivo.ui.components.Movimiento
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

                    BarraAnimada(fraccion = Movimiento.fraccion(respondidas, total))

                    Text("Respondidas: $respondidas de $total")

                    // TT-25: al avanzar, la pregunta nueva entra por la derecha; al volver, por la izquierda.
                    AnimatedContent(
                        targetState = uiState.indiceActual,
                        transitionSpec = {
                            val sentido = Movimiento.direccion(initialState, targetState)
                            val duracion = tween<IntOffset>(Movimiento.DURACION_MEDIA)
                            (slideInHorizontally(duracion) { ancho -> sentido * ancho / 4 } +
                                fadeIn(tween(Movimiento.DURACION_MEDIA))) togetherWith
                                (slideOutHorizontally(duracion) { ancho -> -sentido * ancho / 4 } +
                                    fadeOut(tween(Movimiento.DURACION_CORTA)))
                        },
                        label = "Cambio de pregunta"
                    ) { indice ->
                        val mostrada = uiState.preguntas.getOrNull(indice)
                        if (mostrada != null) {
                            // La pregunta que sale no recibe toques: la respuesta sería para otra pregunta.
                            val esActual = indice == uiState.indiceActual

                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(
                                    text = mostrada.categoria.nombre,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = mostrada.texto,
                                    style = MaterialTheme.typography.titleLarge
                                )

                                Text(
                                    text = mostrada.ayuda,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                GrupoOpciones(
                                    opciones = ValorRespuesta.entries,
                                    seleccionada = uiState.respuestas[mostrada.id],
                                    etiqueta = { it.etiqueta },
                                    habilitado = habilitado && esActual,
                                    onSeleccionar = onRespuestaChange
                                )
                            }
                        }
                    }

                    MensajeError(uiState.errorPregunta)

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