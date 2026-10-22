package cl.duoc.tiotecpreventivo.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoAccion
import cl.duoc.tiotecpreventivo.ui.theme.TioTecPreventivoTheme
import cl.duoc.tiotecpreventivo.viewmodel.AccionesUiState
import java.text.DateFormat
import java.util.Date

@Composable
fun AccionesScreen(
    modifier: Modifier = Modifier,
    uiState: AccionesUiState = AccionesUiState(),
    onFiltroChange: (EstadoAccion) -> Unit = {},
    onCambiarEstado: (Long) -> Unit = {},
    onReintentar: () -> Unit = {}
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Mis acciones", style = MaterialTheme.typography.headlineMedium
                )

                AnimatedContent(
                    targetState = uiState.pendientes, label = "Contador de pendientes"
                ) { cantidad ->
                    Text("Acciones pendientes: $cantidad")
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterChip(
                        selected = uiState.filtro == EstadoAccion.PENDIENTE,
                        onClick = { onFiltroChange(EstadoAccion.PENDIENTE) },
                        enabled = uiState.actualizandoId == null,
                        label = { Text("Pendientes") })
                    FilterChip(
                        selected = uiState.filtro == EstadoAccion.COMPLETADA,
                        onClick = { onFiltroChange(EstadoAccion.COMPLETADA) },
                        enabled = uiState.actualizandoId == null,
                        label = { Text("Completadas") })
                }
            }
        }

        when {
            uiState.cargando -> {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator()
                        Text("Cargando acciones…")
                    }
                }
            }

            uiState.errorCarga != null -> {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = uiState.errorCarga, color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = onReintentar) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            else -> {
                uiState.errorOperacion?.let { error ->
                    item {
                        Text(
                            text = error, color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                if (uiState.acciones.isEmpty()) {
                    item {
                        Text(
                            if (uiState.filtro == EstadoAccion.PENDIENTE) {
                                "No tienes acciones pendientes. Puedes realizar una evaluación desde Mis dispositivos."
                            } else {
                                "Todavía no tienes acciones completadas."
                            }
                        )
                    }
                }

                items(uiState.acciones, key = { it.id }) { accion ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = accion.titulo, style = MaterialTheme.typography.titleMedium
                            )
                            Text(accion.descripcion)
                            Text(accion.prioridad.etiqueta)
                            Text(
                                "Revisión: ${
                                    DateFormat.getDateInstance(DateFormat.MEDIUM)
                                        .format(Date(accion.fechaRevision))
                                }"
                            )

                            Row {
                                Checkbox(
                                    checked = accion.estado == EstadoAccion.COMPLETADA,
                                    onCheckedChange = { onCambiarEstado(accion.id) },
                                    enabled = uiState.actualizandoId == null
                                )
                                Text(
                                    text = if (accion.estado == EstadoAccion.COMPLETADA) {
                                        "Completada"
                                    } else {
                                        "Marcar como completada"
                                    }, modifier = Modifier.padding(top = 12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccionesPreview() {
    TioTecPreventivoTheme {
        AccionesScreen(uiState = AccionesUiState(cargando = false))
    }
}