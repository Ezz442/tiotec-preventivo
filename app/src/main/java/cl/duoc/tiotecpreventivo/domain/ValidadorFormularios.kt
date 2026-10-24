package cl.duoc.tiotecpreventivo.domain

import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.data.local.entity.ExperienciaDigital
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo

/** Resultado de validar el formulario de perfil (TT-26). */
data class ValidacionPerfil(
    val aliasLimpio: String,
    val errorAlias: String? = null,
    val errorExperiencia: String? = null
) {
    val valida: Boolean get() = errorAlias == null && errorExperiencia == null
}

/** Resultado de validar el formulario de dispositivo (TT-26). */
data class ValidacionDispositivo(
    val nombreLimpio: String,
    val errorNombre: String? = null,
    val errorSistemaOperativo: String? = null
) {
    val valida: Boolean get() = errorNombre == null && errorSistemaOperativo == null
}

/**
 * Reglas de los formularios de la app, en un solo lugar y sin Android (TT-26).
 *
 * Los ViewModels llaman a estas funciones al guardar y copian los errores por campo
 * a su UiState; las pantallas solo los muestran. La fecha y hora de los recordatorios
 * se validan en [ValidadorRecordatorio] y la respuesta de cada pregunta en
 * EvaluacionViewModel, con el mismo criterio.
 *
 * No se agregan campos personales: la app solo pide un alias, el nombre de un
 * dispositivo de prueba, selecciones y fechas.
 */
object ValidadorFormularios {

    const val LARGO_MINIMO = 2
    const val LARGO_MAXIMO = 30

    /** Desde esta cantidad de dígitos el texto parece un teléfono o un RUT. */
    const val DIGITOS_DATO_PERSONAL = 7

    const val ALIAS_VACIO = "Ingresa un alias ficticio."
    const val ALIAS_LARGO = "El alias debe tener entre 2 y 30 caracteres."
    const val ALIAS_PERSONAL = "Usa un alias ficticio: no ingreses correos, teléfonos ni RUT."
    const val EXPERIENCIA_VACIA = "Selecciona tu nivel de experiencia digital."

    const val NOMBRE_VACIO = "Ingresa un nombre ficticio."
    const val NOMBRE_LARGO = "El nombre debe tener entre 2 y 30 caracteres."
    const val NOMBRE_PERSONAL = "Usa un nombre de prueba: no ingreses correos, teléfonos ni RUT."
    const val NOMBRE_REPETIDO = "Ya existe un dispositivo con ese nombre."
    const val SISTEMA_VACIO = "Selecciona un sistema operativo."

    const val CATEGORIA_VACIA = "Selecciona una categoría."

    /** Quita espacios al inicio y al final y deja uno solo entre palabras. */
    fun normalizar(texto: String): String = texto.trim().replace(Regex("\\s+"), " ")

    /** Un correo (tiene @) o un número largo (teléfono, RUT) no corresponde a un alias ficticio. */
    fun pareceDatoPersonal(texto: String): Boolean =
        '@' in texto || texto.count { it.isDigit() } >= DIGITOS_DATO_PERSONAL

    fun validarPerfil(alias: String, experiencia: ExperienciaDigital?): ValidacionPerfil {
        val limpio = normalizar(alias)
        return ValidacionPerfil(
            aliasLimpio = limpio,
            errorAlias = errorTexto(limpio, ALIAS_VACIO, ALIAS_LARGO, ALIAS_PERSONAL),
            errorExperiencia = if (experiencia == null) EXPERIENCIA_VACIA else null
        )
    }

    fun validarDispositivo(nombre: String, sistema: SistemaOperativo?): ValidacionDispositivo {
        val limpio = normalizar(nombre)
        return ValidacionDispositivo(
            nombreLimpio = limpio,
            errorNombre = errorTexto(limpio, NOMBRE_VACIO, NOMBRE_LARGO, NOMBRE_PERSONAL),
            errorSistemaOperativo = if (sistema == null) SISTEMA_VACIO else null
        )
    }

    fun validarSolicitud(categoria: Categoria?): String? =
        if (categoria == null) CATEGORIA_VACIA else null

    /**
     * Al editar hay cambios si el nombre o el sistema son distintos al guardado;
     * al crear, si ya se escribió o eligió algo. Sirve para confirmar antes de descartar.
     */
    fun hayCambiosDispositivo(
        nombre: String,
        sistema: SistemaOperativo?,
        nombreGuardado: String?,
        sistemaGuardado: SistemaOperativo?
    ): Boolean =
        normalizar(nombre) != normalizar(nombreGuardado.orEmpty()) || sistema != sistemaGuardado

    /** Texto para el contador bajo el campo, por ejemplo "12/30". */
    fun contador(texto: String): String = "${texto.length}/$LARGO_MAXIMO"

    private fun errorTexto(limpio: String, vacio: String, largo: String, personal: String): String? =
        when {
            limpio.isEmpty() -> vacio
            limpio.length !in LARGO_MINIMO..LARGO_MAXIMO -> largo
            pareceDatoPersonal(limpio) -> personal
            else -> null
        }
}
