package cl.duoc.tiotecpreventivo.data.catalogo

/**
 * Datos ficticios del catálogo (TT-06).
 *
 * Reglas (ver docs/TT-06-datos-ficticios.md):
 * - Todo vive en el código: sin servidor, sin red y sin datos personales.
 * - Los IDs son estables y no se reutilizan entre versiones de la app.
 * - Cada pregunta está redactada para que "Sí" sea la buena práctica.
 * - Cada pregunta tiene una recomendación que se activa con "No" o "No lo sé".
 * - Los textos orientan; no son un diagnóstico ni un escaneo real del dispositivo.
 */
object CatalogoLocal {

    /**
     * Puntajes definitivos (TT-10). Más puntaje = más riesgo.
     * "No lo sé" suma menos que "No", pero cuenta como riesgo hasta que se confirme.
     */
    val PUNTAJES_NORMAL: Map<ValorRespuesta, Int> = mapOf(
        ValorRespuesta.SI to 0,
        ValorRespuesta.NO_SE to 1,
        ValorRespuesta.NO to 2
    )

    /** Prácticas clave: pesan más y, si no se aplican, la categoría queda al menos en riesgo medio. */
    val PUNTAJES_CLAVE: Map<ValorRespuesta, Int> = mapOf(
        ValorRespuesta.SI to 0,
        ValorRespuesta.NO_SE to 2,
        ValorRespuesta.NO to 3
    )

    private val ACTIVAN_CON_RIESGO = setOf(ValorRespuesta.NO, ValorRespuesta.NO_SE)

    private const val DIAS_ALTA = 7
    private const val DIAS_MEDIA = 14
    private const val DIAS_BAJA = 30

    val categorias: List<Categoria> = Categoria.entries.toList()

    val preguntas: List<Pregunta> = listOf(
        // ---------- Respaldos ----------
        Pregunta(
            id = "RES-01",
            categoria = Categoria.RESPALDOS,
            texto = "¿Tienes una copia de seguridad de tus fotos y documentos importantes?",
            ayuda = "Una copia de seguridad es un duplicado de tus archivos guardado en otro lugar, como la nube o un disco externo.",
            puntajes = PUNTAJES_CLAVE,
            orden = 1,
            clave = true
        ),
        Pregunta(
            id = "RES-02",
            categoria = Categoria.RESPALDOS,
            texto = "¿Tus copias de seguridad se hacen de forma automática?",
            ayuda = "Las copias automáticas se guardan solas cada cierto tiempo, sin que tengas que acordarte.",
            puntajes = PUNTAJES_NORMAL,
            orden = 2
        ),
        Pregunta(
            id = "RES-03",
            categoria = Categoria.RESPALDOS,
            texto = "¿Has comprobado alguna vez que puedes recuperar un archivo desde tu copia?",
            ayuda = "Una copia solo sirve si se puede abrir. Probar con un archivo de vez en cuando confirma que funciona.",
            puntajes = PUNTAJES_NORMAL,
            orden = 3
        ),

        // ---------- Actualizaciones ----------
        Pregunta(
            id = "ACT-01",
            categoria = Categoria.ACTUALIZACIONES,
            texto = "¿Instalas las actualizaciones del sistema cuando el dispositivo las ofrece?",
            ayuda = "Las actualizaciones del sistema corrigen fallas de seguridad conocidas.",
            puntajes = PUNTAJES_CLAVE,
            orden = 1,
            clave = true
        ),
        Pregunta(
            id = "ACT-02",
            categoria = Categoria.ACTUALIZACIONES,
            texto = "¿Tienes activadas las actualizaciones automáticas de las aplicaciones?",
            ayuda = "Así las aplicaciones reciben sus correcciones sin que tengas que revisarlas una por una.",
            puntajes = PUNTAJES_NORMAL,
            orden = 2
        ),
        Pregunta(
            id = "ACT-03",
            categoria = Categoria.ACTUALIZACIONES,
            texto = "¿Instalas aplicaciones solo desde la tienda oficial de tu dispositivo?",
            ayuda = "Por ejemplo, Google Play, App Store o Microsoft Store. Las aplicaciones de otras fuentes pueden venir modificadas.",
            puntajes = PUNTAJES_NORMAL,
            orden = 3
        ),

        // ---------- Autenticación ----------
        Pregunta(
            id = "AUT-01",
            categoria = Categoria.AUTENTICACION,
            texto = "¿Tu dispositivo pide PIN, patrón, contraseña o huella para desbloquearse?",
            ayuda = "El bloqueo de pantalla evita que otra persona use tu dispositivo si lo pierdes o lo dejas sin vigilar.",
            puntajes = PUNTAJES_CLAVE,
            orden = 1,
            clave = true
        ),
        Pregunta(
            id = "AUT-02",
            categoria = Categoria.AUTENTICACION,
            texto = "¿Tienes activada la verificación en dos pasos en tu correo principal?",
            ayuda = "Además de la contraseña, pide un código o una confirmación en tu teléfono al iniciar sesión.",
            puntajes = PUNTAJES_CLAVE,
            orden = 2,
            clave = true
        ),
        Pregunta(
            id = "AUT-03",
            categoria = Categoria.AUTENTICACION,
            texto = "¿Usas una contraseña distinta para cada cuenta importante?",
            ayuda = "Si una contraseña se filtra, las demás cuentas siguen protegidas.",
            puntajes = PUNTAJES_NORMAL,
            orden = 3
        ),

        // ---------- Recuperación de cuentas ----------
        Pregunta(
            id = "RCU-01",
            categoria = Categoria.RECUPERACION,
            texto = "¿Tu correo principal tiene un teléfono o correo de recuperación actualizado?",
            ayuda = "Es la vía que usa el servicio para devolverte el acceso si olvidas la contraseña.",
            puntajes = PUNTAJES_CLAVE,
            orden = 1,
            clave = true
        ),
        Pregunta(
            id = "RCU-02",
            categoria = Categoria.RECUPERACION,
            texto = "¿Guardaste en un lugar seguro los códigos de respaldo de tu verificación en dos pasos?",
            ayuda = "Estos códigos te permiten entrar si pierdes el teléfono que recibe las confirmaciones.",
            puntajes = PUNTAJES_NORMAL,
            orden = 2
        ),
        Pregunta(
            id = "RCU-03",
            categoria = Categoria.RECUPERACION,
            texto = "¿Sabes qué pasos seguir si pierdes el acceso a tu cuenta principal?",
            ayuda = "Conocer el proceso de recuperación antes de necesitarlo ahorra tiempo y evita errores.",
            puntajes = PUNTAJES_NORMAL,
            orden = 3
        )
    )

    val recomendaciones: List<Recomendacion> = listOf(
        // ---------- Respaldos ----------
        Recomendacion(
            id = "REC-RES-01",
            preguntaId = "RES-01",
            valoresQueActivan = ACTIVAN_CON_RIESGO,
            titulo = "Crear una copia de seguridad",
            descripcion = "Guarda una copia de tus fotos y documentos importantes en la nube o en un disco externo.",
            prioridad = Prioridad.ALTA,
            diasParaRevision = DIAS_ALTA,
            contenidoId = "CON-RES-01"
        ),
        Recomendacion(
            id = "REC-RES-02",
            preguntaId = "RES-02",
            valoresQueActivan = ACTIVAN_CON_RIESGO,
            titulo = "Activar las copias automáticas",
            descripcion = "Configura la copia de seguridad para que se haga sola, al menos una vez por semana.",
            prioridad = Prioridad.MEDIA,
            diasParaRevision = DIAS_MEDIA,
            contenidoId = "CON-RES-01"
        ),
        Recomendacion(
            id = "REC-RES-03",
            preguntaId = "RES-03",
            valoresQueActivan = ACTIVAN_CON_RIESGO,
            titulo = "Probar la recuperación de un archivo",
            descripcion = "Elige un archivo de prueba y recupéralo desde tu copia para confirmar que funciona.",
            prioridad = Prioridad.BAJA,
            diasParaRevision = DIAS_BAJA,
            contenidoId = "CON-RES-02"
        ),

        // ---------- Actualizaciones ----------
        Recomendacion(
            id = "REC-ACT-01",
            preguntaId = "ACT-01",
            valoresQueActivan = ACTIVAN_CON_RIESGO,
            titulo = "Instalar las actualizaciones del sistema",
            descripcion = "Revisa en Ajustes si hay una actualización pendiente e instálala con la batería cargada.",
            prioridad = Prioridad.ALTA,
            diasParaRevision = DIAS_ALTA,
            contenidoId = "CON-ACT-01"
        ),
        Recomendacion(
            id = "REC-ACT-02",
            preguntaId = "ACT-02",
            valoresQueActivan = ACTIVAN_CON_RIESGO,
            titulo = "Activar la actualización automática de aplicaciones",
            descripcion = "En la tienda de aplicaciones, activa la opción para que se actualicen solas.",
            prioridad = Prioridad.MEDIA,
            diasParaRevision = DIAS_MEDIA,
            contenidoId = "CON-ACT-01"
        ),
        Recomendacion(
            id = "REC-ACT-03",
            preguntaId = "ACT-03",
            valoresQueActivan = ACTIVAN_CON_RIESGO,
            titulo = "Usar solo la tienda oficial",
            descripcion = "Desinstala las aplicaciones de origen desconocido que no necesites e instala solo desde la tienda oficial.",
            prioridad = Prioridad.MEDIA,
            diasParaRevision = DIAS_MEDIA,
            contenidoId = "CON-ACT-02"
        ),

        // ---------- Autenticación ----------
        Recomendacion(
            id = "REC-AUT-01",
            preguntaId = "AUT-01",
            valoresQueActivan = ACTIVAN_CON_RIESGO,
            titulo = "Activar el bloqueo de pantalla",
            descripcion = "Configura un PIN, patrón, contraseña o huella para desbloquear tu dispositivo.",
            prioridad = Prioridad.ALTA,
            diasParaRevision = DIAS_ALTA,
            contenidoId = "CON-AUT-01"
        ),
        Recomendacion(
            id = "REC-AUT-02",
            preguntaId = "AUT-02",
            valoresQueActivan = ACTIVAN_CON_RIESGO,
            titulo = "Activar la verificación en dos pasos",
            descripcion = "Actívala en la configuración de seguridad de tu correo principal.",
            prioridad = Prioridad.ALTA,
            diasParaRevision = DIAS_ALTA,
            contenidoId = "CON-AUT-02"
        ),
        Recomendacion(
            id = "REC-AUT-03",
            preguntaId = "AUT-03",
            valoresQueActivan = ACTIVAN_CON_RIESGO,
            titulo = "Usar contraseñas distintas",
            descripcion = "Cambia las contraseñas repetidas, empezando por el correo. Un gestor de contraseñas puede ayudarte a recordarlas.",
            prioridad = Prioridad.MEDIA,
            diasParaRevision = DIAS_MEDIA,
            contenidoId = "CON-AUT-01"
        ),

        // ---------- Recuperación de cuentas ----------
        Recomendacion(
            id = "REC-RCU-01",
            preguntaId = "RCU-01",
            valoresQueActivan = ACTIVAN_CON_RIESGO,
            titulo = "Actualizar los datos de recuperación",
            descripcion = "Revisa que el teléfono o correo de recuperación de tu cuenta principal esté vigente.",
            prioridad = Prioridad.ALTA,
            diasParaRevision = DIAS_ALTA,
            contenidoId = "CON-RCU-01"
        ),
        Recomendacion(
            id = "REC-RCU-02",
            preguntaId = "RCU-02",
            valoresQueActivan = ACTIVAN_CON_RIESGO,
            titulo = "Guardar los códigos de respaldo",
            descripcion = "Genera los códigos de respaldo de tu verificación en dos pasos y guárdalos fuera del teléfono.",
            prioridad = Prioridad.MEDIA,
            diasParaRevision = DIAS_MEDIA,
            contenidoId = "CON-RCU-02"
        ),
        Recomendacion(
            id = "REC-RCU-03",
            preguntaId = "RCU-03",
            valoresQueActivan = ACTIVAN_CON_RIESGO,
            titulo = "Conocer el proceso de recuperación",
            descripcion = "Lee los pasos que ofrece tu servicio de correo para recuperar una cuenta antes de necesitarlos.",
            prioridad = Prioridad.BAJA,
            diasParaRevision = DIAS_BAJA,
            contenidoId = "CON-RCU-01"
        )
    )

    val contenidos: List<Contenido> = listOf(
        // ---------- Respaldos ----------
        Contenido(
            id = "CON-RES-01",
            categoria = Categoria.RESPALDOS,
            titulo = "Cómo hacer una copia de seguridad",
            resumen = "Guarda tus archivos importantes en un segundo lugar.",
            cuerpo = "Una copia de seguridad protege tus fotos y documentos si el dispositivo se pierde, se daña o falla. " +
                "Puedes usar el servicio de nube de tu teléfono o un disco externo. " +
                "Lo ideal es que la copia se haga de forma automática y que quede en un lugar distinto del dispositivo original."
        ),
        Contenido(
            id = "CON-RES-02",
            categoria = Categoria.RESPALDOS,
            titulo = "Comprueba que tu copia funciona",
            resumen = "Una copia que no se puede abrir no protege nada.",
            cuerpo = "De vez en cuando, elige un archivo de prueba y recupéralo desde tu copia. " +
                "Si se abre correctamente, tu copia funciona. " +
                "Si no, revisa la configuración del servicio de copias o vuelve a crearla."
        ),

        // ---------- Actualizaciones ----------
        Contenido(
            id = "CON-ACT-01",
            categoria = Categoria.ACTUALIZACIONES,
            titulo = "Por qué actualizar tu dispositivo",
            resumen = "Las actualizaciones corrigen fallas de seguridad.",
            cuerpo = "Cuando se descubre una falla de seguridad, el fabricante publica una actualización que la corrige. " +
                "Mientras no la instales, tu dispositivo sigue expuesto. " +
                "Activa las actualizaciones automáticas del sistema y de las aplicaciones, e instálalas con la batería cargada."
        ),
        Contenido(
            id = "CON-ACT-02",
            categoria = Categoria.ACTUALIZACIONES,
            titulo = "Instala aplicaciones desde la tienda oficial",
            resumen = "Las tiendas oficiales revisan las aplicaciones antes de publicarlas.",
            cuerpo = "Las aplicaciones descargadas desde páginas o mensajes desconocidos pueden venir modificadas. " +
                "Usa la tienda oficial de tu dispositivo, revisa quién publica la aplicación " +
                "y desinstala las que ya no uses."
        ),

        // ---------- Autenticación ----------
        Contenido(
            id = "CON-AUT-01",
            categoria = Categoria.AUTENTICACION,
            titulo = "Bloqueo de pantalla y contraseñas",
            resumen = "Protege la entrada a tu dispositivo y a tus cuentas.",
            cuerpo = "Un PIN, patrón, contraseña o huella evita que otra persona use tu dispositivo. " +
                "En tus cuentas, usa una contraseña distinta para cada una: si una se filtra, las demás siguen protegidas. " +
                "Un gestor de contraseñas puede recordarlas por ti."
        ),
        Contenido(
            id = "CON-AUT-02",
            categoria = Categoria.AUTENTICACION,
            titulo = "Qué es la verificación en dos pasos",
            resumen = "Un segundo paso, además de la contraseña.",
            cuerpo = "La verificación en dos pasos pide un código o una confirmación en tu teléfono cuando inicias sesión. " +
                "Aunque alguien conozca tu contraseña, no podrá entrar sin ese segundo paso. " +
                "Actívala primero en tu correo principal, porque sirve para recuperar las demás cuentas."
        ),

        // ---------- Recuperación de cuentas ----------
        Contenido(
            id = "CON-RCU-01",
            categoria = Categoria.RECUPERACION,
            titulo = "Prepara la recuperación de tu cuenta",
            resumen = "Mantén al día tu teléfono y correo de recuperación.",
            cuerpo = "Si olvidas tu contraseña, el servicio usa tu teléfono o correo de recuperación para devolverte el acceso. " +
                "Revisa que estén vigentes y conoce los pasos de recuperación de tu correo principal " +
                "antes de necesitarlos."
        ),
        Contenido(
            id = "CON-RCU-02",
            categoria = Categoria.RECUPERACION,
            titulo = "Guarda tus códigos de respaldo",
            resumen = "Tu llave de emergencia si pierdes el teléfono.",
            cuerpo = "Al activar la verificación en dos pasos, el servicio ofrece códigos de respaldo de un solo uso. " +
                "Guárdalos impresos o en un lugar seguro fuera del teléfono. " +
                "Si pierdes el teléfono, podrás entrar con uno de esos códigos."
        )
    )
}
