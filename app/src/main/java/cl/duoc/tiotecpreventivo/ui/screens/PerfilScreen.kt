package cl.duoc.tiotecpreventivo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import cl.duoc.tiotecpreventivo.data.local.entity.ExperienciaDigital
import cl.duoc.tiotecpreventivo.ui.theme.TioTecPreventivoTheme
import cl.duoc.tiotecpreventivo.viewmodel.PerfilUiState

@Composable
fun PerfilScreen(
    modifier: Modifier = Modifier,
    uiState: PerfilUiState = PerfilUiState(),
    onAliasChange: (String) -> Unit = {},
    onExperienciaChange: (ExperienciaDigital) -> Unit = {},
    onGuardar: () -> Unit = {},
    onReintentar: () -> Unit = {},
    onAbrirAjustes: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Mi perfil", style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Usa un alias ficticio y evita ingresar datos personales reales.",
            style = MaterialTheme.typography.bodyLarge
        )

        when {
            uiState.cargando -> {
                CircularProgressIndicator()
                Text("Cargando perfil…")
            }

            !uiState.perfilCargado -> {
                ErrorPerfil(
                    texto = uiState.errorGeneral ?: "No pudimos cargar tu perfil."
                )

                Button(onClick = onReintentar) {
                    Text("Reintentar")
                }
            }

            else -> {
                OutlinedTextField(
                    value = uiState.alias,
                    onValueChange = onAliasChange,
                    label = { Text("Alias ficticio") },
                    singleLine = true,
                    enabled = !uiState.guardando,
                    isError = uiState.errorAlias != null,
                    supportingText = {
                        Text(
                            uiState.errorAlias ?: "Entre 2 y 30 caracteres."
                        )
                    },
                    trailingIcon = {
                        if (uiState.errorAlias != null) {
                            Icon(
                                painter = painterResource(
                                    android.R.drawable.ic_dialog_alert
                                ), contentDescription = null, tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Nivel de experiencia digital",
                    style = MaterialTheme.typography.titleMedium
                )

                Column(
                    modifier = Modifier.selectableGroup()
                ) {
                    ExperienciaDigital.entries.forEach { experiencia ->
                        val seleccionado = uiState.experienciaDigital == experiencia

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = seleccionado,
                                    enabled = !uiState.guardando,
                                    role = Role.RadioButton,
                                    onClick = {
                                        onExperienciaChange(experiencia)
                                    })
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RadioButton(
                                selected = seleccionado,
                                onClick = null,
                                enabled = !uiState.guardando
                            )

                            Text(experiencia.etiqueta)
                        }
                    }
                }

                uiState.errorExperiencia?.let { error ->
                    ErrorPerfil(texto = error)
                }

                Button(
                    onClick = onGuardar,
                    enabled = !uiState.guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (uiState.guardando) "Guardando…"
                        else "Guardar perfil"
                    )
                }

                uiState.errorGeneral?.let { error ->
                    ErrorPerfil(texto = error)
                }

                uiState.mensaje?.let { mensaje ->
                    Text(text = mensaje)
                }
            }
        }

        OutlinedButton(
            onClick = onAbrirAjustes, modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ajustes")
        }
    }
}

@Composable
private fun ErrorPerfil(texto: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(android.R.drawable.ic_dialog_alert),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )

        Text(
            text = texto,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PerfilPreview() {
    TioTecPreventivoTheme {
        PerfilScreen(
            uiState = PerfilUiState(
                cargando = false, perfilCargado = true
            )
        )
    }
}