package cl.duoc.tiotecpreventivo.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * Paleta TíoTec (TT-05, completada en TT-27). Azul como color principal, gris azulado
 * como secundario y verde como terciario. Las pantallas no usan estos valores
 * directamente: los leen desde MaterialTheme.colorScheme.
 */

// ---------- Tema claro ----------
val AzulPrincipal = Color(0xFF154B85)
val ContenedorAzulClaro = Color(0xFFD5E3FF)
val SobreContenedorAzulClaro = Color(0xFF001B3C)

val GrisAzulado = Color(0xFF53647C)
val ContenedorGrisClaro = Color(0xFFD7E3F8)
val SobreContenedorGrisClaro = Color(0xFF101C2B)

val VerdePrincipal = Color(0xFF38665D)
val ContenedorVerdeClaro = Color(0xFFBBECE0)
val SobreContenedorVerdeClaro = Color(0xFF00201B)

val ErrorClaro = Color(0xFFBA1A1A)
val ContenedorErrorClaro = Color(0xFFFFDAD6)
val SobreContenedorErrorClaro = Color(0xFF410002)

val FondoClaro = Color(0xFFF8FAFC)
val TextoOscuro = Color(0xFF172B45)
val VarianteSuperficieClara = Color(0xFFDFE2EB)
val SobreVarianteClara = Color(0xFF43474E)
val ContornoClaro = Color(0xFF73777F)
val ContornoSuaveClaro = Color(0xFFC3C6CF)

// Superficies de tarjetas y barras, de la más baja a la más alta.
val SuperficieClaraMasBaja = Color(0xFFFFFFFF)
val SuperficieClaraBaja = Color(0xFFF2F4F9)
val SuperficieClaraMedia = Color(0xFFECEEF4)
val SuperficieClaraAlta = Color(0xFFE6E8EE)
val SuperficieClaraMasAlta = Color(0xFFE0E2E8)
val SuperficieClaraTenue = Color(0xFFD8DAE0)

// ---------- Tema oscuro ----------
val AzulClaro = Color(0xFFA8C8FF)
val SobreAzulOscuro = Color(0xFF003258)
val ContenedorAzulOscuro = Color(0xFF004880)
val SobreContenedorAzulOscuro = Color(0xFFD5E3FF)

val GrisAzuladoClaro = Color(0xFFBFCBE0)
val SobreGrisOscuro = Color(0xFF243349)
val ContenedorGrisOscuro = Color(0xFF3B4858)
val SobreContenedorGrisOscuro = Color(0xFFD7E3F8)

val VerdeClaro = Color(0xFFADCFC8)
val SobreVerdeOscuro = Color(0xFF073730)
val ContenedorVerdeOscuro = Color(0xFF1F4D45)
val SobreContenedorVerdeOscuro = Color(0xFFBBECE0)

val ErrorOscuro = Color(0xFFFFB4AB)
val SobreErrorOscuro = Color(0xFF690005)
val ContenedorErrorOscuro = Color(0xFF93000A)
val SobreContenedorErrorOscuro = Color(0xFFFFDAD6)

val FondoOscuro = Color(0xFF101820)
val SuperficieOscura = Color(0xFF18232E)
val TextoClaro = Color(0xFFE1E9F2)
val VarianteSuperficieOscura = Color(0xFF3A4552)
val SobreVarianteOscura = Color(0xFFC3C6CF)
val ContornoOscuro = Color(0xFF8D9199)
val ContornoSuaveOscuro = Color(0xFF43474E)

val SuperficieOscuraMasBaja = Color(0xFF0B121A)
val SuperficieOscuraBaja = Color(0xFF141E28)
val SuperficieOscuraMedia = Color(0xFF18232E)
val SuperficieOscuraAlta = Color(0xFF222D38)
val SuperficieOscuraMasAlta = Color(0xFF2D3843)
val SuperficieOscuraBrillante = Color(0xFF36404B)

// ---------- Riesgo medio (TT-12) ----------
// Material 3 no trae un color de advertencia: se define aquí una sola vez para ambos temas.
val RiesgoMedioClaro = Color(0xFF8A5100)
val RiesgoMedioOscuro = Color(0xFFFFB870)
