package cl.duoc.tiotecpreventivo.ui.screens

import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import cl.duoc.tiotecpreventivo.data.imagenes.AlmacenImagenes
import cl.duoc.tiotecpreventivo.viewmodel.DispositivosViewModel

/**
 * Mis dispositivos con cámara (TT-09 + TT-18).
 *
 * Usa la Activity Result API (`TakePicture`): abre la app de cámara del teléfono y le
 * entrega, mediante FileProvider, una URI temporal dentro del almacenamiento privado
 * de la app. No se pide el permiso CAMERA porque la foto la toma la app de cámara.
 */
@Composable
fun DispositivosRuta(
    viewModel: DispositivosViewModel,
    almacenImagenes: AlmacenImagenes,
    onEvaluar: (Long) -> Unit,
    onIrAPerfil: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // true = la cámara guardó la foto; false = la persona canceló o la cámara falló.
    val tomarFoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        viewModel.fotoTomada(exito)
    }

    val abrirCamara: (Long) -> Unit = abrir@{ dispositivoId ->
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            viewModel.camaraNoDisponible()
            return@abrir
        }
        val ruta = viewModel.prepararFoto(dispositivoId) ?: return@abrir
        val archivo = almacenImagenes.archivo(ruta)
        if (archivo == null) {
            viewModel.fotoTomada(false)
            return@abrir
        }
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.archivos", archivo)
            tomarFoto.launch(uri)
        } catch (e: ActivityNotFoundException) {
            viewModel.camaraNoDisponible()
        } catch (e: IllegalArgumentException) {
            // La ruta no está configurada en FileProvider: no se bloquea la app.
            viewModel.camaraNoDisponible()
        }
    }

    DispositivosScreen(
        uiState = uiState,
        onAgregar = viewModel::abrirNuevo,
        onEditar = viewModel::editarDispositivo,
        onEvaluar = onEvaluar,
        onSolicitarEliminar = viewModel::solicitarEliminacion,
        onConfirmarEliminar = viewModel::confirmarEliminacion,
        onCancelarEliminar = viewModel::cancelarEliminacion,
        onNombreChange = viewModel::onNombreChange,
        onSistemaChange = viewModel::onSistemaOperativoChange,
        onGuardar = viewModel::guardarDispositivo,
        onCancelarFormulario = viewModel::cancelarFormulario,
        onReintentar = viewModel::reintentarCarga,
        onIrAPerfil = onIrAPerfil,
        onConsumirMensaje = viewModel::consumirMensaje,
        archivoImagen = almacenImagenes::archivo,
        onTomarFoto = abrirCamara,
        onSolicitarQuitarFoto = viewModel::solicitarQuitarFoto,
        onConfirmarQuitarFoto = viewModel::confirmarQuitarFoto,
        onCancelarQuitarFoto = viewModel::cancelarQuitarFoto
    )
}
