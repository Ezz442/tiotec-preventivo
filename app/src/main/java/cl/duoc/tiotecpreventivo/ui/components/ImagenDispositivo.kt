package cl.duoc.tiotecpreventivo.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cl.duoc.tiotecpreventivo.ui.theme.TioTecPreventivoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private sealed interface EstadoImagen {
    data object Cargando : EstadoImagen
    data class Lista(val bitmap: Bitmap) : EstadoImagen
    data object NoDisponible : EstadoImagen
}

/**
 * Foto de un dispositivo ficticio (TT-18). Componente compartido con la galería (TT-19).
 *
 * Lee el archivo en segundo plano y reducido al tamaño de pantalla, para no trabar la
 * lista ni gastar memoria con fotos de varios megapíxeles. Si el archivo ya no existe
 * (por ejemplo, se borraron los datos de la app), muestra "Imagen no disponible" en vez
 * de fallar.
 *
 * @param archivo archivo de la foto; null si el dispositivo no tiene.
 */
@Composable
fun ImagenDispositivo(
    archivo: File?,
    descripcion: String,
    modifier: Modifier = Modifier,
    alto: Dp = 160.dp
) {
    val forma = RoundedCornerShape(12.dp)
    val contenedor = modifier
        .fillMaxWidth()
        .height(alto)
        .clip(forma)
        .background(MaterialTheme.colorScheme.surfaceVariant)

    if (archivo == null) {
        Box(contenedor, contentAlignment = Alignment.Center) {
            Text("Sin foto", style = MaterialTheme.typography.bodySmall)
        }
        return
    }

    val estado by produceState<EstadoImagen>(EstadoImagen.Cargando, archivo.path, archivo.lastModified()) {
        value = withContext(Dispatchers.IO) {
            decodificarReducida(archivo, ladoMaximo = 1024)
                ?.let { EstadoImagen.Lista(it) }
                ?: EstadoImagen.NoDisponible
        }
    }

    Box(contenedor, contentAlignment = Alignment.Center) {
        when (val actual = estado) {
            EstadoImagen.Cargando -> CircularProgressIndicator()
            is EstadoImagen.Lista -> Image(
                bitmap = actual.bitmap.asImageBitmap(),
                contentDescription = descripcion,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            EstadoImagen.NoDisponible -> Text(
                text = "Imagen no disponible",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

/** Lee solo las dimensiones y luego carga una versión reducida (potencia de 2). */
private fun decodificarReducida(archivo: File, ladoMaximo: Int): Bitmap? {
    if (!archivo.isFile || archivo.length() == 0L) return null
    return try {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(archivo.path, limites)
        if (limites.outWidth <= 0 || limites.outHeight <= 0) return null

        var muestra = 1
        while (limites.outWidth / (muestra * 2) >= ladoMaximo || limites.outHeight / (muestra * 2) >= ladoMaximo) {
            muestra *= 2
        }
        BitmapFactory.decodeFile(archivo.path, BitmapFactory.Options().apply { inSampleSize = muestra })
    } catch (e: Exception) {
        null
    } catch (e: OutOfMemoryError) {
        null
    }
}

@Preview(showBackground = true, name = "Sin foto")
@Composable
fun ImagenDispositivoSinFotoPreview() {
    TioTecPreventivoTheme {
        ImagenDispositivo(archivo = null, descripcion = "Foto del dispositivo", modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true, name = "Imagen no disponible")
@Composable
fun ImagenDispositivoNoDisponiblePreview() {
    TioTecPreventivoTheme {
        ImagenDispositivo(
            archivo = File("no-existe.jpg"),
            descripcion = "Foto del dispositivo",
            modifier = Modifier.padding(16.dp)
        )
    }
}
