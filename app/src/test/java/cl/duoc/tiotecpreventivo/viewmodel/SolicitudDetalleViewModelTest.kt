package cl.duoc.tiotecpreventivo.viewmodel

import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.data.local.entity.EstadoSolicitud
import cl.duoc.tiotecpreventivo.data.local.entity.SolicitudConHistorial
import cl.duoc.tiotecpreventivo.data.local.entity.SolicitudEntity
import cl.duoc.tiotecpreventivo.data.remote.ErrorRemoto
import cl.duoc.tiotecpreventivo.repository.SolicitudRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** TT-31: detalle de una solicitud (cargar, sincronizar, cambiar estado y eliminar) con MockK. */
@OptIn(ExperimentalCoroutinesApi::class)
class SolicitudDetalleViewModelTest {

    private val repo = mockk<SolicitudRepository>()

    private fun detalle(estado: EstadoSolicitud = EstadoSolicitud.PENDIENTE) = SolicitudConHistorial(
        solicitud = SolicitudEntity(1, 1, "DEMO-001", Categoria.RESPALDOS, estado, 0),
        historial = emptyList()
    )

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        coEvery { repo.sincronizarDetalle(1) } just Runs
    }

    @After
    fun restaurar() = Dispatchers.resetMain()

    private fun crear(flujo: Flow<SolicitudConHistorial?> = MutableStateFlow(detalle())): SolicitudDetalleViewModel {
        every { repo.observarConHistorial(1) } returns flujo
        return SolicitudDetalleViewModel(1, repo)
    }

    // ---------- cargar y sincronizar ----------

    @Test
    fun alAbrirMuestraElDetalleYSincroniza() {
        val vm = crear()

        val estado = vm.uiState.value
        assertEquals("DEMO-001", estado.detalle?.solicitud?.codigo)
        assertFalse(estado.cargando)
        assertFalse(estado.sincronizando)
        assertNull(estado.errorCarga)
        assertNull(estado.errorSincronizacion)
        coVerify { repo.sincronizarDetalle(1) }
    }

    @Test
    fun sinDetalleMuestraQueNoExiste() {
        val vm = crear(MutableStateFlow<SolicitudConHistorial?>(null))

        assertEquals("La solicitud no existe.", vm.uiState.value.errorCarga)
    }

    @Test
    fun siRoomFallaMuestraErrorDeCarga() {
        val vm = crear(flow { throw IllegalStateException("fallo") })

        assertEquals("No pudimos cargar el detalle.", vm.uiState.value.errorCarga)
        assertFalse(vm.uiState.value.cargando)
    }

    @Test
    fun errorDelServicioAlSincronizarSeMuestraYLoGuardadoSigueVisible() {
        coEvery { repo.sincronizarDetalle(1) } throws ErrorRemoto("Sin conexión", sinConexion = true)

        val vm = crear()

        assertEquals("Sin conexión", vm.uiState.value.errorSincronizacion)
        assertFalse(vm.uiState.value.sincronizando)
        assertNotNull(vm.uiState.value.detalle)
    }

    @Test
    fun errorInesperadoAlSincronizarMuestraMensajeGenerico() {
        coEvery { repo.sincronizarDetalle(1) } throws IllegalStateException("bug")

        val vm = crear()

        assertEquals("No pudimos actualizar desde el servicio.", vm.uiState.value.errorSincronizacion)
    }

    // ---------- cambiar estado ----------

    @Test
    fun solicitarCambioPideConfirmacionYSePuedeCancelar() {
        val vm = crear()

        vm.solicitarCambio(EstadoSolicitud.EN_REVISION)
        assertEquals(EstadoSolicitud.EN_REVISION, vm.uiState.value.estadoPorConfirmar)

        vm.cancelarCambio()
        assertNull(vm.uiState.value.estadoPorConfirmar)
    }

    @Test
    fun solicitarElMismoEstadoNoHaceNada() {
        val vm = crear()

        vm.solicitarCambio(EstadoSolicitud.PENDIENTE)

        assertNull(vm.uiState.value.estadoPorConfirmar)
    }

    @Test
    fun confirmarSinHaberSolicitadoNoLlamaAlServicio() {
        val vm = crear()

        vm.confirmarCambio()

        coVerify(exactly = 0) { repo.cambiarEstado(any(), any()) }
    }

    @Test
    fun confirmarCambioActualizaElEstadoEnElServicio() {
        coEvery { repo.obtenerConHistorial(1) } returns detalle()
        coEvery { repo.cambiarEstado(1, EstadoSolicitud.EN_REVISION) } returns true
        val vm = crear()
        vm.solicitarCambio(EstadoSolicitud.EN_REVISION)

        vm.confirmarCambio()

        val estado = vm.uiState.value
        assertEquals("Estado actualizado en el servicio.", estado.mensaje)
        assertNull(estado.estadoPorConfirmar)
        assertFalse(estado.actualizando)
        coVerify { repo.cambiarEstado(1, EstadoSolicitud.EN_REVISION) }
    }

    @Test
    fun confirmarCambioSiLaSolicitudYaNoExisteAvisa() {
        coEvery { repo.obtenerConHistorial(1) } returns null
        val vm = crear()
        vm.solicitarCambio(EstadoSolicitud.EN_REVISION)

        vm.confirmarCambio()

        assertEquals("La solicitud ya no existe.", vm.uiState.value.errorOperacion)
        assertNull(vm.uiState.value.estadoPorConfirmar)
        assertFalse(vm.uiState.value.actualizando)
    }

    @Test
    fun confirmarCambioSiYaTeniaEseEstadoLoAvisaSinLlamarAlServicio() {
        coEvery { repo.obtenerConHistorial(1) } returns detalle(EstadoSolicitud.EN_REVISION)
        val vm = crear()
        vm.solicitarCambio(EstadoSolicitud.EN_REVISION)

        vm.confirmarCambio()

        assertEquals("La solicitud ya tiene ese estado.", vm.uiState.value.mensaje)
        coVerify(exactly = 0) { repo.cambiarEstado(any(), any()) }
    }

    @Test
    fun confirmarCambioSiElRepositorioDevuelveFalseMuestraError() {
        coEvery { repo.obtenerConHistorial(1) } returns detalle()
        coEvery { repo.cambiarEstado(1, any()) } returns false
        val vm = crear()
        vm.solicitarCambio(EstadoSolicitud.CANCELADA)

        vm.confirmarCambio()

        assertEquals("No pudimos actualizar la solicitud.", vm.uiState.value.errorOperacion)
    }

    @Test
    fun confirmarCambioConErrorDelServicioMuestraSuMensaje() {
        coEvery { repo.obtenerConHistorial(1) } returns detalle()
        coEvery { repo.cambiarEstado(1, any()) } throws ErrorRemoto("Transición no permitida", codigoHttp = 409)
        val vm = crear()
        vm.solicitarCambio(EstadoSolicitud.RESUELTA)

        vm.confirmarCambio()

        assertEquals("Transición no permitida", vm.uiState.value.errorOperacion)
        assertFalse(vm.uiState.value.actualizando)
    }

    @Test
    fun confirmarCambioConErrorInesperadoMuestraMensajeGenerico() {
        coEvery { repo.obtenerConHistorial(1) } returns detalle()
        coEvery { repo.cambiarEstado(1, any()) } throws IllegalStateException("bug")
        val vm = crear()
        vm.solicitarCambio(EstadoSolicitud.CANCELADA)

        vm.confirmarCambio()

        assertEquals("No pudimos cambiar el estado. Reintenta.", vm.uiState.value.errorOperacion)
    }

    // ---------- eliminar ----------

    @Test
    fun solicitarEliminarAbreLaConfirmacionYSePuedeCancelar() {
        val vm = crear()

        vm.solicitarEliminar()
        assertTrue(vm.uiState.value.confirmarEliminar)

        vm.cancelarEliminar()
        assertFalse(vm.uiState.value.confirmarEliminar)
    }

    @Test
    fun confirmarEliminarSinPedirloNoHaceNada() {
        val vm = crear()

        vm.confirmarEliminar()

        coVerify(exactly = 0) { repo.eliminar(any()) }
    }

    @Test
    fun confirmarEliminarBorraEnElServicioYMarcaLaSolicitudComoEliminada() {
        coEvery { repo.eliminar(1) } returns true
        val vm = crear()
        vm.solicitarEliminar()

        vm.confirmarEliminar()

        val estado = vm.uiState.value
        assertTrue(estado.eliminada)
        assertFalse(estado.confirmarEliminar)
        assertFalse(estado.eliminando)
        coVerify { repo.eliminar(1) }
    }

    @Test
    fun eliminarConErrorDelServicioMuestraSuMensajeYNoLaMarcaEliminada() {
        coEvery { repo.eliminar(1) } throws ErrorRemoto("Sin conexión", sinConexion = true)
        val vm = crear()
        vm.solicitarEliminar()

        vm.confirmarEliminar()

        assertEquals("Sin conexión", vm.uiState.value.errorOperacion)
        assertFalse(vm.uiState.value.eliminada)
        assertFalse(vm.uiState.value.eliminando)
    }

    @Test
    fun eliminarConErrorInesperadoMuestraMensajeGenerico() {
        coEvery { repo.eliminar(1) } throws IllegalStateException("bug")
        val vm = crear()
        vm.solicitarEliminar()

        vm.confirmarEliminar()

        assertEquals("No pudimos eliminar la solicitud. Reintenta.", vm.uiState.value.errorOperacion)
    }

    @Test
    fun limpiarMensajeBorraElMensaje() {
        coEvery { repo.obtenerConHistorial(1) } returns detalle(EstadoSolicitud.EN_REVISION)
        val vm = crear()
        vm.solicitarCambio(EstadoSolicitud.EN_REVISION)
        vm.confirmarCambio()
        assertNotNull(vm.uiState.value.mensaje)

        vm.limpiarMensaje()

        assertNull(vm.uiState.value.mensaje)
    }
}
