package cl.duoc.tiotecpreventivo.ui.screens

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import cl.duoc.tiotecpreventivo.data.imagenes.AlmacenImagenes
import cl.duoc.tiotecpreventivo.viewmodel.DispositivosViewModel
import androidx.activity.result.PickVisualMediaRequest

/**
 * Mis dispositivos con cámara (TT-09 + TT-18).
 *
 * Usa la Activity Result API: primero `RequestPermission` pide el permiso CAMERA (si
 * falta) y luego `TakePicture` abre la app de cámara del teléfono y le entrega, mediante
 * FileProvider, una URI temporal dentro del almacenamiento privado de la app.
 * Si el permiso se rechaza, se avisa y se puede seguir usando la galería.
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

    val resolver = context.applicationContext.contentResolver

    val elegirImagen = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) {
            viewModel.cancelarGaleria()
        } else {
            viewModel.imagenSeleccionada {
                resolver.openInputStream(uri)
            }
        }
    }

    // true = la cámara guardó la foto; false = la persona canceló o la cámara falló.
    val tomarFoto =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
            viewModel.fotoTomada(exito)
        }

    // Abre la app de cámara; solo se llama con el permiso ya concedido.
    val lanzarCamara: (Long) -> Unit = abrir@{ dispositivoId ->
        val ruta = viewModel.prepararFoto(dispositivoId) ?: return@abrir
        val archivo = almacenImagenes.archivo(ruta)
        if (archivo == null) {
            viewModel.fotoTomada(false)
            return@abrir
        }
        try {
            val uri =
                FileProvider.getUriForFile(context, "${context.packageName}.archivos", archivo)
            tomarFoto.launch(uri)
        } catch (e: ActivityNotFoundException) {
            viewModel.camaraNoDisponible()
        } catch (e: IllegalArgumentException) {
            // La ruta no está configurada en FileProvider: no se bloquea la app.
            viewModel.camaraNoDisponible()
        } catch (e: SecurityException) {
            // El permiso se revocó justo antes de abrir la cámara.
            viewModel.permisoCamaraDenegado()
        }
    }

    // Dispositivo que espera la respuesta del diálogo de permiso (sobrevive a la rotación).
    var esperandoPermisoPara by rememberSaveable { mutableStateOf<Long?>(null) }

    val pedirPermisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        val dispositivoId = esperandoPermisoPara
        esperandoPermisoPara = null
        if (dispositivoId == null) return@rememberLauncherForActivityResult
        if (concedido) lanzarCamara(dispositivoId) else viewModel.permisoCamaraDenegado()
    }

    // 1) ¿hay cámara? 2) ¿hay permiso? Si no, se pide con el diálogo del sistema.
    val abrirCamara: (Long) -> Unit = abrir@{ dispositivoId ->
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            viewModel.camaraNoDisponible()
            return@abrir
        }
        val tienePermiso = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (tienePermiso) {
            lanzarCamara(dispositivoId)
        } else {
            esperandoPermisoPara = dispositivoId
            pedirPermisoCamara.launch(Manifest.permission.CAMERA)
        }
    }

    val abrirGaleria: (Long) -> Unit = { dispositivoId ->
        if (viewModel.prepararGaleria(dispositivoId)) {
            try {
                elegirImagen.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            } catch (e: ActivityNotFoundException) {
                viewModel.cancelarGaleria()
                android.widget.Toast.makeText(
                    context,
                    "No hay un selector de imágenes disponible.",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
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
        onElegirImagen = abrirGaleria,
        onSolicitarQuitarFoto = viewModel::solicitarQuitarFoto,
        onConfirmarQuitarFoto = viewModel::confirmarQuitarFoto,
        onCancelarQuitarFoto = viewModel::cancelarQuitarFoto,
        onDescartarCambios = viewModel::descartarCambios,
        onSeguirEditando = viewModel::seguirEditando
    )
}
