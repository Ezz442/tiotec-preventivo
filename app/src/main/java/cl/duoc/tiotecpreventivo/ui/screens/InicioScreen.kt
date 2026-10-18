package cl.duoc.tiotecpreventivo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.ui.theme.TioTecPreventivoTheme
import androidx.compose.material3.Button
import cl.duoc.tiotecpreventivo.ui.components.TarjetaInformativa
@Composable
fun InicioScreen(
    modifier: Modifier = Modifier,
    onVerDispositivos: () -> Unit = {}
) {    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Tu prevención digital",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Revisa tus hábitos y organiza acciones para proteger tus dispositivos.",
            style = MaterialTheme.typography.bodyLarge
        )

        TarjetaInformativa(
            titulo = "Comienza con un dispositivo",
            descripcion = "Registra un equipo ficticio para realizar tu primera evaluación preventiva."
        )

        Button(
            onClick = onVerDispositivos,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Mis dispositivos")
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