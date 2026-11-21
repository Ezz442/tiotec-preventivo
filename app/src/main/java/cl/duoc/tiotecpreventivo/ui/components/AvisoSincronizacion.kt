package cl.duoc.tiotecpreventivo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// #aviso cuando el microservicio no responde
/**
 * TT-29: estado de la conexión con un microservicio.
 * - Mientras se actualiza: indicador de carga con texto.
 * - Si el servicio no respondió: el error, aclaración de que se ven los datos guardados y Reintentar.
 * - Si todo salió bien no muestra nada.
 */
@Composable
fun AvisoSincronizacion(
    sincronizando: Boolean,
    error: String?,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        sincronizando -> Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            Text(
                text = "Actualizando desde el servicio…",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        error != null -> Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Mientras tanto se ven los datos guardados en el teléfono.",
                style = MaterialTheme.typography.bodySmall
            )
            TextButton(onClick = onReintentar) {
                Text("Reintentar")
            }
        }
    }
}
