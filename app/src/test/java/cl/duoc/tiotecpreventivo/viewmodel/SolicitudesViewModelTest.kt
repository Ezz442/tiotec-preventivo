package cl.duoc.tiotecpreventivo.viewmodel

import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoSolicitud
import cl.duoc.tiotecpreventivo.repository.PerfilDaoFalso
import cl.duoc.tiotecpreventivo.repository.PerfilRepository
import cl.duoc.tiotecpreventivo.repository.SolicitudDaoFalso
import cl.duoc.tiotecpreventivo.repository.SolicitudRepository
import cl.duoc.tiotecpreventivo.repository.perfilDePrueba
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** TT-21 (TT-17): solicitudes simuladas, categoría obligatoria y transiciones de estado. */
@OptIn(ExperimentalCoroutinesApi::class)
class SolicitudesViewModelTest {

    private val dao = SolicitudDaoFalso()
    private val repositorio = SolicitudRepository(dao) { 1_000L }

    @Before
    fun dispatcher() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun restaurar() = Dispatchers.resetMain()

    private fun lista(perfil: PerfilDaoFalso = PerfilDaoFalso(perfilDePrueba())) =
        SolicitudesViewModel(repositorio, PerfilRepository(perfil))

    // ---------- Formulario y lista ----------

    @Test
    fun sinPerfilNoSePuedeCrear() {
        val vm = lista(PerfilDaoFalso(null))

        vm.abrirFormulario()

        assertFalse(vm.uiState.value.mostrarFormulario)
        assertEquals("Crea tu perfil antes de solicitar asistencia.", vm.uiState.value.errorOperacion)
    }

    @Test
    fun categoriaObligatoriaMuestraErrorYSeBorraAlElegir() {
        val vm = lista()
        vm.abrirFormulario()

        vm.guardar()
        assertEquals("Selecciona una categoría.", vm.uiState.value.errorCategoria)
        assertTrue(dao.solicitudes.value.isEmpty())

        vm.seleccionarCategoria(Categoria.AUTENTICACION)
        assertNull(vm.uiState.value.errorCategoria)
    }

    @Test
    fun crearGuardaPendienteConCodigoDemoEHistorial() {
        val vm = lista()
        vm.abrirFormulario()
        vm.seleccionarCategoria(Categoria.RECUPERACION)

        vm.guardar()

        val solicitud = vm.uiState.value.solicitudes.single()
        assertEquals("DEMO-001", solicitud.codigo)
        assertEquals(EstadoSolicitud.PENDIENTE, solicitud.estado)
        assertEquals(1, dao.historial.value.size)
        assertFalse(vm.uiState.value.mostrarFormulario)
        assertTrue(vm.uiState.value.mensaje!!.contains("No se envió a ningún servicio"))
    }

    @Test
    fun cerrarElFormularioNoCreaNada() {
        val vm = lista()
        vm.abrirFormulario()
        vm.seleccionarCategoria(Categoria.RESPALDOS)

        vm.cerrarFormulario()

        assertTrue(dao.solicitudes.value.isEmpty())
        assertNull(vm.uiState.value.categoriaSeleccionada)
    }

    // ---------- Detalle y cambio de estado ----------

    private fun detalleDeUnaNueva(): SolicitudDetalleViewModel {
        val vm = lista()
        vm.abrirFormulario()
        vm.seleccionarCategoria(Categoria.ACTUALIZACIONES)
        vm.guardar()
        return SolicitudDetalleViewModel(dao.solicitudes.value.single().id, repositorio)
    }

    @Test
    fun cambiarEstadoPideConfirmacionYCancelarNoCambia() {
        val detalle = detalleDeUnaNueva()

        detalle.solicitarCambio(EstadoSolicitud.EN_REVISION)
        assertEquals(EstadoSolicitud.EN_REVISION, detalle.uiState.value.estadoPorConfirmar)

        detalle.cancelarCambio()

        assertNull(detalle.uiState.value.estadoPorConfirmar)
        assertEquals(EstadoSolicitud.PENDIENTE, dao.solicitudes.value.single().estado)
        assertEquals(1, dao.historial.value.size)
    }

    @Test
    fun cadaCambioConfirmadoQuedaEnElHistorial() {
        val detalle = detalleDeUnaNueva()

        listOf(EstadoSolicitud.EN_REVISION, EstadoSolicitud.RESUELTA).forEach {
            detalle.solicitarCambio(it)
            detalle.confirmarCambio()
        }

        val estado = detalle.uiState.value.detalle!!
        assertEquals(EstadoSolicitud.RESUELTA, estado.solicitud.estado)
        assertEquals(
            listOf(EstadoSolicitud.PENDIENTE, EstadoSolicitud.EN_REVISION, EstadoSolicitud.RESUELTA),
            estado.historial.map { it.estado }
        )
        assertEquals("Estado actualizado en la simulación.", detalle.uiState.value.mensaje)
    }

    @Test
    fun elegirElMismoEstadoNoHaceNada() {
        val detalle = detalleDeUnaNueva()

        detalle.solicitarCambio(EstadoSolicitud.PENDIENTE)

        assertNull(detalle.uiState.value.estadoPorConfirmar)
        assertEquals(1, dao.historial.value.size)
    }

    @Test
    fun solicitudInexistenteMuestraError() {
        val detalle = SolicitudDetalleViewModel(99, repositorio)

        assertEquals("La solicitud no existe.", detalle.uiState.value.errorCarga)
    }

    @Test
    fun errorAlCambiarNoAgregaHistorial() {
        val detalle = detalleDeUnaNueva()
        dao.falla = true

        detalle.solicitarCambio(EstadoSolicitud.CANCELADA)
        detalle.confirmarCambio()

        assertEquals("No pudimos cambiar el estado. Reintenta.", detalle.uiState.value.errorOperacion)
        assertEquals(EstadoSolicitud.PENDIENTE, dao.solicitudes.value.single().estado)
        assertEquals(1, dao.historial.value.size)
    }
}
