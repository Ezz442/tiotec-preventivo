package cl.duoc.tiotecpreventivo.data.remote

import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

const val MENSAJE_API_EXTERNA_SIN_CONEXION =
    "No pudimos conectar con el servicio externo. Revisa tu conexión a internet."

// #llamada a la api externa con manejo de errores
/**
 * TT-30: igual que [llamarServicio] pero para una API de terceros. Reutiliza [ErrorRemoto],
 * con mensajes distintos: aquí el fallo no es de un servicio de TíoTec que haya que encender,
 * y el cuerpo del error de un tercero no tiene el formato de nuestros microservicios.
 */
suspend fun <T> llamarApiExterna(llamada: suspend () -> T): T =
    try {
        llamada()
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        val mensaje = when (e.code()) {
            404 -> "El servicio externo no encontró lo pedido."
            429 -> "El servicio externo recibió demasiadas consultas. Intenta en un momento."
            in 500..599 -> "El servicio externo tuvo un problema. Inténtalo más tarde."
            else -> "El servicio externo rechazó la consulta (${e.code()})."
        }
        throw ErrorRemoto(mensaje, codigoHttp = e.code())
    } catch (e: IOException) {
        throw ErrorRemoto(MENSAJE_API_EXTERNA_SIN_CONEXION, sinConexion = true)
    } catch (e: JsonParseException) {
        throw ErrorRemoto("El servicio externo respondió algo inesperado. Inténtalo nuevamente.")
    }
