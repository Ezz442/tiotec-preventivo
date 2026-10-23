package cl.duoc.tiotecpreventivo

import android.app.Application
import cl.duoc.tiotecpreventivo.data.AppContainer
import cl.duoc.tiotecpreventivo.notificaciones.Avisos

/** Punto único donde se crea el contenedor de datos de la app (TT-07). */
class TioTecApplication : Application() {

    lateinit var contenedor: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = AppContainer(this)
        Avisos.crearCanal(this)
    }
}
