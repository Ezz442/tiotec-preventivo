package cl.duoc.tiotecpreventivo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.ui.components.TarjetaInformativa
import cl.duoc.tiotecpreventivo.ui.theme.TioTecPreventivoTheme
import cl.duoc.tiotecpreventivo.viewmodel.InicioUiState

@Composable
fun InicioScreen(
    uiState: InicioUiState = InicioUiState(cargando = false),
    modifier: Modifier = Modifier,
    onVerDispositivos: () -> Unit = {},
    onVerHistorial: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Tu prevención digital", style = MaterialTheme.typography.headlineMedium
        )

        when {
            uiState.cargando -> {
                CircularProgressIndicator()

                Text(
                    text = "Cargando información…", style = MaterialTheme.typography.bodyLarge
                )
            }

            uiState.error != null -> {
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            else -> {
                Text(text = uiState.alias?.let { "Hola, $it." } ?: "Bienvenido a TíoTec.",
                    style = MaterialTheme.typography.bodyLarge)

                Text(
                    text = "Revisa tus hábitos y organiza acciones para proteger tus dispositivos.",
                    style = MaterialTheme.typography.bodyLarge
                )

                TarjetaInformativa(
                    titulo = "Tu plan preventivo",
                    descripcion = "Acciones pendientes: ${uiState.accionesPendientes}"
                )

                TarjetaInformativa(
                    titulo = "Comienza con un dispositivo",
                    descripcion = "Registra un equipo ficticio para realizar tu primera evaluación preventiva."
                )
            }
        }

        Button(
            onClick = onVerDispositivos,
            enabled = !uiState.cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Mis dispositivos")
        }

        OutlinedButton(
            onClick = onVerHistorial,
            enabled = !uiState.cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Historial de evaluaciones")
        }
    }
}

@Preview(showBackground = true, name = "Inicio claro")
@Composable
fun InicioClaroPreview() {
    TioTecPreventivoTheme(darkTheme = false) {
        InicioScreen()
    }
}

@Preview(showBackground = true, name = "Inicio oscuro")
@Composable
fun InicioOscuroPreview() {
    TioTecPreventivoTheme(darkTheme = true) {
        InicioScreen()
    }
}

@Preview(showBackground = true, name = "Inicio cargando")
@Composable
fun InicioCargandoPreview() {
    TioTecPreventivoTheme {
        InicioScreen(
            uiState = InicioUiState(cargando = true)
        )
    }
}