package cl.duoc.tiotecpreventivo.data.preferences

/** Tema elegido por el usuario (TT-04, sección 7). */
enum class Tema(val etiqueta: String) {
    SISTEMA("Igual que el teléfono"),
    CLARO("Claro"),
    OSCURO("Oscuro");

    /** Decide si se usa el modo oscuro según la preferencia y el modo del teléfono. */
    fun usarModoOscuro(sistemaEnModoOscuro: Boolean): Boolean = when (this) {
        SISTEMA -> sistemaEnModoOscuro
        CLARO -> false
        OSCURO -> true
    }
}

/**
 * Preferencias simples guardadas con DataStore. Los registros estructurados
 * (perfil, dispositivos, evaluaciones…) van en Room, no aquí.
 */
data class Preferencias(
    val tema: Tema = Tema.SISTEMA,
    /** true hasta que se muestra la bienvenida y el aviso de datos ficticios. */
    val primeraEjecucion: Boolean = true,
    /** Preferencia del usuario. El permiso de notificaciones del sistema se revisa aparte (TT-16). */
    val avisosActivos: Boolean = true
)
