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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import cl.duoc.tiotecpreventivo.ui.screens.AccionesScreen
import cl.duoc.tiotecpreventivo.ui.screens.AprendeScreen
import cl.duoc.tiotecpreventivo.ui.screens.DispositivosScreen
import cl.duoc.tiotecpreventivo.ui.screens.InicioScreen
import cl.duoc.tiotecpreventivo.ui.screens.PerfilScreen
import androidx.compose.ui.tooling.preview.Preview
import cl.duoc.tiotecpreventivo.ui.theme.TioTecPreventivoTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.tiotecpreventivo.data.local.InicioRepositoryMemoria
import cl.duoc.tiotecpreventivo.viewmodel.InicioViewModel
import cl.duoc.tiotecpreventivo.viewmodel.InicioViewModelFactory
import cl.duoc.tiotecpreventivo.viewmodel.PreferenciasUiState
import cl.duoc.tiotecpreventivo.viewmodel.PreferenciasViewModel
import cl.duoc.tiotecpreventivo.ui.screens.AjustesRuta
import cl.duoc.tiotecpreventivo.ui.screens.AjustesScreen
import androidx.compose.ui.platform.LocalContext
import cl.duoc.tiotecpreventivo.TioTecApplication
import cl.duoc.tiotecpreventivo.viewmodel.PerfilViewModel
import cl.duoc.tiotecpreventivo.viewmodel.PerfilViewModelFactory

private const val RUTA_DISPOSITIVOS = "dispositivos"
private const val GRAFO_DISPOSITIVOS = "gestion_dispositivos"
private const val RUTA_AJUSTES = "ajustes"

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
    val enDispositivos = rutaActual == RUTA_DISPOSITIVOS
    val enAjustes = rutaActual == RUTA_AJUSTES
    val enPantallaSecundaria = enDispositivos || enAjustes
    val esCompacto = windowWidthSizeClass == WindowWidthSizeClass.Compact

    val destinoSeleccionado = when {
        enDispositivos -> DestinoPrincipal.INICIO.ruta
        enAjustes -> DestinoPrincipal.PERFIL.ruta
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
            if (!esCompacto) {
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
                if (esCompacto) {
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
                    val factory = remember {
                        InicioViewModelFactory(
                            InicioRepositoryMemoria()
                        )
                    }

                    val inicioViewModel: InicioViewModel = viewModel(
                        factory = factory
                    )

                    val uiState by inicioViewModel.uiState.collectAsState()

                    InicioScreen(
                        uiState = uiState, onVerDispositivos = {
                            navController.navigate(GRAFO_DISPOSITIVOS) {
                                launchSingleTop = true
                            }
                        })
                }

                composable(DestinoPrincipal.ACCIONES.ruta) {
                    AccionesScreen()
                }

                composable(DestinoPrincipal.APRENDE.ruta) {
                    AprendeScreen()
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
                        DispositivosScreen()
                    }
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