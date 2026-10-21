package cl.duoc.tiotecpreventivo.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cl.duoc.tiotecpreventivo.TioTecApplication
import cl.duoc.tiotecpreventivo.ui.screens.EvaluacionScreen
import cl.duoc.tiotecpreventivo.ui.screens.RevisionEvaluacionScreen
import cl.duoc.tiotecpreventivo.viewmodel.EvaluacionViewModel
import cl.duoc.tiotecpreventivo.viewmodel.EvaluacionViewModelFactory

@Composable
fun EvaluacionFlujo(
    dispositivoId: Long, onSalir: () -> Unit
) {
    val context = LocalContext.current
    val contenedor = (context.applicationContext as TioTecApplication).contenedor

    val factory = remember(contenedor, dispositivoId) {
        EvaluacionViewModelFactory(
            dispositivoId = dispositivoId,
            catalogoRepository = contenedor.catalogoRepository,
            dispositivoRepository = contenedor.dispositivoRepository,
            evaluacionRepository = contenedor.evaluacionRepository
        )
    }

    val evaluacionViewModel: EvaluacionViewModel = viewModel(
        factory = factory
    )

    val uiState by evaluacionViewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val enRevision = entradaActual?.destination?.route == "revision"

    val solicitarSalida: () -> Unit = {
        if (evaluacionViewModel.solicitarSalida()) {
            onSalir()
        }
    }

    if (uiState.evaluacionGuardadaId != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Evaluación guardada", style = MaterialTheme.typography.headlineMedium
            )

            Text("Tus respuestas y resultados quedaron guardados.")

            Button(onClick = onSalir) {
                Text("Volver a mis dispositivos")
            }
        }
    } else {
        NavHost(
            navController = navController, startDestination = "preguntas"
        ) {
            composable("preguntas") {
                EvaluacionScreen(
                    uiState = uiState,
                    onRespuestaChange = evaluacionViewModel::onRespuestaChange,
                    onAnterior = evaluacionViewModel::anterior,
                    onSiguiente = evaluacionViewModel::siguiente,
                    onRevisar = {
                        if (evaluacionViewModel.prepararRevision()) {
                            navController.navigate("revision") {
                                launchSingleTop = true
                            }
                        }
                    },
                    onSalir = solicitarSalida,
                    onReintentar = evaluacionViewModel::reintentarCarga
                )
            }

            composable("revision") {
                RevisionEvaluacionScreen(
                    uiState = uiState, onEditarRespuesta = { indice ->
                    if (evaluacionViewModel.irAPregunta(indice)) {
                        navController.popBackStack()
                    }
                }, onFinalizar = evaluacionViewModel::finalizarEvaluacion, onVolver = {
                    navController.popBackStack()
                }, onSalir = solicitarSalida
                )
            }
        }
    }

    BackHandler {
        when {
            uiState.guardando -> Unit
            uiState.evaluacionGuardadaId != null -> onSalir()
            enRevision -> navController.popBackStack()
            else -> solicitarSalida()
        }
    }

    if (uiState.mostrarConfirmarSalida) {
        AlertDialog(onDismissRequest = evaluacionViewModel::cancelarSalida, title = {
            Text("¿Salir sin guardar?")
        }, text = {
            Text("Las respuestas de esta evaluación se perderán.")
        }, confirmButton = {
            TextButton(
                onClick = {
                    if (evaluacionViewModel.confirmarSalida()) {
                        onSalir()
                    }
                }) {
                Text("Salir sin guardar")
            }
        }, dismissButton = {
            TextButton(
                onClick = evaluacionViewModel::cancelarSalida
            ) {
                Text("Seguir respondiendo")
            }
        })
    }
}