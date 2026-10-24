package cl.duoc.tiotecpreventivo.domain

import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.data.local.entity.ExperienciaDigital
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** TT-26: reglas de los formularios, sin Android ni ViewModels. */
class ValidadorFormulariosTest {

    private val v = ValidadorFormularios

    @Test
    fun perfilVacioDaUnErrorPorCampo() {
        val r = v.validarPerfil("   ", null)

        assertEquals(v.ALIAS_VACIO, r.errorAlias)
        assertEquals(v.EXPERIENCIA_VACIA, r.errorExperiencia)
        assertFalse(r.valida)
    }

    @Test
    fun aliasRespetaLosLimitesDeLargo() {
        assertEquals(v.ALIAS_LARGO, v.validarPerfil("T", ExperienciaDigital.PRINCIPIANTE).errorAlias)
        assertNull(v.validarPerfil("Tí", ExperienciaDigital.PRINCIPIANTE).errorAlias)
        assertNull(v.validarPerfil("x".repeat(30), ExperienciaDigital.PRINCIPIANTE).errorAlias)
        assertEquals(v.ALIAS_LARGO, v.validarPerfil("x".repeat(31), ExperienciaDigital.PRINCIPIANTE).errorAlias)
    }

    @Test
    fun elLargoSeMideDespuesDeQuitarEspacios() {
        // " T " tiene 3 caracteres, pero sin espacios queda en 1.
        assertEquals(v.ALIAS_LARGO, v.validarPerfil(" T ", ExperienciaDigital.PRINCIPIANTE).errorAlias)
    }

    @Test
    fun perfilValidoEntregaElAliasNormalizado() {
        val r = v.validarPerfil("  Tía    Demo  ", ExperienciaDigital.AVANZADO)

        assertTrue(r.valida)
        assertEquals("Tía Demo", r.aliasLimpio)
    }

    @Test
    fun aliasConCorreoTelefonoORutSeRechaza() {
        listOf("tia@correo.cl", "+56 9 1234 5678", "12.345.678-9", "Tía 1234567").forEach {
            assertEquals(it, v.ALIAS_PERSONAL, v.validarPerfil(it, ExperienciaDigital.PRINCIPIANTE).errorAlias)
        }
    }

    @Test
    fun pocosNumerosSiSePermiten() {
        assertNull(v.validarPerfil("Tío 2026", ExperienciaDigital.PRINCIPIANTE).errorAlias)
        assertNull(v.validarDispositivo("iPhone 15", SistemaOperativo.IOS).errorNombre)
        assertFalse(v.pareceDatoPersonal("Tablet 123456")) // 6 dígitos
        assertTrue(v.pareceDatoPersonal("Tablet 1234567")) // 7 dígitos
    }

    @Test
    fun dispositivoVacioDaUnErrorPorCampo() {
        val r = v.validarDispositivo("", null)

        assertEquals(v.NOMBRE_VACIO, r.errorNombre)
        assertEquals(v.SISTEMA_VACIO, r.errorSistemaOperativo)
        assertFalse(r.valida)
    }

    @Test
    fun dispositivoValidoConLimitesYDatoPersonal() {
        assertEquals(v.NOMBRE_LARGO, v.validarDispositivo("A", SistemaOperativo.ANDROID).errorNombre)
        assertEquals(v.NOMBRE_LARGO, v.validarDispositivo("x".repeat(31), SistemaOperativo.ANDROID).errorNombre)
        assertEquals(v.NOMBRE_PERSONAL, v.validarDispositivo("tio@correo.cl", SistemaOperativo.ANDROID).errorNombre)

        val r = v.validarDispositivo("  Teléfono   de prueba ", SistemaOperativo.ANDROID)
        assertTrue(r.valida)
        assertEquals("Teléfono de prueba", r.nombreLimpio)
    }

    @Test
    fun solicitudExigeCategoria() {
        assertEquals(v.CATEGORIA_VACIA, v.validarSolicitud(null))
        assertNull(v.validarSolicitud(Categoria.entries.first()))
    }

    @Test
    fun cambiosAlCrearUnDispositivo() {
        assertFalse(v.hayCambiosDispositivo("", null, null, null))
        assertFalse(v.hayCambiosDispositivo("   ", null, null, null))
        assertTrue(v.hayCambiosDispositivo("Tablet", null, null, null))
        assertTrue(v.hayCambiosDispositivo("", SistemaOperativo.IOS, null, null))
    }

    @Test
    fun cambiosAlEditarUnDispositivo() {
        val nombre = "Teléfono de prueba"
        val so = SistemaOperativo.ANDROID

        assertFalse(v.hayCambiosDispositivo(nombre, so, nombre, so))
        assertFalse(v.hayCambiosDispositivo("  Teléfono  de prueba ", so, nombre, so))
        assertTrue(v.hayCambiosDispositivo("Teléfono nuevo", so, nombre, so))
        assertTrue(v.hayCambiosDispositivo(nombre, SistemaOperativo.IOS, nombre, so))
    }

    @Test
    fun contadorDeCaracteres() {
        assertEquals("0/30", v.contador(""))
        assertEquals("8/30", v.contador("Tía Demo"))
    }
}
