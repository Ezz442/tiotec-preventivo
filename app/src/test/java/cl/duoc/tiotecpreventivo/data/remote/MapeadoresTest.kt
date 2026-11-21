package cl.duoc.tiotecpreventivo.data.remote

import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoSolicitud
import cl.duoc.tiotecpreventivo.data.local.entity.SistemaOperativo
import cl.duoc.tiotecpreventivo.data.remote.model.DispositivoDto
import cl.duoc.tiotecpreventivo.data.remote.model.HistorialDto
import cl.duoc.tiotecpreventivo.data.remote.model.SolicitudDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// #pruebas de la conversion servidor a datos locales
/** TT-31: conversión de los JSON de los microservicios a entidades de Room. */
class MapeadoresTest {

    private fun solicitud(
        id: Long = 7,
        codigo: String? = "DEMO-007",
        categoria: String = "RESPALDOS",
        estado: String = "PENDIENTE",
        historial: List<HistorialDto>? = null
    ) = SolicitudDto(id, codigo, "Ana", categoria, estado, creadaEn = 100, actualizadaEn = 200, historial = historial)

    private fun dispositivo(sistema: String = "ANDROID") = DispositivoDto(
        id = 3, nombre = "Table de Ana", sistemaOperativo = sistema,
        archivado = true, creadoEn = 10, actualizadoEn = 20
    )

    @Test
    fun solicitudValidaSeConvierteConLosMismosDatos() {
        val e = solicitud(estado = "EN_REVISION", categoria = "AUTENTICACION").aEntidad(perfilId = 5)!!

        assertEquals(7L, e.id)
        assertEquals(5L, e.perfilId)
        assertEquals("DEMO-007", e.codigo)
        assertEquals(Categoria.AUTENTICACION, e.categoria)
        assertEquals(EstadoSolicitud.EN_REVISION, e.estado)
        assertEquals(100L, e.creadaEn)
    }

    @Test
    fun sinCodigoSeGeneraDemoConTresDigitos() {
        assertEquals("DEMO-007", solicitud(codigo = null).aEntidad(1)!!.codigo)
        assertEquals("DEMO-042", solicitud(id = 42, codigo = null).aEntidad(1)!!.codigo)
    }

    @Test
    fun categoriaOEstadoDesconocidosDevuelvenNull() {
        assertNull(solicitud(categoria = "OTRA").aEntidad(1))
        assertNull(solicitud(estado = "INVENTADO").aEntidad(1))
    }

    @Test
    fun historialNuloSeConvierteEnListaVacia() {
        assertTrue(solicitud(historial = null).historialEntidades().isEmpty())
    }

    @Test
    fun historialSeConvierteEIgnoraEstadosDesconocidos() {
        val dto = solicitud(
            historial = listOf(
                HistorialDto(1, "PENDIENTE", 10, "Creada"),
                HistorialDto(2, "RARO", 20, "Ignorado"),
                HistorialDto(3, "RESUELTA", 30, "Lista")
            )
        )

        val lista = dto.historialEntidades()

        assertEquals(2, lista.size)
        assertEquals(listOf(1L, 3L), lista.map { it.id })
        assertTrue(lista.all { it.solicitudId == 7L })
        assertEquals(EstadoSolicitud.RESUELTA, lista[1].estado)
        assertEquals("Lista", lista[1].nota)
        assertEquals(30L, lista[1].fecha)
    }

    @Test
    fun dispositivoValidoSeConvierteYConservaLaImagenLocal() {
        val e = dispositivo("IOS").aEntidad(perfilId = 9, imagenRuta = "fotos/a.jpg")!!

        assertEquals(3L, e.id)
        assertEquals(9L, e.perfilId)
        assertEquals("Table de Ana", e.nombre)
        assertEquals(SistemaOperativo.IOS, e.sistemaOperativo)
        assertEquals("fotos/a.jpg", e.imagenRuta)
        assertTrue(e.archivado)
        assertEquals(10L, e.creadoEn)
        assertEquals(20L, e.actualizadoEn)
    }

    @Test
    fun dispositivoSinImagenLaDejaEnNull() {
        val e = dispositivo().aEntidad(perfilId = 1)
        assertNotNull(e)
        assertNull(e!!.imagenRuta)
        assertFalse(e.nombre.isEmpty())
    }

    @Test
    fun sistemaOperativoDesconocidoDevuelveNull() {
        assertNull(dispositivo("LINUX").aEntidad(1))
    }
}
