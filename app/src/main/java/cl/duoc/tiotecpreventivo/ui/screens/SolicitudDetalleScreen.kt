package cl.duoc.tiotecpreventivo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.ui.components.AvisoSincronizacion
import cl.duoc.tiotecpreventivo.ui.components.TextoConCarga
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoSolicitud
import cl.duoc.tiotecpreventivo.viewmodel.SolicitudDetalleUiState
import java.text.DateFormat
import java.util.Date

@Composable
fun SolicitudDetalleScreen(
    uiState: SolicitudDetalleUiState,
    onSolicitarCambio: (EstadoSolicitud) -> Unit,
    onConfirmarCambio: () -> Unit,
    onCancelarCambio: () -> Unit,
    onReintentar: () -> Unit,
    onMensajeConsumido: () -> Unit,
    onSolicitarEliminar: () -> Unit,
    onConfirmarEliminar: () -> Unit,
    onCancelarEliminar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val formatoFecha = remember {
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    }
    val detalle = uiState.detalle

    // TT-29: al eliminar la solicitud ya no hay nada que mostrar; se vuelve a la lista.
    LaunchedEffect(uiState.eliminada) {
        if (uiState.eliminada) onVolver()
    }

    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            onMensajeConsumido()
        }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Demostración académica. No envía solicitudes a especialistas reales.")
                    AvisoSincronizacion(
                        sincronizando = uiState.sincronizando,
                        error = uiState.errorSincronizacion,
                        onReintentar = onReintentar
                    )
                }
            }

            when {
                uiState.cargando -> {
                    item {
                        CircularProgressIndicator()
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

                detalle != null -> {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = detalle.solicitud.codigo,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text("Categoría: ${detalle.solicitud.categoria.nombre}")
                                Text("Estado actual: ${detalle.solicitud.estado.etiqueta}")
                                Text(
                                    "Creada: ${
                                        formatoFecha.format(Date(detalle.solicitud.creadaEn))
                                    }"
                                )
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Cambiar estado",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text("Selecciona un estado y confirma el cambio.")

                            EstadoSolicitud.entries.forEach { estado ->
                                FilterChip(
                                    modifier = Modifier.fillMaxWidth(),
                                    selected = detalle.solicitud.estado == estado,
                                    onClick = { onSolicitarCambio(estado) },
                                    enabled = !uiState.actualizando && detalle.solicitud.estado != estado,
                                    label = { Text(estado.etiqueta) })
                            }

                            if (uiState.estadoPorConfirmar == null) {
                                uiState.errorOperacion?.let { error ->
                                    Text(
                                        text = error, color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedButton(
                            onClick = onSolicitarEliminar,
                            enabled = !uiState.actualizando && !uiState.eliminando,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Eliminar solicitud")
                        }
                    }

                    item {
                        Text(
                            text = "Historial de estados",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    if (detalle.historial.isEmpty()) {
                        item {
                            Text("No hay cambios registrados.")
                        }
                    }

                    items(
                        items = detalle.historial.sortedWith(compareByDescending<cl.duoc.tiotecpreventivo.data.local.entity.HistorialSolicitudEntity> { it.fecha }.thenByDescending { it.id }),
                        key = { it.id }) { registro ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = registro.estado.etiqueta,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(formatoFecha.format(Date(registro.fecha)))
                                Text(registro.nota)
                            }
                        }
                    }
                }
            }
        }
    }

    uiState.estadoPorConfirmar?.let { estado ->
        AlertDialog(onDismissRequest = {
            if (!uiState.actualizando) onCancelarCambio()
        }, title = { Text("Confirmar cambio") }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("¿Cambiar el estado a «${estado.etiqueta}»?")

                uiState.errorOperacion?.let { error ->
                    Text(
                        text = error, color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }, confirmButton = {
            Button(
                onClick = onConfirmarCambio, enabled = !uiState.actualizando
            ) {
                TextoConCarga(texto = "Confirmar", cargando = uiState.actualizando)
            }
        }, dismissButton = {
            TextButton(
                onClick = onCancelarCambio, enabled = !uiState.actualizando
            ) {
                Text("Cancelar")
            }
        })
    }

    if (uiState.confirmarEliminar) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.eliminando) onCancelarEliminar()
            },
            title = { Text("Eliminar solicitud") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("¿Eliminar esta solicitud y su historial? Esta acción no se puede deshacer.")

                    uiState.errorOperacion?.let { error ->
                        Text(text = error, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirmarEliminar, enabled = !uiState.eliminando
                ) {
                    TextoConCarga(texto = "Eliminar", cargando = uiState.eliminando)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onCancelarEliminar, enabled = !uiState.eliminando
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
