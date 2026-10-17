package cl.duoc.tiotecpreventivo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.ui.theme.TioTecPreventivoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text("TíoTec Preventivo")
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
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

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Comienza con un dispositivo",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Registra un equipo ficticio para realizar tu primera evaluación preventiva.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
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