package cl.duoc.tiotecpreventivo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo
import cl.duoc.tiotecpreventivo.ui.theme.TioTecPreventivoTheme
import cl.duoc.tiotecpreventivo.viewmodel.DispositivosUiState

@Composable
fun DispositivoFormulario(
    uiState: DispositivosUiState,
    onNombreChange: (String) -> Unit,
    onSistemaChange: (SistemaOperativo) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (uiState.dispositivoEnEdicionId == null) {
                "Agregar dispositivo"
            } else {
                "Editar dispositivo"
            }, style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = uiState.nombre,
            onValueChange = onNombreChange,
            label = { Text("Nombre ficticio") },
            singleLine = true,
            enabled = !uiState.guardando,
            isError = uiState.errorNombre != null,
            supportingText = {
                Text(
                    uiState.errorNombre ?: "Entre 2 y 30 caracteres. Ejemplo: Teléfono de prueba."
                )
            },
            trailingIcon = {
                if (uiState.errorNombre != null) {
                    IconoErrorDispositivo()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Sistema operativo", style = MaterialTheme.typography.titleMedium
        )

        Column(
            modifier = Modifier.selectableGroup()
        ) {
            SistemaOperativo.entries.forEach { sistema ->
                val seleccionado = uiState.sistemaOperativo == sistema

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = seleccionado,
                            enabled = !uiState.guardando,
                            role = Role.RadioButton,
                            onClick = { onSistemaChange(sistema) })
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RadioButton(
                        selected = seleccionado, onClick = null, enabled = !uiState.guardando
                    )

                    Text(sistema.etiqueta)
                }
            }
        }

        uiState.errorSistemaOperativo?.let { error ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconoErrorDispositivo()

                Text(
                    text = error, color = MaterialTheme.colorScheme.error
                )
            }
        }

        uiState.errorOperacion?.let { error ->
            Text(
                text = error, color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = onGuardar, enabled = !uiState.guardando, modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (uiState.guardando) "Guardando…"
                else "Guardar dispositivo"
            )
        }

        OutlinedButton(
            onClick = onCancelar, enabled = !uiState.guardando, modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }
    }
}

@Composable
private fun IconoErrorDispositivo() {
    Icon(
        painter = painterResource(android.R.drawable.ic_dialog_alert),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.error
    )
}

@Preview(showBackground = true)
@Composable
fun DispositivoFormularioPreview() {
    TioTecPreventivoTheme {
        DispositivoFormulario(
            uiState = DispositivosUiState(
            cargando = false, formularioVisible = true
        ),
            onNombreChange = {},
            onSistemaChange = {},
            onGuardar = {},
            onCancelar = {},
            modifier = Modifier.padding(20.dp)
        )
    }
}