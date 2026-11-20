package cl.duoc.tiotecpreventivo.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Crea los clientes Retrofit una sola vez (los usa AppContainer). Todos comparten el mismo
 * OkHttpClient y los mismos tiempos de espera; cada servicio tiene su propia URL base.
 *
 * La API externa de TT-30 puede reutilizar [crearApi] con su propia URL.
 */
class ServiciosRemotos(
    private val urlSolicitudes: String = ConfigRed.URL_SOLICITUDES,
    private val urlDispositivos: String = ConfigRed.URL_DISPOSITIVOS
) {

    private val cliente: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(ConfigRed.TIEMPO_ESPERA_SEGUNDOS, TimeUnit.SECONDS)
            .readTimeout(ConfigRed.TIEMPO_ESPERA_SEGUNDOS, TimeUnit.SECONDS)
            .writeTimeout(ConfigRed.TIEMPO_ESPERA_SEGUNDOS, TimeUnit.SECONDS)
            .build()
    }

    fun <T> crearApi(urlBase: String, clase: Class<T>): T =
        Retrofit.Builder()
            .baseUrl(urlBase)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(clase)

    val solicitudes: SolicitudesApi by lazy { crearApi(urlSolicitudes, SolicitudesApi::class.java) }

    val dispositivos: DispositivosApi by lazy { crearApi(urlDispositivos, DispositivosApi::class.java) }
}
