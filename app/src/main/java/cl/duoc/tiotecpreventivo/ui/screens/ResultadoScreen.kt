package cl.duoc.tiotecpreventivo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.data.catalogo.CatalogoLocal
import cl.duoc.tiotecpreventivo.data.catalogo.Prioridad
import cl.duoc.tiotecpreventivo.data.catalogo.ValorRespuesta
import cl.duoc.tiotecpreventivo.data.local.entity.NivelRiesgo
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo
import cl.duoc.tiotecpreventivo.domain.RecomendacionVista
import cl.duoc.tiotecpreventivo.domain.ResultadoCategoriaVista
import cl.duoc.tiotecpreventivo.domain.ResultadoEvaluacion
import cl.duoc.tiotecpreventivo.domain.RespuestaVista
import cl.duoc.tiotecpreventivo.domain.ReglasRiesgo
import cl.duoc.tiotecpreventivo.ui.components.BarraAnimada
import cl.duoc.tiotecpreventivo.ui.components.Movimiento
import cl.duoc.tiotecpreventivo.ui.components.NivelRiesgoIndicador
import cl.duoc.tiotecpreventivo.ui.components.colorNivel
import cl.duoc.tiotecpreventivo.ui.theme.TioTecPreventivoTheme
import cl.duoc.tiotecpreventivo.viewmodel.ResultadoUiState
import java.text.DateFormat
import java.util.Date
import java.util.Locale

private val ANCHO_MAXIMO = 720.dp

/** Resultados de una evaluación guardada (TT-12). Solo muestra: el estado viene del ViewModel. */
@Composable
fun ResultadoScreen(
    uiState: ResultadoUiState,
    onReintentar: () -> Unit,
    onVerAcciones: () -> Unit,
    onAprender: (contenidoId: String) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val anchoContenido = Modifier
            .widthIn(max = ANCHO_MAXIMO)
            .fillMaxWidth()

        val resultado = uiState.resultado

        when {
            uiState.cargando -> item {
                Column(anchoContenido, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator()
                    Text("Cargando resultado…")
                }
            }

            resultado == null -> item {
                Column(anchoContenido, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = uiState.error ?: "No encontramos esta evaluación.",
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = onReintentar) { Text("Reintentar") }
                    TextButton(onClick = onVolver) { Text("Volver") }
                }
            }

            else -> {
                item { ResumenGlobal(resultado, anchoContenido) }

                item {
                    Titulo("Resultado por categoría", anchoContenido)
                }
                items(resultado.categorias, key = { it.categoria.name }) { categoria ->
                    TarjetaCategoria(categoria, anchoContenido)
                }

                item {
                    Column(anchoContenido, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Titulo("Recomendaciones para ti", Modifier)
                        Text(
                            text = if (resultado.recomendaciones.isEmpty()) {
                                "No hay recomendaciones: respondiste \"Sí\" en todas las buenas prácticas."
                            } else {
                                "Ordenadas por prioridad. Cada una aparece por una de tus respuestas."
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                items(resultado.recomendaciones, key = { it.recomendacionId }) { recomendacion ->
                    TarjetaRecomendacion(recomendacion, onAprender, anchoContenido)
                }

                if (resultado.respuestas.isNotEmpty()) {
                    item { RespuestasGuardadas(resultado.respuestas, anchoContenido) }
                }

                item {
                    Column(anchoContenido, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (resultado.recomendaciones.isNotEmpty()) {
                            Button(onClick = onVerAcciones, modifier = Modifier.fillMaxWidth()) {
                                Text("Ver mis acciones (${resultado.recomendaciones.size})")
                            }
                        }
                        OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                            Text("Volver")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumenGlobal(resultado: ResultadoEvaluacion, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Resultado de tu evaluación",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() }
        )

        Text(
            text = "${resultado.nombreDispositivo} · ${resultado.sistemaOperativo.etiqueta} · ${formatearFecha(resultado.fecha)}",
            style = MaterialTheme.typography.bodyMedium
        )

        Card(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Nivel general", style = MaterialTheme.typography.labelLarge)
                NivelRiesgoIndicador(
                    nivel = resultado.nivelGlobal,
                    tamanoIcono = 32.dp,
                    estilo = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = textoNivelGlobal(resultado.nivelGlobal),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        OutlinedCard(Modifier.fillMaxWidth()) {
            Text(
                text = resultado.aviso,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun TarjetaCategoria(categoria: ResultadoCategoriaVista, modifier: Modifier) {
    Card(modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = categoria.categoria.nombre,
                style = MaterialTheme.typography.titleMedium
            )
            NivelRiesgoIndicador(categoria.nivel)
            // TT-25: la barra se llena al abrir el resultado; el texto de abajo dice el puntaje.
            BarraAnimada(
                fraccion = Movimiento.fraccion(categoria.puntaje, categoria.puntajeMaximo),
                color = colorNivel(categoria.nivel),
                desdeCero = true,
                duracion = Movimiento.DURACION_LARGA
            )
            Text(
                text = "Puntaje de riesgo: ${categoria.puntaje} de ${categoria.puntajeMaximo}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(categoria.explicacion, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TarjetaRecomendacion(
    recomendacion: RecomendacionVista,
    onAprender: (String) -> Unit,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${recomendacion.prioridad.etiqueta} · ${recomendacion.categoria.nombre}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (recomendacion.prioridad == Prioridad.ALTA) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            Text(recomendacion.titulo, style = MaterialTheme.typography.titleMedium)
            Text(recomendacion.descripcion, style = MaterialTheme.typography.bodyMedium)

            HorizontalDivider()

            Text(
                text = "Por tu respuesta \"${recomendacion.respuesta.etiqueta}\" a: ${recomendacion.textoPregunta}",
                style = MaterialTheme.typography.bodySmall
            )

            recomendacion.contenidoId?.let { contenidoId ->
                TextButton(onClick = { onAprender(contenidoId) }) {
                    Text("Aprender cómo hacerlo")
                }
            }
        }
    }
}

/** Respuestas tal como se guardaron (TT-14). Se muestran al pedirlas para no alargar la pantalla. */
@Composable
private fun RespuestasGuardadas(respuestas: List<RespuestaVista>, modifier: Modifier) {
    var visibles by rememberSaveable { mutableStateOf(false) }

    OutlinedCard(modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Titulo("Tus respuestas", Modifier)
            TextButton(onClick = { visibles = !visibles }) {
                Text(if (visibles) "Ocultar respuestas" else "Ver las ${respuestas.size} respuestas")
            }

            AnimatedVisibility(visible = visibles) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    respuestas.groupBy { it.categoria }.forEach { (categoria, deCategoria) ->
                        Text(
                            text = categoria.nombre,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        deCategoria.forEach { respuesta ->
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(respuesta.textoPregunta, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "Respuesta: ${respuesta.respuesta.etiqueta}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Titulo(texto: String, modifier: Modifier) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier.semantics { heading() }
    )
}

private fun textoNivelGlobal(nivel: NivelRiesgo): String = when (nivel) {
    NivelRiesgo.RIESGO_BAJO -> "Tus respuestas muestran buenas prácticas en todas las áreas."
    NivelRiesgo.RIESGO_MEDIO -> "Hay prácticas por mejorar. Revisa las recomendaciones de prioridad alta primero."
    NivelRiesgo.RIESGO_ALTO -> "Al menos un área necesita atención pronto. Empieza por las recomendaciones de prioridad alta."
}

private fun formatearFecha(fecha: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.forLanguageTag("es-CL"))
        .format(Date(fecha))

// ---------- Previews ----------

private fun resultadoDeEjemplo(): ResultadoEvaluacion {
    val pregunta = CatalogoLocal.preguntas.first { it.id == "RCU-01" }
    return ResultadoEvaluacion(
        evaluacionId = 1,
        nombreDispositivo = "Teléfono de prueba",
        sistemaOperativo = SistemaOperativo.ANDROID,
        fecha = 1_761_000_000_000,
        nivelGlobal = NivelRiesgo.RIESGO_ALTO,
        categorias = listOf(
            ResultadoCategoriaVista(pregunta.categoria, NivelRiesgo.RIESGO_ALTO, 5, 7,
                "Riesgo alto en Recuperación de cuentas: aplicas 1 de 3 buenas prácticas."),
        ),
        recomendaciones = listOf(
            RecomendacionVista(
                recomendacionId = "REC-RCU-01",
                titulo = "Actualizar los datos de recuperación",
                descripcion = "Revisa que el teléfono o correo de recuperación de tu cuenta principal esté vigente.",
                prioridad = Prioridad.ALTA,
                categoria = pregunta.categoria,
                textoPregunta = pregunta.texto,
                respuesta = ValorRespuesta.NO,
                accionId = 1,
                estadoAccion = null,
                contenidoId = "CON-RCU-01"
            )
        ),
        respuestas = CatalogoLocal.preguntas.take(3).map {
            RespuestaVista(it.id, it.categoria, it.texto, ValorRespuesta.NO_SE)
        },
        aviso = ReglasRiesgo.AVISO_ORIENTACION
    )
}

@Preview(showBackground = true, heightDp = 1400, name = "Resultado")
@Composable
fun ResultadoPreview() {
    TioTecPreventivoTheme {
        ResultadoScreen(
            uiState = ResultadoUiState(cargando = false, resultado = resultadoDeEjemplo()),
            onReintentar = {}, onVerAcciones = {}, onAprender = {}, onVolver = {}
        )
    }
}

@Preview(showBackground = true, name = "Resultado sin recomendaciones")
@Composable
fun ResultadoSinRecomendacionesPreview() {
    TioTecPreventivoTheme {
        ResultadoScreen(
            uiState = ResultadoUiState(
                cargando = false,
                resultado = resultadoDeEjemplo().copy(
                    nivelGlobal = NivelRiesgo.RIESGO_BAJO, categorias = emptyList(), recomendaciones = emptyList()
                )
            ),
            onReintentar = {}, onVerAcciones = {}, onAprender = {}, onVolver = {}
        )
    }
}

@Preview(showBackground = true, name = "Resultado con error")
@Composable
fun ResultadoErrorPreview() {
    TioTecPreventivoTheme {
        ResultadoScreen(
            uiState = ResultadoUiState(cargando = false, error = "No pudimos cargar el resultado."),
            onReintentar = {}, onVerAcciones = {}, onAprender = {}, onVolver = {}
        )
    }
}
