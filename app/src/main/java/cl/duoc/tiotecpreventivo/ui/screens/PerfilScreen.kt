package cl.duoc.tiotecpreventivo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.ui.theme.TioTecPreventivoTheme

@Composable
fun PerfilScreen(
    modifier: Modifier = Modifier,
    onAbrirAjustes: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Mi perfil",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Configura tu perfil ficticio y tu experiencia digital.",
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = "Usa un alias de prueba y evita ingresar datos personales reales.",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedButton(
            onClick = onAbrirAjustes,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ajustes")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PerfilPreview() {
    TioTecPreventivoTheme {
        PerfilScreen()
    }
}