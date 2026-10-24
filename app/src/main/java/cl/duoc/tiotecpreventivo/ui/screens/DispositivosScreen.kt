package cl.duoc.tiotecpreventivo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import java.io.File
import cl.duoc.tiotecpreventivo.ui.components.ImagenDispositivo
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.ui.components.TextoConCarga
import cl.duoc.tiotecpreventivo.ui.components.MensajeError
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo
import cl.duoc.tiotecpreventivo.ui.components.DispositivoFormulario
import cl.duoc.tiotecpreventivo.ui.theme.TioTecPreventivoTheme
import cl.duoc.tiotecpreventivo.viewmodel.DispositivosUiState
import androidx.compose.foundation.layout.WindowInsets

@Composable
fun DispositivosScreen(
    modifier: Modifier = Modifier,
    uiState: DispositivosUiState = DispositivosUiState(),
    onAgregar: () -> Unit = {},
    onEditar: (Long) -> Unit = {},
    onEvaluar: (Long) -> Unit = {},
    onSolicitarEliminar: (Long) -> Unit = {},
    onConfirmarEliminar: () -> Unit = {},
    onCancelarEliminar: () -> Unit = {},
    onNombreChange: (String) -> Unit = {},
    onSistemaChange: (SistemaOperativo) -> Unit = {},
    onGuardar: () -> Unit = {},
    onCancelarFormulario: () -> Unit = {},
    onReintentar: () -> Unit = {},
    onIrAPerfil: () -> Unit = {},
    onConsumirMensaje: () -> Unit = {},
    /** TT-18: convierte la ruta guardada en Room en el archivo de la foto. */
    archivoImagen: (String) -> File? = { null },
    onTomarFoto: (Long) -> Unit = {},
    onElegirImagen: (Long) -> Unit = {},
    onSolicitarQuitarFoto: (Long) -> Unit = {},
    onConfirmarQuitarFoto: () -> Unit = {},
    onCancelarQuitarFoto: () -> Unit = {},
    /** TT-26: confirmar o no el descarte de cambios sin guardar. */
    onDescartarCambios: () -> Unit = {},
    onSeguirEditando: () -> Unit = {}
) {
    val fotoOcupada =
        uiState.procesandoFoto || uiState.capturaFoto != null || uiState.dispositivoGaleriaId != null
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            onConsumirMensaje()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Administra los equipos ficticios de tus evaluaciones.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            when {
                uiState.cargando -> {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Cargando dispositivos…")
                        }
                    }
                }

                uiState.errorCarga != null -> {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = uiState.errorCarga, color = MaterialTheme.colorScheme.error
                            )

                            Button(onClick = onReintentar) {
                                Text("Reintentar")
                            }
                        }
                    }
                }

                uiState.perfilId == null -> {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Primero crea tu perfil ficticio.")

                            Button(onClick = onIrAPerfil) {
                                Text("Ir a Perfil")
                            }
                        }
                    }
                }

                uiState.formularioVisible -> {
                    item {
                        DispositivoFormulario(
                            uiState = uiState,
                            onNombreChange = onNombreChange,
                            onSistemaChange = onSistemaChange,
                            onGuardar = onGuardar,
                            onCancelar = onCancelarFormulario
                        )
                    }
                }

                else -> {
                    item {
                        Button(
                            onClick = onAgregar,
                            enabled = !uiState.eliminando && !fotoOcupada,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Agregar dispositivo")
                        }
                    }

                    if (uiState.dispositivos.isEmpty()) {
                        item {
                            Text(
                                "Todavía no tienes dispositivos. Agrega uno para comenzar."
                            )
                        }
                    }

                    items(
                        items = uiState.dispositivos, key = { it.id }) { dispositivo ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = dispositivo.nombre,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(dispositivo.sistemaOperativo.etiqueta)

                                // TT-18: foto del objeto de prueba asociada al dispositivo.
                                dispositivo.imagenRuta?.let { ruta ->
                                    ImagenDispositivo(
                                        archivo = archivoImagen(ruta),
                                        descripcion = "Foto de ${dispositivo.nombre}"
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { onTomarFoto(dispositivo.id) },
                                        enabled = !uiState.eliminando && !fotoOcupada
                                    ) {
                                        Text(if (dispositivo.imagenRuta == null) "Tomar foto" else "Reemplazar foto")
                                    }
                                    if (dispositivo.imagenRuta != null) {
                                        TextButton(
                                            onClick = { onSolicitarQuitarFoto(dispositivo.id) },
                                            enabled = !uiState.eliminando && !fotoOcupada
                                        ) {
                                            Text("Quitar foto")
                                        }
                                    }
                                }

                                OutlinedButton(
                                    onClick = { onElegirImagen(dispositivo.id) },
                                    enabled = !uiState.eliminando && !fotoOcupada,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Elegir imagen de galería")
                                }

                                Button(
                                    onClick = { onEvaluar(dispositivo.id) },
                                    enabled = !uiState.eliminando && !fotoOcupada
                                ) {
                                    Text("Evaluar dispositivo")
                                }

                                OutlinedButton(
                                    onClick = { onEditar(dispositivo.id) },
                                    enabled = !uiState.eliminando && !fotoOcupada
                                ) {
                                    Text("Editar")
                                }

                                TextButton(
                                    onClick = {
                                        onSolicitarEliminar(dispositivo.id)
                                    }, enabled = !uiState.eliminando && !fotoOcupada
                                ) {
                                    Text("Eliminar")
                                }
                            }
                        }
                    }

                    if (uiState.dispositivoAEliminar == null) {
                        uiState.errorOperacion?.let { error ->
                            item {
                                Text(
                                    text = error, color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // TT-26: cancelar con cambios sin guardar pide confirmación.
    if (uiState.confirmarDescartar) {
        AlertDialog(
            onDismissRequest = onSeguirEditando,
            title = { Text("¿Descartar cambios?") },
            text = {
                Text(
                    if (uiState.dispositivoEnEdicionId == null) "El dispositivo no se agregará."
                    else "Los cambios de este dispositivo no se guardarán."
                )
            },
            confirmButton = {
                TextButton(onClick = onDescartarCambios) { Text("Descartar") }
            },
            dismissButton = {
                TextButton(onClick = onSeguirEditando) { Text("Seguir editando") }
            }
        )
    }

    uiState.dispositivoQuitarFoto?.let { dispositivo ->
        AlertDialog(
            onDismissRequest = onCancelarQuitarFoto,
            title = { Text("Quitar foto") },
            text = { Text("¿Quitar la foto de «${dispositivo.nombre}»? El archivo se borrará del teléfono.") },
            confirmButton = {
                TextButton(onClick = onConfirmarQuitarFoto, enabled = !uiState.procesandoFoto) {
                    TextoConCarga(texto = "Quitar", cargando = uiState.procesandoFoto, textoCargando = "Quitando…")
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelarQuitarFoto, enabled = !uiState.procesandoFoto) {
                    Text("Cancelar")
                }
            })
    }

    uiState.dispositivoAEliminar?.let { dispositivo ->
        AlertDialog(onDismissRequest = onCancelarEliminar, title = {
            Text("Eliminar dispositivo")
        }, text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("¿Quieres quitar «${dispositivo.nombre}»?")

                Text(
                    "Si tiene evaluaciones, se archivará y conservará su historial. " + "Si no tiene evaluaciones, se eliminará."
                )

                MensajeError(uiState.errorOperacion)
            }
        }, confirmButton = {
            TextButton(
                onClick = onConfirmarEliminar, enabled = !uiState.eliminando
            ) {
                TextoConCarga(texto = "Confirmar", cargando = uiState.eliminando, textoCargando = "Procesando…")
            }
        }, dismissButton = {
            TextButton(
                onClick = onCancelarEliminar, enabled = !uiState.eliminando
            ) {
                Text("Cancelar")
            }
        })
    }
}

@Preview(showBackground = true)
@Composable
fun DispositivosPreview() {
    TioTecPreventivoTheme {
        DispositivosScreen(
            uiState = DispositivosUiState(
                cargando = false, perfilId = 1L
            )
        )
    }
}