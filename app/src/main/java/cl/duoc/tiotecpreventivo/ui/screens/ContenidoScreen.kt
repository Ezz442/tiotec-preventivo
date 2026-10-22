package cl.duoc.tiotecpreventivo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.data.catalogo.CatalogoLocal
import cl.duoc.tiotecpreventivo.data.catalogo.Contenido
import cl.duoc.tiotecpreventivo.ui.theme.TioTecPreventivoTheme

/**
 * Detalle mínimo de un contenido educativo, para el botón "Aprender cómo hacerlo"
 * de los resultados (TT-12). TT-15 lo amplía con la lista y los filtros de Aprende.
 * El contenido es fijo y local (catálogo de TT-06): no necesita red ni estado propio.
 */
@Composable
fun ContenidoScreen(
    contenido: Contenido?,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (contenido == null) {
                Text(
                    text = "Este contenido no está disponible.",
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Text(
                    text = contenido.categoria.nombre,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = contenido.titulo,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.semantics { heading() }
                )
                Text(contenido.resumen, style = MaterialTheme.typography.titleMedium)
                Text(contenido.cuerpo, style = MaterialTheme.typography.bodyLarge)
            }

            OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContenidoPreview() {
    TioTecPreventivoTheme {
        ContenidoScreen(contenido = CatalogoLocal.contenidos.first(), onVolver = {})
    }
}

@Preview(showBackground = true, name = "Contenido no disponible")
@Composable
fun ContenidoNoDisponiblePreview() {
    TioTecPreventivoTheme {
        ContenidoScreen(contenido = null, onVolver = {})
    }
}
