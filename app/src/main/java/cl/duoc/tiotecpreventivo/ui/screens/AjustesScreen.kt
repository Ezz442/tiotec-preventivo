package cl.duoc.tiotecpreventivo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.data.preferences.Tema
import cl.duoc.tiotecpreventivo.ui.theme.TioTecPreventivoTheme
import cl.duoc.tiotecpreventivo.viewmodel.PreferenciasUiState
import cl.duoc.tiotecpreventivo.viewmodel.PreferenciasViewModel

/**
 * Punto de entrada con ViewModel: observa el StateFlow de preferencias, reenvía
 * los eventos y muestra un Snackbar si algo no se pudo guardar.
 */
@Composable
fun AjustesRuta(
    viewModel: PreferenciasViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.onMensajeMostrado()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AjustesScreen(
            uiState = uiState,
            onTemaChange = viewModel::onTemaChange,
            onAvisosChange = viewModel::onAvisosChange
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/** Pantalla sin estado propio: recibe el UiState y avisa los cambios hacia arriba. */
@Composable
fun AjustesScreen(
    uiState: PreferenciasUiState,
    onTemaChange: (Tema) -> Unit,
    onAvisosChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (uiState.cargando) {
            CircularProgressIndicator()
            return@Column
        }

        Text(
            text = "Apariencia",
            style = MaterialTheme.typography.titleMedium
        )

        Column(modifier = Modifier.selectableGroup()) {
            Tema.entries.forEach { tema ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = uiState.tema == tema,
                            onClick = { onTemaChange(tema) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.tema == tema,
                        onClick = null // el clic lo maneja toda la fila
                    )
                    Text(
                        text = tema.etiqueta,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }

        HorizontalDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = uiState.avisosActivos,
                    onValueChange = onAvisosChange,
                    role = Role.Switch
                )
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Recordatorios",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Recibe avisos para revisar tus acciones preventivas.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Switch(
                checked = uiState.avisosActivos,
                onCheckedChange = null // el clic lo maneja toda la fila
            )
        }

        Text(
            text = "Tus ajustes se guardan en este teléfono y se mantienen al cerrar la app.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, name = "Ajustes claro")
@Composable
fun AjustesClaroPreview() {
    TioTecPreventivoTheme(darkTheme = false) {
        AjustesScreen(
            uiState = PreferenciasUiState(cargando = false),
            onTemaChange = {},
            onAvisosChange = {}
        )
    }
}

@Preview(showBackground = true, name = "Ajustes oscuro")
@Composable
fun AjustesOscuroPreview() {
    TioTecPreventivoTheme(darkTheme = true) {
        AjustesScreen(
            uiState = PreferenciasUiState(
                tema = Tema.OSCURO,
                avisosActivos = false,
                cargando = false
            ),
            onTemaChange = {},
            onAvisosChange = {}
        )
    }
}
