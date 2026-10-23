package cl.duoc.tiotecpreventivo.data.imagenes

import java.io.File

/**
 * Imágenes de dispositivos ficticios (TT-18 cámara, TT-19 galería).
 *
 * Las fotos viven en el almacenamiento privado de la app (`filesDir/imagenes`): no se
 * necesitan permisos de almacenamiento, otras apps no las ven y se borran al
 * desinstalar. En Room solo se guarda la ruta relativa, por ejemplo
 * `imagenes/disp_3_1729650000000.jpg`.
 */
interface AlmacenImagenes {

    /** Crea una ruta nueva y vacía para una foto del dispositivo. */
    fun nuevaRuta(dispositivoId: Long): String

    /** Archivo real de una ruta guardada; null si la ruta no es válida. */
    fun archivo(ruta: String): File?

    /** true si la ruta apunta a una imagen que existe y no está vacía. */
    fun existe(ruta: String): Boolean

    /** Borra el archivo si existe. No falla si ya no estaba. */
    fun borrar(ruta: String?)
}

/** Implementación con `java.io.File`, sin Android: se puede probar en la JVM. */
class AlmacenImagenesLocal(private val base: File) : AlmacenImagenes {

    private val carpeta: File get() = File(base, CARPETA)

    override fun nuevaRuta(dispositivoId: Long): String {
        carpeta.mkdirs()
        var marca = System.currentTimeMillis()
        var ruta: String
        do {
            ruta = "$CARPETA/disp_${dispositivoId}_${marca++}.jpg"
        } while (File(base, ruta).exists())
        return ruta
    }

    override fun archivo(ruta: String): File? {
        val archivo = File(base, ruta).canonicalFile
        // Evita rutas como "../../otra_carpeta": solo se aceptan archivos dentro de imagenes/.
        return archivo.takeIf { it.parentFile == carpeta.canonicalFile }
    }

    override fun existe(ruta: String): Boolean =
        archivo(ruta)?.let { it.isFile && it.length() > 0 } ?: false

    override fun borrar(ruta: String?) {
        if (ruta == null) return
        archivo(ruta)?.delete()
    }

    companion object {
        const val CARPETA = "imagenes"
    }
}
