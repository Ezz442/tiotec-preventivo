package cl.duoc.tiotecpreventivo.notificaciones

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cl.duoc.tiotecpreventivo.MainActivity
import cl.duoc.tiotecpreventivo.R

/**
 * Notificaciones locales de recordatorios (TT-16). Sin red: el aviso lo genera el
 * propio teléfono a la hora programada.
 */
object Avisos {

    const val CANAL_RECORDATORIOS = "recordatorios"

    /** Extra del Intent que indica a qué pantalla abrir la app al tocar el aviso. */
    const val EXTRA_DESTINO = "cl.duoc.tiotecpreventivo.DESTINO"
    const val DESTINO_ACCIONES = "acciones"

    /** Se llama al iniciar la app. Desde Android 8 las notificaciones necesitan un canal. */
    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val canal = NotificationChannel(
            CANAL_RECORDATORIOS,
            "Recordatorios de acciones",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos para revisar tus acciones preventivas."
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    /**
     * true si la app puede mostrar avisos. En Android 13+ depende del permiso
     * POST_NOTIFICATIONS; antes, de que el usuario no las haya desactivado en Ajustes.
     */
    fun puedeNotificar(context: Context): Boolean {
        val permiso = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        return permiso && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun mostrarRecordatorio(context: Context, recordatorioId: Long, titulo: String) {
        if (!puedeNotificar(context)) return

        val abrirAcciones = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_DESTINO, DESTINO_ACCIONES)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            recordatorioId.toInt(),
            abrirAcciones,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val aviso = NotificationCompat.Builder(context, CANAL_RECORDATORIOS)
            .setSmallIcon(R.drawable.ic_riesgo_medio)
            .setContentTitle("Recordatorio de TíoTec")
            .setContentText(titulo)
            .setStyle(NotificationCompat.BigTextStyle().bigText(titulo))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(recordatorioId.toInt(), aviso)
        } catch (e: SecurityException) {
            // El permiso se quitó justo ahora: no se muestra el aviso, pero la app sigue funcionando.
        }
    }
}
