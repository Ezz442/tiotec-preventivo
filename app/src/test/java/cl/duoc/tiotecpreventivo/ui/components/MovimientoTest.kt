package cl.duoc.tiotecpreventivo.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** TT-25: sentido de las transiciones y relleno de las barras animadas. */
class MovimientoTest {

    @Test
    fun avanzarDePreguntaEsHaciaAdelante() {
        assertEquals(1, Movimiento.direccion(desde = 3, hacia = 4))
        // Saltar varias preguntas desde la revisión también desliza hacia adelante.
        assertEquals(1, Movimiento.direccion(desde = 0, hacia = 11))
    }

    @Test
    fun volverDePreguntaEsHaciaAtras() {
        assertEquals(-1, Movimiento.direccion(desde = 4, hacia = 3))
        assertEquals(-1, Movimiento.direccion(desde = 11, hacia = 0))
    }

    @Test
    fun sinCambioNoHayDesplazamiento() {
        assertEquals(0, Movimiento.direccion(desde = 5, hacia = 5))
    }

    @Test
    fun elContadorSubeOBajaSegunLasPendientes() {
        assertEquals(-1, Movimiento.direccion(desde = 4, hacia = 3)) // se completó una acción
        assertEquals(1, Movimiento.direccion(desde = 3, hacia = 7)) // nueva evaluación con acciones
    }

    @Test
    fun fraccionDelProgresoDeLaEvaluacion() {
        assertEquals(0f, Movimiento.fraccion(0, 12), 0.0001f)
        assertEquals(0.5f, Movimiento.fraccion(6, 12), 0.0001f)
        assertEquals(1f, Movimiento.fraccion(12, 12), 0.0001f)
    }

    @Test
    fun fraccionDelPuntajeDeUnaCategoria() {
        // Ejemplo: 5 de 9 puntos de riesgo posibles.
        assertEquals(5f / 9f, Movimiento.fraccion(5, 9), 0.0001f)
    }

    @Test
    fun fraccionSeQuedaEntreCeroYUno() {
        assertEquals(1f, Movimiento.fraccion(15, 12), 0.0001f)
        assertEquals(0f, Movimiento.fraccion(-2, 12), 0.0001f)
    }

    @Test
    fun maximoCeroDaUnaBarraVaciaSinDividirPorCero() {
        val fraccion = Movimiento.fraccion(3, 0)
        assertEquals(0f, fraccion, 0.0001f)
        assertTrue(!fraccion.isNaN())
        assertEquals(0f, Movimiento.fraccion(3, -1), 0.0001f)
    }

    @Test
    fun duracionesOrdenadasDeCortaALarga() {
        assertTrue(Movimiento.DURACION_CORTA < Movimiento.DURACION_MEDIA)
        assertTrue(Movimiento.DURACION_MEDIA < Movimiento.DURACION_LARGA)
        // Ninguna animación hace esperar más de un segundo.
        assertTrue(Movimiento.DURACION_LARGA <= 1000)
    }
}
