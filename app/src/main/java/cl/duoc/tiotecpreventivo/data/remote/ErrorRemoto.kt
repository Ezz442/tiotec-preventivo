package cl.duoc.tiotecpreventivo.data.remote

import cl.duoc.tiotecpreventivo.data.remote.model.ErrorApiDto
import com.google.gson.Gson
import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Error al hablar con un microservicio, con un mensaje listo para mostrar al usuario.
 * Los ViewModels solo capturan este tipo y muestran [message]; no conocen Retrofit ni HTTP.
 *
 * @param codigoHttp código de la respuesta (400, 404, 409…) o null si no hubo respuesta.
 * @param sinConexion true si no se pudo llegar al servicio (apagado, sin red, tiempo agotado).
 */
class ErrorRemoto(
    mensaje: String,
    val codigoHttp: Int? = null,
    val sinConexion: Boolean = false
) : Exception(mensaje) {

    val noExiste: Boolean get() = codigoHttp == 404
    val conflicto: Boolean get() = codigoHttp == 409
}

const val MENSAJE_SIN_CONEXION =
    "No hay conexión con el servicio. Revisa que esté encendido y que la app apunte a su dirección."

private val gson = Gson()

/**
 * Ejecuta una llamada a un servicio y traduce sus fallos a [ErrorRemoto]:
 * sin red o tiempo agotado → mensaje de conexión; HTTP 4xx/5xx → mensaje del servicio;
 * JSON inesperado → mensaje genérico. La cancelación de corrutinas se deja pasar.
 */
suspend fun <T> llamarServicio(llamada: suspend () -> T): T =
    try {
        llamada()
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        throw ErrorRemoto(mensajeDeHttp(e), codigoHttp = e.code())
    } catch (e: IOException) {
        throw ErrorRemoto(MENSAJE_SIN_CONEXION, sinConexion = true)
    } catch (e: JsonParseException) {
        throw ErrorRemoto("El servicio respondió algo inesperado. Inténtalo nuevamente.")
    }

private fun mensajeDeHttp(e: HttpException): String {
    val cuerpo = try {
        e.response()?.errorBody()?.string()
    } catch (ignorada: IOException) {
        null
    }
    val error = try {
        if (cuerpo.isNullOrBlank()) null else gson.fromJson(cuerpo, ErrorApiDto::class.java)
    } catch (ignorada: JsonParseException) {
        null
    }

    val detalleCampos = error?.campos?.values?.joinToString(" ")?.takeIf { it.isNotBlank() }
    return detalleCampos
        ?: error?.mensaje?.takeIf { it.isNotBlank() }
        ?: when (e.code()) {
            404 -> "No existe en el servicio."
            409 -> "El servicio no permite esa operación en el estado actual."
            in 500..599 -> "El servicio tuvo un problema. Inténtalo más tarde."
            else -> "El servicio rechazó la solicitud (${e.code()})."
        }
}
