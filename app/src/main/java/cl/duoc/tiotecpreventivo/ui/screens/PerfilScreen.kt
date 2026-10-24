package cl.duoc.tiotecpreventivo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.data.local.entity.ExperienciaDigital
import cl.duoc.tiotecpreventivo.ui.components.GrupoOpciones
import cl.duoc.tiotecpreventivo.ui.components.MensajeError
import cl.duoc.tiotecpreventivo.ui.components.MensajeInformativo
import cl.duoc.tiotecpreventivo.ui.components.TextoConCarga
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
    onAbrirAjustes: () -> Unit = {},
    onAbrirSolicitudes: () -> Unit = {}
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
                MensajeError(uiState.errorGeneral ?: "No pudimos cargar tu perfil.")

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

                GrupoOpciones(
                    opciones = ExperienciaDigital.entries,
                    seleccionada = uiState.experienciaDigital,
                    etiqueta = { it.etiqueta },
                    habilitado = !uiState.guardando,
                    onSeleccionar = onExperienciaChange
                )

                MensajeError(uiState.errorExperiencia)

                Button(
                    onClick = onGuardar,
                    enabled = !uiState.guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextoConCarga(texto = "Guardar perfil", cargando = uiState.guardando)
                }

                MensajeError(uiState.errorGeneral)

                MensajeInformativo(uiState.mensaje)
            }
        }

        OutlinedButton(
            onClick = onAbrirSolicitudes, modifier = Modifier.fillMaxWidth()
        ) {
            Text("Solicitudes de asistencia")
        }

        OutlinedButton(
            onClick = onAbrirAjustes, modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ajustes")
        }
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