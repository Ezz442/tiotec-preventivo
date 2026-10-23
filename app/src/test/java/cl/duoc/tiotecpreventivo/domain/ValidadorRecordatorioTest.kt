package cl.duoc.tiotecpreventivo.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/** TT-16: fecha y hora del recordatorio, con la zona horaria de Chile fija para que sea reproducible. */
class ValidadorRecordatorioTest {

    private val chile = TimeZone.getTimeZone("America/Santiago")

    /** Medianoche UTC del día, como la entrega el DatePicker. */
    private fun dia(anio: Int, mes: Int, dia: Int): Long =
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { clear(); set(anio, mes - 1, dia) }.timeInMillis

    private fun local(anio: Int, mes: Int, dia: Int, hora: Int, minuto: Int): Long =
        Calendar.getInstance(chile).apply { clear(); set(anio, mes - 1, dia, hora, minuto) }.timeInMillis

    private val ahora = local(2026, 10, 22, 23, 30)

    @Test
    fun combinaFechaDelSelectorConHoraLocal() {
        assertEquals(local(2026, 10, 24, 10, 15), ValidadorRecordatorio.combinar(dia(2026, 10, 24), 10, 15, chile))
    }

    @Test
    fun separarEsLoInversoDeCombinar() {
        val guardado = local(2026, 12, 31, 23, 59)
        val (fecha, hora, minuto) = ValidadorRecordatorio.separar(guardado, chile)

        assertEquals(dia(2026, 12, 31), fecha)
        assertEquals(23, hora)
        assertEquals(59, minuto)
        assertEquals(guardado, ValidadorRecordatorio.combinar(fecha, hora, minuto, chile))
    }

    @Test
    fun fechaYHoraFuturasSonValidas() {
        val resultado = ValidadorRecordatorio.validar(dia(2026, 10, 24), 10, 0, ahora, chile)

        assertTrue(resultado.esValido)
        assertEquals(local(2026, 10, 24, 10, 0), resultado.fechaHora)
    }

    @Test
    fun sinFechaNiHoraMuestraAmbosErrores() {
        val resultado = ValidadorRecordatorio.validar(null, null, null, ahora, chile)

        assertFalse(resultado.esValido)
        assertEquals("Elige una fecha.", resultado.errorFecha)
        assertEquals("Elige una hora.", resultado.errorHora)
    }

    @Test
    fun soloFaltaLaHora() {
        val resultado = ValidadorRecordatorio.validar(dia(2026, 10, 24), null, null, ahora, chile)

        assertNull(resultado.errorFecha)
        assertEquals("Elige una hora.", resultado.errorHora)
    }

    @Test
    fun horaPasadaDeHoyNoEsValida() {
        val resultado = ValidadorRecordatorio.validar(dia(2026, 10, 22), 23, 0, ahora, chile)

        assertFalse(resultado.esValido)
        assertEquals("Elige una fecha y hora en el futuro.", resultado.errorHora)
    }

    @Test
    fun menosDeUnMinutoEnElFuturoNoEsValido() {
        val resultado = ValidadorRecordatorio.validar(dia(2026, 10, 22), 23, 30, ahora, chile)
        assertFalse(resultado.esValido)

        assertTrue(ValidadorRecordatorio.validar(dia(2026, 10, 22), 23, 31, ahora, chile).esValido)
    }

    @Test
    fun masDeUnAnioNoEsValido() {
        val resultado = ValidadorRecordatorio.validar(dia(2027, 10, 25), 9, 0, ahora, chile)

        assertFalse(resultado.esValido)
        assertEquals("Elige una fecha dentro del próximo año.", resultado.errorFecha)
    }

    @Test
    fun horaFueraDeRangoNoEsValida() {
        assertFalse(ValidadorRecordatorio.validar(dia(2026, 10, 24), 24, 0, ahora, chile).esValido)
        assertFalse(ValidadorRecordatorio.validar(dia(2026, 10, 24), 10, 60, ahora, chile).esValido)
    }
}
