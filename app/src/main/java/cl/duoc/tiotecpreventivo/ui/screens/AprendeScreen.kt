package cl.duoc.tiotecpreventivo.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.ui.theme.TioTecPreventivoTheme
import cl.duoc.tiotecpreventivo.viewmodel.AprendeUiState

@Composable
fun AprendeScreen(
    modifier: Modifier = Modifier,
    uiState: AprendeUiState = AprendeUiState(),
    onCategoriaChange: (Categoria?) -> Unit = {},
    onAbrirContenido: (String) -> Unit = {},
    onReintentar: () -> Unit = {}
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Aprende", style = MaterialTheme.typography.headlineMedium
                )
                Text("Consejos para proteger tus dispositivos y tus cuentas.")

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.categoriaSeleccionada == null,
                        onClick = { onCategoriaChange(null) },
                        label = { Text("Todos") })

                    uiState.categorias.forEach { categoria ->
                        FilterChip(
                            selected = uiState.categoriaSeleccionada == categoria,
                            onClick = { onCategoriaChange(categoria) },
                            label = { Text(categoria.nombre) })
                    }
                }
            }
        }

        when {
            uiState.cargando -> {
                item {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = uiState.error, color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = onReintentar) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            else -> {
                if (uiState.contenidos.isEmpty()) {
                    item {
                        Text("No hay contenidos disponibles en esta categoría.")
                    }
                }

                items(uiState.contenidos, key = { it.id }) { contenido ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = contenido.categoria.nombre,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = contenido.titulo,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(contenido.resumen)

                            TextButton(
                                onClick = { onAbrirContenido(contenido.id) }) {
                                Text("Leer contenido")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AprendePreview() {
    TioTecPreventivoTheme {
        AprendeScreen(uiState = AprendeUiState(cargando = false))
    }
}