package cl.duoc.tiotecpreventivo.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import cl.duoc.tiotecpreventivo.TioTecApplication
import cl.duoc.tiotecpreventivo.data.local.InicioRepositoryLocal
import cl.duoc.tiotecpreventivo.ui.screens.AccionesScreen
import cl.duoc.tiotecpreventivo.ui.screens.AjustesRuta
import cl.duoc.tiotecpreventivo.ui.screens.AjustesScreen
import cl.duoc.tiotecpreventivo.ui.screens.AprendeScreen
import cl.duoc.tiotecpreventivo.ui.screens.ContenidoScreen
import cl.duoc.tiotecpreventivo.ui.screens.DispositivosScreen
import cl.duoc.tiotecpreventivo.ui.screens.HistorialScreen
import cl.duoc.tiotecpreventivo.ui.screens.InicioScreen
import cl.duoc.tiotecpreventivo.ui.screens.PerfilScreen
import cl.duoc.tiotecpreventivo.ui.screens.ResultadoScreen
import cl.duoc.tiotecpreventivo.ui.theme.TioTecPreventivoTheme
import cl.duoc.tiotecpreventivo.viewmodel.AccionesViewModel
import cl.duoc.tiotecpreventivo.viewmodel.AccionesViewModelFactory
import cl.duoc.tiotecpreventivo.viewmodel.DispositivosViewModel
import cl.duoc.tiotecpreventivo.viewmodel.DispositivosViewModelFactory
import cl.duoc.tiotecpreventivo.viewmodel.HistorialViewModel
import cl.duoc.tiotecpreventivo.viewmodel.HistorialViewModelFactory
import cl.duoc.tiotecpreventivo.viewmodel.InicioViewModel
import cl.duoc.tiotecpreventivo.viewmodel.InicioViewModelFactory
import cl.duoc.tiotecpreventivo.viewmodel.PerfilViewModel
import cl.duoc.tiotecpreventivo.viewmodel.PerfilViewModelFactory
import cl.duoc.tiotecpreventivo.viewmodel.PreferenciasUiState
import cl.duoc.tiotecpreventivo.viewmodel.PreferenciasViewModel
import cl.duoc.tiotecpreventivo.viewmodel.ResultadoViewModel
import cl.duoc.tiotecpreventivo.viewmodel.ResultadoViewModelFactory
import cl.duoc.tiotecpreventivo.viewmodel.AprendeViewModel
import cl.duoc.tiotecpreventivo.viewmodel.AprendeViewModelFactory

private const val RUTA_DISPOSITIVOS = "dispositivos"
private const val GRAFO_DISPOSITIVOS = "gestion_dispositivos"
private const val RUTA_AJUSTES = "ajustes"
private const val RUTA_EVALUACION = "evaluacion/{dispositivoId}"
private const val RUTA_RESULTADO = "resultado/{evaluacionId}"
private const val RUTA_CONTENIDO = "contenido/{contenidoId}"
private const val RUTA_HISTORIAL = "historial"

private enum class DestinoPrincipal(
    val ruta: String, val titulo: String, val simbolo: String
) {
    INICIO("inicio", "Inicio", "⌂"), ACCIONES("acciones", "Mis acciones", "✓"), APRENDE(
        "aprende", "Aprende", "?"
    ),
    PERFIL("perfil", "Perfil", "○")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TioTecApp(
    windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    /** Lo crea MainActivity para que Ajustes y el tema usen la misma instancia. Null en previews. */
    preferenciasViewModel: PreferenciasViewModel? = null
) {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    val enEvaluacion = rutaActual == RUTA_EVALUACION
    val enDispositivos = rutaActual == RUTA_DISPOSITIVOS
    val enAjustes = rutaActual == RUTA_AJUSTES
    val enResultado = rutaActual == RUTA_RESULTADO
    val enContenido = rutaActual == RUTA_CONTENIDO
    val enHistorial = rutaActual == RUTA_HISTORIAL
    val enPantallaSecundaria =
        enDispositivos || enAjustes || enResultado || enContenido || enHistorial
    val esCompacto = windowWidthSizeClass == WindowWidthSizeClass.Compact

    val destinoSeleccionado = when {
        enDispositivos || enResultado || enHistorial -> DestinoPrincipal.INICIO.ruta
        enAjustes -> DestinoPrincipal.PERFIL.ruta
        enContenido -> DestinoPrincipal.APRENDE.ruta
        else -> rutaActual
    }

    val navegarPrincipal: (DestinoPrincipal) -> Unit = { destino ->
        navController.navigate(destino.ruta) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    PermanentNavigationDrawer(
        modifier = Modifier.fillMaxSize(), drawerContent = {
            if (!esCompacto && !enEvaluacion) {
                PermanentDrawerSheet {
                    Text(
                        text = "TíoTec Preventivo",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(24.dp)
                    )

                    DestinoPrincipal.entries.forEach { destino ->
                        NavigationDrawerItem(
                            label = {
                                Text(destino.titulo)
                            }, icon = {
                                Text(destino.simbolo)
                            }, selected = destinoSeleccionado == destino.ruta, onClick = {
                                navegarPrincipal(destino)
                            }, modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(title = {
                    Text(
                        when {
                            enDispositivos -> "Mis dispositivos"
                            enAjustes -> "Ajustes"
                            enEvaluacion -> "Evaluación preventiva"
                            enResultado -> "Resultados"
                            enContenido -> "Aprende"
                            enHistorial -> "Historial"
                            else -> "TíoTec Preventivo"
                        }
                    )
                }, navigationIcon = {
                    if (enPantallaSecundaria) {
                        TextButton(
                            onClick = {
                                navController.popBackStack()
                            }) {
                            Text("← Volver")
                        }
                    }
                })
            },
            bottomBar = {
                if (esCompacto && !enEvaluacion) {
                    NavigationBar {
                        DestinoPrincipal.entries.forEach { destino ->
                            NavigationBarItem(
                                selected = destinoSeleccionado == destino.ruta,
                                onClick = {
                                    navegarPrincipal(destino)
                                },
                                icon = {
                                    Text(
                                        text = destino.simbolo,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                },
                                label = {
                                    Text(destino.titulo)
                                })
                        }
                    }
                }
            }) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = DestinoPrincipal.INICIO.ruta,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                composable(DestinoPrincipal.INICIO.ruta) {
                    val context = LocalContext.current
                    val contenedor = (context.applicationContext as TioTecApplication).contenedor

                    val factory = remember(contenedor) {
                        InicioViewModelFactory(
                            InicioRepositoryLocal(
                                perfilRepository = contenedor.perfilRepository,
                                accionRepository = contenedor.accionRepository
                            )
                        )
                    }

                    val inicioViewModel: InicioViewModel = viewModel(
                        factory = factory
                    )

                    val uiState by inicioViewModel.uiState.collectAsState()

                    InicioScreen(uiState = uiState, onVerDispositivos = {
                        navController.navigate(GRAFO_DISPOSITIVOS) {
                            launchSingleTop = true
                        }
                    }, onVerHistorial = {
                        navController.navigate(RUTA_HISTORIAL) {
                            launchSingleTop = true
                        }
                    })
                }

                composable(RUTA_HISTORIAL) {
                    val contenedor =
                        (LocalContext.current.applicationContext as TioTecApplication).contenedor

                    val factory = remember(contenedor) {
                        HistorialViewModelFactory(
                            evaluacionRepository = contenedor.evaluacionRepository,
                            dispositivoRepository = contenedor.dispositivoRepository
                        )
                    }

                    val historialViewModel: HistorialViewModel = viewModel(factory = factory)
                    val uiState by historialViewModel.uiState.collectAsState()

                    HistorialScreen(
                        uiState = uiState,
                        onSeleccionarDispositivo = historialViewModel::seleccionarDispositivo,
                        // El detalle es la misma pantalla de resultados (TT-12); "Volver" regresa aquí.
                        onAbrirEvaluacion = { evaluacionId ->
                            navController.navigate("resultado/$evaluacionId") {
                                launchSingleTop = true
                            }
                        },
                        onIrADispositivos = {
                            navController.navigate(GRAFO_DISPOSITIVOS) {
                                launchSingleTop = true
                            }
                        },
                        onReintentar = historialViewModel::reintentar
                    )
                }

                composable(DestinoPrincipal.ACCIONES.ruta) {
                    val context = LocalContext.current
                    val contenedor = (context.applicationContext as TioTecApplication).contenedor

                    val factory = remember(contenedor) {
                        AccionesViewModelFactory(contenedor.accionRepository)
                    }

                    val accionesViewModel: AccionesViewModel = viewModel(factory = factory)
                    val uiState by accionesViewModel.uiState.collectAsState()

                    AccionesScreen(
                        uiState = uiState,
                        onFiltroChange = accionesViewModel::cambiarFiltro,
                        onCambiarEstado = accionesViewModel::cambiarEstado,
                        onReintentar = accionesViewModel::reintentarCarga
                    )
                }

                composable(DestinoPrincipal.APRENDE.ruta) {
                    val context = LocalContext.current
                    val contenedor = (context.applicationContext as TioTecApplication).contenedor

                    val factory = remember(contenedor) {
                        AprendeViewModelFactory(contenedor.catalogoRepository)
                    }

                    val aprendeViewModel: AprendeViewModel = viewModel(factory = factory)
                    val uiState by aprendeViewModel.uiState.collectAsState()

                    AprendeScreen(
                        uiState = uiState,
                        onCategoriaChange = aprendeViewModel::seleccionarCategoria,
                        onReintentar = aprendeViewModel::reintentar,
                        onAbrirContenido = { contenidoId ->
                            navController.navigate("contenido/$contenidoId") {
                                launchSingleTop = true
                            }
                        })
                }

                composable(DestinoPrincipal.PERFIL.ruta) {
                    val context = LocalContext.current

                    val contenedor = (context.applicationContext as TioTecApplication).contenedor

                    val factory = remember(contenedor) {
                        PerfilViewModelFactory(contenedor.perfilRepository)
                    }

                    val perfilViewModel: PerfilViewModel = viewModel(
                        factory = factory
                    )

                    val uiState by perfilViewModel.uiState.collectAsState()

                    PerfilScreen(
                        uiState = uiState,
                        onAliasChange = perfilViewModel::onAliasChange,
                        onExperienciaChange = perfilViewModel::onExperienciaChange,
                        onGuardar = perfilViewModel::guardarPerfil,
                        onReintentar = perfilViewModel::reintentarCarga,
                        onAbrirAjustes = {
                            navController.navigate(RUTA_AJUSTES) {
                                launchSingleTop = true
                            }
                        })
                }

                composable(RUTA_AJUSTES) {
                    if (preferenciasViewModel != null) {
                        AjustesRuta(viewModel = preferenciasViewModel)
                    } else {
                        // Solo en previews, donde no hay DataStore.
                        AjustesScreen(
                            uiState = PreferenciasUiState(cargando = false),
                            onTemaChange = {},
                            onAvisosChange = {})
                    }
                }

                navigation(
                    startDestination = RUTA_DISPOSITIVOS, route = GRAFO_DISPOSITIVOS
                ) {
                    composable(RUTA_DISPOSITIVOS) {
                        val context = LocalContext.current

                        val contenedor =
                            (context.applicationContext as TioTecApplication).contenedor

                        val factory = remember(contenedor) {
                            DispositivosViewModelFactory(
                                dispositivoRepository = contenedor.dispositivoRepository,
                                perfilRepository = contenedor.perfilRepository
                            )
                        }

                        val dispositivosViewModel: DispositivosViewModel = viewModel(
                            factory = factory
                        )

                        val uiState by dispositivosViewModel.uiState.collectAsState()

                        DispositivosScreen(
                            uiState = uiState,
                            onAgregar = dispositivosViewModel::abrirNuevo,
                            onEditar = dispositivosViewModel::editarDispositivo,
                            onSolicitarEliminar = dispositivosViewModel::solicitarEliminacion,
                            onConfirmarEliminar = dispositivosViewModel::confirmarEliminacion,
                            onCancelarEliminar = dispositivosViewModel::cancelarEliminacion,
                            onNombreChange = dispositivosViewModel::onNombreChange,
                            onSistemaChange = dispositivosViewModel::onSistemaOperativoChange,
                            onGuardar = dispositivosViewModel::guardarDispositivo,
                            onCancelarFormulario = dispositivosViewModel::cancelarFormulario,
                            onReintentar = dispositivosViewModel::reintentarCarga,
                            onEvaluar = { dispositivoId ->
                                navController.navigate("evaluacion/$dispositivoId") {
                                    launchSingleTop = true
                                }
                            },
                            onIrAPerfil = {
                                navegarPrincipal(DestinoPrincipal.PERFIL)
                            },
                            onConsumirMensaje = dispositivosViewModel::consumirMensaje
                        )
                    }

                    composable(
                        route = RUTA_EVALUACION, arguments = listOf(
                            navArgument("dispositivoId") {
                                type = NavType.LongType
                            })
                    ) { entrada ->
                        val dispositivoId = requireNotNull(
                            entrada.arguments
                        ).getLong("dispositivoId")

                        EvaluacionFlujo(dispositivoId = dispositivoId, onSalir = {
                            navController.popBackStack()
                        }, onVerResultado = { evaluacionId ->
                            // Se quita la evaluación de la pila: "Volver" lleva a Mis dispositivos.
                            navController.navigate("resultado/$evaluacionId") {
                                popUpTo(RUTA_EVALUACION) { inclusive = true }
                                launchSingleTop = true
                            }
                        })
                    }

                    composable(
                        route = RUTA_RESULTADO,
                        arguments = listOf(navArgument("evaluacionId") { type = NavType.LongType })
                    ) { entrada ->
                        val evaluacionId = requireNotNull(entrada.arguments).getLong("evaluacionId")
                        val contenedor =
                            (LocalContext.current.applicationContext as TioTecApplication).contenedor

                        val factory = remember(contenedor, evaluacionId) {
                            ResultadoViewModelFactory(
                                evaluacionId = evaluacionId,
                                evaluacionRepository = contenedor.evaluacionRepository,
                                catalogoRepository = contenedor.catalogoRepository
                            )
                        }

                        val resultadoViewModel: ResultadoViewModel = viewModel(factory = factory)
                        val uiState by resultadoViewModel.uiState.collectAsState()

                        ResultadoScreen(
                            uiState = uiState,
                            onReintentar = resultadoViewModel::reintentar,
                            onVerAcciones = { navegarPrincipal(DestinoPrincipal.ACCIONES) },
                            onAprender = { contenidoId ->
                                navController.navigate("contenido/$contenidoId") {
                                    launchSingleTop = true
                                }
                            },
                            onVolver = { navController.popBackStack() })
                    }
                }

                composable(
                    route = RUTA_CONTENIDO,
                    arguments = listOf(navArgument("contenidoId") { type = NavType.StringType })
                ) { entrada ->
                    val contenidoId = entrada.arguments?.getString("contenidoId").orEmpty()
                    val contenedor =
                        (LocalContext.current.applicationContext as TioTecApplication).contenedor
                    val contenido = remember(contenidoId) {
                        contenedor.catalogoRepository.contenido(contenidoId)
                    }

                    ContenidoScreen(
                        contenido = contenido, onVolver = { navController.popBackStack() })
                }
            }
        }
    }
}

@Preview(
    showBackground = true, widthDp = 360, heightDp = 800, name = "Navegación Compact"
)
@Composable
fun TioTecCompactPreview() {
    TioTecPreventivoTheme {
        TioTecApp(
            windowWidthSizeClass = WindowWidthSizeClass.Compact
        )
    }
}

@Preview(
    showBackground = true, widthDp = 1200, heightDp = 800, name = "Navegación Expanded"
)
@Composable
fun TioTecExpandedPreview() {
    TioTecPreventivoTheme {
        TioTecApp(
            windowWidthSizeClass = WindowWidthSizeClass.Expanded
        )
    }
}