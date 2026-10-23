package cl.duoc.tiotecpreventivo.data

import android.content.Context
import cl.duoc.tiotecpreventivo.data.catalogo.CatalogoRepositoryLocal
import cl.duoc.tiotecpreventivo.data.local.TioTecDatabase
import cl.duoc.tiotecpreventivo.data.preferences.preferenciasDataStore
import cl.duoc.tiotecpreventivo.repository.AccionRepository
import cl.duoc.tiotecpreventivo.repository.CatalogoRepository
import cl.duoc.tiotecpreventivo.repository.DispositivoRepository
import cl.duoc.tiotecpreventivo.repository.EvaluacionRepository
import cl.duoc.tiotecpreventivo.data.imagenes.AlmacenImagenes
import cl.duoc.tiotecpreventivo.data.imagenes.AlmacenImagenesLocal
import cl.duoc.tiotecpreventivo.repository.GestorRecordatorios
import cl.duoc.tiotecpreventivo.notificaciones.ProgramadorWorkManager
import cl.duoc.tiotecpreventivo.repository.PerfilRepository
import cl.duoc.tiotecpreventivo.repository.PreferenciasRepository
import cl.duoc.tiotecpreventivo.repository.RecordatorioRepository
import cl.duoc.tiotecpreventivo.repository.SolicitudRepository

/**
 * Crea una sola vez la base de datos y los repositorios. Los ViewModels los reciben
 * mediante su Factory; ningún Composable accede a Room directamente.
 *
 * Uso: `(context.applicationContext as TioTecApplication).contenedor.dispositivoRepository`
 */
class AppContainer(context: Context) {

    private val baseDeDatos: TioTecDatabase by lazy { TioTecDatabase.obtener(context) }

    val catalogoRepository: CatalogoRepository by lazy { CatalogoRepositoryLocal() }

    val perfilRepository: PerfilRepository by lazy {
        PerfilRepository(baseDeDatos.perfilDao())
    }

    val dispositivoRepository: DispositivoRepository by lazy {
        DispositivoRepository(baseDeDatos.dispositivoDao())
    }

    val evaluacionRepository: EvaluacionRepository by lazy {
        EvaluacionRepository(
            baseDeDatos.evaluacionDao(),
            baseDeDatos.dispositivoDao(),
            catalogoRepository
        )
    }

    val accionRepository: AccionRepository by lazy {
        AccionRepository(baseDeDatos.accionDao())
    }

    val recordatorioRepository: RecordatorioRepository by lazy {
        RecordatorioRepository(baseDeDatos.recordatorioDao())
    }

    /** TT-16: guarda el recordatorio en Room y programa su aviso con WorkManager. */
    val gestorRecordatorios: GestorRecordatorios by lazy {
        GestorRecordatorios(recordatorioRepository, ProgramadorWorkManager(context))
    }

    /** TT-18/19: fotos de dispositivos en el almacenamiento privado de la app. */
    val almacenImagenes: AlmacenImagenes by lazy { AlmacenImagenesLocal(context.filesDir) }

    val solicitudRepository: SolicitudRepository by lazy {
        SolicitudRepository(baseDeDatos.solicitudDao())
    }

    /** Preferencias simples en DataStore (TT-08): tema, primera ejecución y avisos. */
    val preferenciasRepository: PreferenciasRepository by lazy {
        PreferenciasRepository(context.preferenciasDataStore)
    }
}
