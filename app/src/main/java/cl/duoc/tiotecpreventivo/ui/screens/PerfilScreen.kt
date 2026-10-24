package cl.duoc.tiotecpreventivo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.data.local.entity.ExperienciaDigital
import cl.duoc.tiotecpreventivo.domain.ValidadorFormularios
import cl.duoc.tiotecpreventivo.ui.components.GrupoOpciones
import cl.duoc.tiotecpreventivo.ui.components.MensajeError
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
    onAbrirSolicitudes: () -> Unit = {},
    /** TT-26: el Snackbar ya mostró el resultado de guardar. */
    onConsumirMensaje: () -> Unit = {}
) {
    // TT-26: el resultado de guardar se muestra en un Snackbar; los errores siguen bajo cada campo.
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let { mensaje ->
            snackbar.showSnackbar(mensaje)
            onConsumirMensaje()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
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
                            // El error viene del ViewModel; si no hay error, la regla y el contador.
                            Text(
                                uiState.errorAlias
                                    ?: "Entre 2 y 30 caracteres, sin correos ni teléfonos. · ${ValidadorFormularios.contador(uiState.alias)}"
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

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
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