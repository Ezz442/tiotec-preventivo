package cl.duoc.tiotecpreventivo.data.remote

/**
 * Direcciones de los microservicios propios (TT-28) y tiempos de espera (TT-29).
 *
 * - Emulador de Android Studio: `10.0.2.2` es el computador donde corren los servicios.
 * - Teléfono físico: reemplazar `10.0.2.2` por la IP del computador en la misma red Wi-Fi
 *   (por ejemplo `http://192.168.1.50:8081/`) y volver a compilar.
 * - Cada URL debe terminar en `/`, como exige Retrofit.
 */
object ConfigRed {
    // #url del microservicio solicitudes
    const val URL_SOLICITUDES = "http://localhost:8081/"
    // #url del microservicio dispositivos
    const val URL_DISPOSITIVOS = "http://localhost:8082/"

    // #tiempo maximo de espera de la red
    const val TIEMPO_ESPERA_SEGUNDOS = 10L

    // #url de la api externa (JSONPlaceholder)
    /** TT-30: API publica externa (JSONPlaceholder). Es HTTPS, no necesita adb reverse. */
    const val URL_API_EXTERNA = "https://jsonplaceholder.typicode.com/"
}
