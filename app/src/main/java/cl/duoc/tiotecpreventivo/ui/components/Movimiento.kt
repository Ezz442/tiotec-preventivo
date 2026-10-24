package cl.duoc.tiotecpreventivo.ui.components

import kotlin.math.sign

/**
 * Duraciones y cálculos de las animaciones (TT-25). Sin Compose, para probarlos en la JVM.
 *
 * Las duraciones siguen los rangos de movimiento de Material 3: cortas para
 * colores y mensajes, medias para cambiar de pregunta y largas solo para barras
 * de puntaje que se leen una vez. Si la persona desactiva las animaciones en
 * Opciones de desarrollador o Accesibilidad, Compose las omite y el contenido
 * aparece de inmediato.
 */
object Movimiento {

    /** Color de una opción, aparición de un error o de un indicador de carga. */
    const val DURACION_CORTA = 150

    /** Cambio de pregunta, contador y tarjetas que entran o salen de una lista. */
    const val DURACION_MEDIA = 300

    /** Barra de puntaje de cada categoría al abrir los resultados. */
    const val DURACION_LARGA = 600

    /**
     * Sentido del cambio entre dos posiciones: 1 hacia adelante, -1 hacia atrás, 0 sin cambio.
     * Avanzar de pregunta desliza la nueva desde la derecha; volver, desde la izquierda.
     */
    fun direccion(desde: Int, hacia: Int): Int = (hacia - desde).sign

    /** Parte llena de una barra entre 0 y 1. Un máximo de 0 o negativo da una barra vacía. */
    fun fraccion(valor: Int, maximo: Int): Float =
        if (maximo <= 0) 0f else (valor.toFloat() / maximo).coerceIn(0f, 1f)
}
