package cl.duoc.tiotecpreventivo.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.notificaciones.Avisos
import cl.duoc.tiotecpreventivo.ui.components.RecordatorioDialogo
import cl.duoc.tiotecpreventivo.viewmodel.AccionesViewModel
import cl.duoc.tiotecpreventivo.viewmodel.RecordatorioViewModel

/**
 * Mis acciones con recordatorios (TT-13 + TT-16). Une los dos ViewModels, pide el
 * permiso de notificaciones cuando hace falta y muestra el resultado en un Snackbar.
 */
@Composable
fun AccionesRuta(
    accionesViewModel: AccionesViewModel,
    recordatorioViewModel: RecordatorioViewModel
) {
    val context = LocalContext.current
    val acciones by accionesViewModel.uiState.collectAsState()
    val recordatorios by recordatorioViewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var puedeNotificar by remember { mutableStateOf(Avisos.puedeNotificar(context)) }

    // Android 13+: se pide el permiso al guardar el primer recordatorio. Si se niega,
    // el recordatorio se guarda igual y el mensaje explica cómo activarlo.
    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        puedeNotificar = Avisos.puedeNotificar(context)
        recordatorioViewModel.guardar(puedeNotificar)
    }

    LaunchedEffect(recordatorios.mensaje) {
        recordatorios.mensaje?.let {
            snackbar.showSnackbar(it)
            recordatorioViewModel.consumirMensaje()
        }
    }

    Box(Modifier.fillMaxSize()) {
        AccionesScreen(
            uiState = acciones,
            onFiltroChange = accionesViewModel::cambiarFiltro,
            onCambiarEstado = accionesViewModel::cambiarEstado,
            onReintentar = accionesViewModel::reintentarCarga,
            recordatorios = recordatorios.porAccion,
            avisosBloqueados = !puedeNotificar,
            onRecordar = { accion -> recordatorioViewModel.abrirNuevo(accion.id, accion.titulo) }
        )

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }

    recordatorios.formulario?.let { formulario ->
        RecordatorioDialogo(
            formulario = formulario,
            guardando = recordatorios.guardando,
            errorOperacion = recordatorios.errorOperacion,
            onFechaChange = recordatorioViewModel::onFechaChange,
            onHoraChange = recordatorioViewModel::onHoraChange,
            onGuardar = {
                puedeNotificar = Avisos.puedeNotificar(context)
                if (!puedeNotificar && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    recordatorioViewModel.guardar(puedeNotificar)
                }
            },
            onCancelarRecordatorio = recordatorioViewModel::cancelarRecordatorio,
            onCerrar = recordatorioViewModel::cerrarFormulario
        )
    }
}
