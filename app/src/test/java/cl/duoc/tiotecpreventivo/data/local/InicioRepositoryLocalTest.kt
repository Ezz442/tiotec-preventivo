package cl.duoc.tiotecpreventivo.data.local

import cl.duoc.tiotecpreventivo.data.catalogo.Prioridad
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoAccion
import cl.duoc.tiotecpreventivo.repository.AccionDaoFalso
import cl.duoc.tiotecpreventivo.repository.AccionDaoFalso.Companion.accion
import cl.duoc.tiotecpreventivo.repository.AccionRepository
import cl.duoc.tiotecpreventivo.repository.PerfilDaoFalso
import cl.duoc.tiotecpreventivo.repository.PerfilRepository
import cl.duoc.tiotecpreventivo.repository.perfilDePrueba
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** TT-21 (TT-13): Inicio muestra el alias y el contador reales, no datos de ejemplo. */
class InicioRepositoryLocalTest {

    @Test
    fun aliasYPendientesSalenDeLosRepositorios(): Unit = runBlocking {
        val acciones = AccionDaoFalso().apply {
            filas.value = listOf(
                accion(1, Prioridad.ALTA, 1),
                accion(2, Prioridad.BAJA, 1, estado = EstadoAccion.COMPLETADA),
                accion(3, Prioridad.MEDIA, 1)
            )
        }
        val inicio = InicioRepositoryLocal(PerfilRepository(PerfilDaoFalso(perfilDePrueba())), AccionRepository(acciones))

        assertEquals("Tío Prueba", inicio.observarAlias().first())
        assertEquals(2, inicio.observarAccionesPendientes().first())
    }

    @Test
    fun sinPerfilNiAccionesNoInventaDatos(): Unit = runBlocking {
        val inicio = InicioRepositoryLocal(PerfilRepository(PerfilDaoFalso(null)), AccionRepository(AccionDaoFalso()))

        assertNull(inicio.observarAlias().first())
        assertEquals(0, inicio.observarAccionesPendientes().first())
    }
}
