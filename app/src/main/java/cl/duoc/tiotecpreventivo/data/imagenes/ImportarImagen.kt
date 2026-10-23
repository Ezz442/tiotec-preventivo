package cl.duoc.tiotecpreventivo.data.imagenes

import java.io.IOException
import java.io.InputStream

fun AlmacenImagenes.copiarImagen(
    ruta: String, abrirOrigen: () -> InputStream?
) {
    val destino = archivo(ruta) ?: throw IOException("La ruta de destino no es válida.")

    try {
        val origen = abrirOrigen() ?: throw IOException("No se pudo abrir la imagen.")

        origen.use { entrada ->
            destino.outputStream().use { salida ->
                val bytesCopiados = entrada.copyTo(salida)

                if (bytesCopiados == 0L) {
                    throw IOException("La imagen está vacía.")
                }
            }
        }
    } catch (e: Exception) {
        destino.delete()
        throw e
    }
}