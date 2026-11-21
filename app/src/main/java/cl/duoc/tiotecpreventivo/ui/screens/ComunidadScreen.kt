package cl.duoc.tiotecpreventivo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.viewmodel.PostsUiState

// #pantalla que muestra los datos de la api externa
/** TT-30: textos traídos de una API pública externa (JSONPlaceholder) con Retrofit. */
@Composable
fun ComunidadScreen(
    modifier: Modifier = Modifier,
    uiState: PostsUiState = PostsUiState(),
    onReintentar: () -> Unit = {}
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Consejos de la comunidad", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Contenido de demostración obtenido de JSONPlaceholder, una API pública externa. " +
                        "No es parte de los microservicios de TíoTec ni se guarda en tu teléfono.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        when {
            uiState.cargando -> item {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }

            uiState.error != null -> item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(uiState.error, color = MaterialTheme.colorScheme.error)
                    Button(onClick = onReintentar) { Text("Reintentar") }
                }
            }

            uiState.vacio -> item { Text("Aún no hay consejos para mostrar.") }

            else -> items(uiState.consejos, key = { it.id }) { consejo ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(consejo.titulo, style = MaterialTheme.typography.titleMedium)
                        Text(consejo.texto, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
