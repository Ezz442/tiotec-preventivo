package cl.duoc.tiotecpreventivo.data.catalogo

/**
 * Modelos del catálogo fijo (TT-04, sección 3).
 * Son datos de prueba que el usuario no edita: viven en el código y no en Room,
 * por lo que no se duplican ni se borran al reabrir la app.
 */

enum class Categoria(val nombre: String) {
    RESPALDOS("Respaldos"),
    ACTUALIZACIONES("Actualizaciones"),
    AUTENTICACION("Autenticación"),
    RECUPERACION("Recuperación de cuentas")
}

enum class ValorRespuesta(val etiqueta: String) {
    SI("Sí"),
    NO("No"),
    NO_SE("No lo sé")
}

/** Orden de importancia: ALTA → MEDIA → BAJA (se usa `ordinal` para ordenar). */
enum class Prioridad(val etiqueta: String) {
    ALTA("Prioridad alta"),
    MEDIA("Prioridad media"),
    BAJA("Prioridad baja")
}

data class Pregunta(
    val id: String,                          // "AUT-01"
    val categoria: Categoria,
    val texto: String,                       // redactada para que "Sí" sea la buena práctica
    val ayuda: String,                       // explicación breve bajo la pregunta
    val puntajes: Map<ValorRespuesta, Int>,  // provisionales; TT-10 define los definitivos
    val orden: Int                           // orden dentro de su categoría
)

data class Recomendacion(
    val id: String,                          // "REC-AUT-01"
    val preguntaId: String,                  // pregunta que la activa
    val valoresQueActivan: Set<ValorRespuesta>,
    val titulo: String,
    val descripcion: String,
    val prioridad: Prioridad,
    val diasParaRevision: Int,               // fecha de revisión de la acción = hoy + días
    val contenidoId: String?                 // botón "Aprender cómo hacerlo"
)

data class Contenido(
    val id: String,                          // "CON-AUT-01"
    val categoria: Categoria,
    val titulo: String,
    val resumen: String,
    val cuerpo: String
)
