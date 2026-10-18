package cl.duoc.tiotecpreventivo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.ui.components.TarjetaInformativa
import cl.duoc.tiotecpreventivo.ui.theme.TioTecPreventivoTheme

@Composable
fun AccionesScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Mis acciones",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Organiza las medidas para mejorar tu seguridad digital.",
            style = MaterialTheme.typography.bodyLarge
        )

        TarjetaInformativa(
            titulo = "Tu plan preventivo",
            descripcion = "Aquí podrás consultar las acciones recomendadas después de realizar una evaluación."
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AccionesPreview() {
    TioTecPreventivoTheme {
        AccionesScreen()
    }
}
