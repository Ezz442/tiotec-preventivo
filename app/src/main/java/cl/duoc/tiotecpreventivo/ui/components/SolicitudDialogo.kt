package cl.duoc.tiotecpreventivo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import cl.duoc.tiotecpreventivo.R
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.viewmodel.SolicitudesUiState

@Composable
fun SolicitudDialogo(
    uiState: SolicitudesUiState,
    onCategoriaChange: (Categoria) -> Unit,
    onGuardar: () -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(onDismissRequest = {
        if (!uiState.guardando) onCerrar()
    }, title = {
        Text("Nueva solicitud simulada")
    }, text = {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Esta solicitud se guarda solo en tu teléfono. No se envía a ningún servicio.")
            Text(
                text = "Categoría obligatoria", style = MaterialTheme.typography.titleSmall
            )

            Categoria.entries.forEach { categoria ->
                FilterChip(
                    modifier = Modifier.fillMaxWidth(),
                    selected = uiState.categoriaSeleccionada == categoria,
                    onClick = { onCategoriaChange(categoria) },
                    enabled = !uiState.guardando,
                    label = { Text(categoria.nombre) })
            }

            uiState.errorCategoria?.let { error ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_riesgo_medio),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = error, color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Text("Estado inicial: Pendiente")

            uiState.errorOperacion?.let { error ->
                Text(
                    text = error, color = MaterialTheme.colorScheme.error
                )
            }
        }
    }, confirmButton = {
        Button(
            onClick = onGuardar, enabled = !uiState.guardando
        ) {
            Text(if (uiState.guardando) "Guardando..." else "Guardar")
        }
    }, dismissButton = {
        TextButton(
            onClick = onCerrar, enabled = !uiState.guardando
        ) {
            Text("Cancelar")
        }
    })
}