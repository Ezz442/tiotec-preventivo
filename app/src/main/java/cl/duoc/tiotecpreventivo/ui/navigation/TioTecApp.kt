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

private const val RUTA_DISPOSITIVOS = "dispositivos"
private const val GRAFO_DISPOSITIVOS = "gestion_dispositivos"

private enum class DestinoPrincipal(
    val ruta: String,
    val titulo: String,
    val simbolo: String
) {
    INICIO("inicio", "Inicio", "⌂"),
    ACCIONES("acciones", "Mis acciones", "✓"),
    APRENDE("aprende", "Aprende", "?"),
    PERFIL("perfil", "Perfil", "○")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TioTecApp(
    windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact
) {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    val enDispositivos = rutaActual == RUTA_DISPOSITIVOS
    val esCompacto = windowWidthSizeClass == WindowWidthSizeClass.Compact

    val destinoSeleccionado = if (enDispositivos) {
        DestinoPrincipal.INICIO.ruta
    } else {
        rutaActual
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
        modifier = Modifier.fillMaxSize(),
        drawerContent = {
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
                            },
                            icon = {
                                Text(destino.simbolo)
                            },
                            selected = destinoSeleccionado == destino.ruta,
                            onClick = {
                                navegarPrincipal(destino)
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (enDispositivos) {
                                "Mis dispositivos"
                            } else {
                                "TíoTec Preventivo"
                            }
                        )
                    },
                    navigationIcon = {
                        if (enDispositivos) {
                            TextButton(
                                onClick = {
                                    navController.popBackStack()
                                }
                            ) {
                                Text("← Volver")
                            }
                        }
                    }
                )
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
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = DestinoPrincipal.INICIO.ruta,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                composable(DestinoPrincipal.INICIO.ruta) {
                    InicioScreen(
                        onVerDispositivos = {
                            navController.navigate(GRAFO_DISPOSITIVOS) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(DestinoPrincipal.ACCIONES.ruta) {
                    AccionesScreen()
                }

                composable(DestinoPrincipal.APRENDE.ruta) {
                    AprendeScreen()
                }

                composable(DestinoPrincipal.PERFIL.ruta) {
                    PerfilScreen()
                }

                navigation(
                    startDestination = RUTA_DISPOSITIVOS,
                    route = GRAFO_DISPOSITIVOS
                ) {
                    composable(RUTA_DISPOSITIVOS) {
                        DispositivosScreen()
                    }
                }
            }
        }
    }
}