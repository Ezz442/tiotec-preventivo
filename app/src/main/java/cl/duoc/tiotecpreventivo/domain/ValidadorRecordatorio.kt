package cl.duoc.tiotecpreventivo.domain

import java.util.Calendar
import java.util.TimeZone

/** Resultado de validar el formulario de recordatorio (TT-16). */
data class ValidacionRecordatorio(
    val errorFecha: String? = null,
    val errorHora: String? = null,
    /** Fecha y hora local en milisegundos; null si hay errores. */
    val fechaHora: Long? = null
) {
    val esValido: Boolean get() = fechaHora != null
}

/**
 * Reglas del formulario de recordatorio, sin Android (TT-16).
 *
 * - Fecha y hora obligatorias.
 * - Al menos [MINUTOS_MINIMOS] minuto en el futuro.
 * - Como máximo [DIAS_MAXIMOS] días hacia adelante.
 *
 * Usa `java.util.Calendar` (y no `java.time`) porque la app soporta Android 7 (minSdk 24).
 */
object ValidadorRecordatorio {

    const val MINUTOS_MINIMOS = 1
    const val DIAS_MAXIMOS = 365

    private const val MINUTO = 60_000L
    private const val DIA = 24 * 60 * MINUTO

    /**
     * Junta la fecha del DatePicker (medianoche UTC del día elegido) con la hora del
     * TimePicker y devuelve ese momento en la zona horaria del teléfono.
     */
    fun combinar(
        fechaUtcMillis: Long,
        hora: Int,
        minuto: Int,
        zona: TimeZone = TimeZone.getDefault()
    ): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = fechaUtcMillis }
        return Calendar.getInstance(zona).apply {
            clear()
            set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), hora, minuto, 0)
        }.timeInMillis
    }

    /** Lo inverso de [combinar]: fecha (medianoche UTC) y hora/minuto locales de un momento guardado. */
    fun separar(fechaHora: Long, zona: TimeZone = TimeZone.getDefault()): Triple<Long, Int, Int> {
        val local = Calendar.getInstance(zona).apply { timeInMillis = fechaHora }
        val fechaUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
        return Triple(fechaUtc, local.get(Calendar.HOUR_OF_DAY), local.get(Calendar.MINUTE))
    }

    fun validar(
        fechaUtcMillis: Long?,
        hora: Int?,
        minuto: Int?,
        ahora: Long,
        zona: TimeZone = TimeZone.getDefault()
    ): ValidacionRecordatorio {
        val errorFechaVacia = if (fechaUtcMillis == null) "Elige una fecha." else null
        val errorHoraVacia = if (hora == null || minuto == null) "Elige una hora." else null
        if (errorFechaVacia != null || errorHoraVacia != null) {
            return ValidacionRecordatorio(errorFecha = errorFechaVacia, errorHora = errorHoraVacia)
        }
        if (hora!! !in 0..23 || minuto!! !in 0..59) {
            return ValidacionRecordatorio(errorHora = "La hora no es válida.")
        }

        val fechaHora = combinar(fechaUtcMillis!!, hora, minuto, zona)
        return when {
            fechaHora < ahora + MINUTOS_MINIMOS * MINUTO ->
                ValidacionRecordatorio(errorHora = "Elige una fecha y hora en el futuro.")
            fechaHora > ahora + DIAS_MAXIMOS * DIA ->
                ValidacionRecordatorio(errorFecha = "Elige una fecha dentro del próximo año.")
            else -> ValidacionRecordatorio(fechaHora = fechaHora)
        }
    }
}
