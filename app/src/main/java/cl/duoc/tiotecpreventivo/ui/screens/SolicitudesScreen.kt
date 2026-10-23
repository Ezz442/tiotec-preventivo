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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.ui.components.SolicitudDialogo
import cl.duoc.tiotecpreventivo.viewmodel.SolicitudesUiState
import java.text.DateFormat
import java.util.Date

@Composable
fun SolicitudesScreen(
    uiState: SolicitudesUiState,
    onNuevaSolicitud: () -> Unit,
    onCategoriaChange: (Categoria) -> Unit,
    onGuardar: () -> Unit,
    onCerrarFormulario: () -> Unit,
    onReintentar: () -> Unit,
    onMensajeConsumido: () -> Unit,
    onAbrirDetalle: (Long) -> Unit,
    onIrPerfil: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val formatoFecha = remember {
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
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
                    Text(
                        text = "Solicitudes de asistencia",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        "Simulación local: las solicitudes no se envían a ningún servicio."
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

                else -> {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (uiState.perfilId == null) {
                                Text("Primero crea tu perfil para registrar solicitudes.")
                                Button(onClick = onIrPerfil) {
                                    Text("Ir a Perfil")
                                }
                            } else {
                                Button(
                                    onClick = onNuevaSolicitud, enabled = !uiState.guardando
                                ) {
                                    Text("Nueva solicitud")
                                }
                            }

                            if (!uiState.mostrarFormulario) {
                                uiState.errorOperacion?.let { error ->
                                    Text(
                                        text = error, color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }

                    if (uiState.solicitudes.isEmpty()) {
                        item {
                            Text("Todavía no tienes solicitudes simuladas.")
                        }
                    }

                    items(uiState.solicitudes, key = { it.id }) { solicitud ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = solicitud.codigo,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text("Categoría: ${solicitud.categoria.nombre}")
                                Text("Estado: ${solicitud.estado.etiqueta}")
                                Text(
                                    "Creada: ${formatoFecha.format(Date(solicitud.creadaEn))}"
                                )
                                TextButton(
                                    onClick = { onAbrirDetalle(solicitud.id) }) {
                                    Text("Ver detalle")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.mostrarFormulario) {
        SolicitudDialogo(
            uiState = uiState,
            onCategoriaChange = onCategoriaChange,
            onGuardar = onGuardar,
            onCerrar = onCerrarFormulario
        )
    }
}