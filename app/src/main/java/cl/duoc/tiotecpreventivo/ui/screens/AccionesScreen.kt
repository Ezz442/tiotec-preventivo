package cl.duoc.tiotecpreventivo.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.data.local.entity.AccionEntity
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoAccion
import cl.duoc.tiotecpreventivo.data.local.entity.RecordatorioEntity
import cl.duoc.tiotecpreventivo.ui.components.MensajeError
import cl.duoc.tiotecpreventivo.ui.components.MensajeInformativo
import cl.duoc.tiotecpreventivo.ui.components.Movimiento
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
    onReintentar: () -> Unit = {},
    /** TT-16: accionId → recordatorio programado. */
    recordatorios: Map<Long, RecordatorioEntity> = emptyMap(),
    /** TT-16: true si el teléfono no permite mostrar avisos. */
    avisosBloqueados: Boolean = false,
    onRecordar: (AccionEntity) -> Unit = {}
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

                // TT-25: el número sube o baja según cambie la cantidad de pendientes.
                AnimatedContent(
                    targetState = uiState.pendientes,
                    transitionSpec = {
                        val sentido = Movimiento.direccion(initialState, targetState)
                        (slideInVertically(tween(Movimiento.DURACION_MEDIA)) { alto -> sentido * alto } +
                            fadeIn(tween(Movimiento.DURACION_MEDIA))) togetherWith
                            (slideOutVertically(tween(Movimiento.DURACION_MEDIA)) { alto -> -sentido * alto } +
                                fadeOut(tween(Movimiento.DURACION_CORTA)))
                    },
                    label = "Contador de pendientes"
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

                // Dentro de la cabecera: sin error no ocupa espacio en la lista.
                MensajeError(uiState.errorOperacion)
            }
        }

        if (avisosBloqueados && recordatorios.isNotEmpty()) {
            item {
                Text(
                    text = "Las notificaciones están desactivadas: tus recordatorios se guardan, " +
                        "pero no verás el aviso. Puedes activarlas en Ajustes del teléfono.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
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
                    val completada = accion.estado == EstadoAccion.COMPLETADA
                    // TT-25: la tarjeta cambia de color al completarse y entra o sale de la lista con animateItem.
                    val fondo by animateColorAsState(
                        targetValue = if (completada) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        },
                        animationSpec = tween(Movimiento.DURACION_CORTA),
                        label = "Fondo de la acción"
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(),
                        colors = CardDefaults.cardColors(containerColor = fondo)
                    ) {
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
                                    checked = completada,
                                    onCheckedChange = { onCambiarEstado(accion.id) },
                                    enabled = uiState.actualizandoId == null
                                )
                                Text(
                                    text = if (completada) {
                                        "Completada"
                                    } else {
                                        "Marcar como completada"
                                    }, modifier = Modifier.padding(top = 12.dp)
                                )
                            }

                            // TT-16: recordatorio solo para acciones pendientes.
                            if (accion.estado == EstadoAccion.PENDIENTE) {
                                val recordatorio = recordatorios[accion.id]
                                // TT-25: la fecha del recordatorio aparece o se va al programarlo o cancelarlo.
                                MensajeInformativo(
                                    texto = recordatorio?.let {
                                        "Recordatorio: " + DateFormat
                                            .getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                                            .format(Date(it.fechaHora))
                                    },
                                    estilo = MaterialTheme.typography.bodySmall
                                )
                                TextButton(
                                    onClick = { onRecordar(accion) },
                                    enabled = uiState.actualizandoId == null
                                ) {
                                    Text(if (recordatorio == null) "Programar recordatorio" else "Editar recordatorio")
                                }
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