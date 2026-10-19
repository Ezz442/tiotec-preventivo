package cl.duoc.tiotecpreventivo.data.local.entity

/**
 * Enums de los registros guardados en Room (TT-04, sección 4).
 * Room los guarda como texto con su nombre, por eso no se deben renombrar
 * sin una migración.
 */

enum class ExperienciaDigital(val etiqueta: String) {
    PRINCIPIANTE("Principiante"),
    INTERMEDIO("Intermedio"),
    AVANZADO("Avanzado")
}

enum class SistemaOperativo(val etiqueta: String) {
    ANDROID("Android"),
    IOS("iOS"),
    WINDOWS("Windows"),
    MACOS("macOS"),
    OTRO("Otro")
}

/** Ordenados de menor a mayor riesgo: el nivel global es el de mayor `ordinal`. */
enum class NivelRiesgo(val etiqueta: String) {
    RIESGO_BAJO("Riesgo bajo"),
    RIESGO_MEDIO("Riesgo medio"),
    RIESGO_ALTO("Riesgo alto")
}

enum class EstadoAccion { PENDIENTE, COMPLETADA }

enum class TipoRecordatorio { ACCION, PROXIMA_EVALUACION }

enum class EstadoRecordatorio { PROGRAMADO, CANCELADO, ENVIADO }

enum class EstadoSolicitud(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    EN_REVISION("En revisión"),
    RESUELTA("Resuelta"),
    CANCELADA("Cancelada")
}
