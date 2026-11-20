package cl.duoc.tiotecpreventivo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.duoc.tiotecpreventivo.data.imagenes.AlmacenImagenes
import cl.duoc.tiotecpreventivo.repository.AccionRepository
import cl.duoc.tiotecpreventivo.repository.CatalogoRepository
import cl.duoc.tiotecpreventivo.repository.DispositivoRepository
import cl.duoc.tiotecpreventivo.repository.EvaluacionRepository
import cl.duoc.tiotecpreventivo.repository.GestorRecordatorios
import cl.duoc.tiotecpreventivo.repository.InicioRepository
import cl.duoc.tiotecpreventivo.repository.PerfilRepository
import cl.duoc.tiotecpreventivo.repository.PostsRepository
import cl.duoc.tiotecpreventivo.repository.PreferenciasRepository
import cl.duoc.tiotecpreventivo.repository.SolicitudRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * TT-31: cada fábrica crea su ViewModel y rechaza cualquier otro tipo.
 * Los repositorios son simulados con MockK (relaxed): aquí solo importa el cableado.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelFactoriesTest {

    @Before
    fun dispatcher() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun restaurar() = Dispatchers.resetMain()

    /** Una clase de ViewModel que ninguna fábrica sabe crear. */
    private class ViewModelAjeno : ViewModel()

    private inline fun <reified T : ViewModel> crea(fabrica: ViewModelProvider.Factory) {
        assertTrue(fabrica.create(T::class.java) is T)
    }

    private fun rechazaAjenos(vararg fabricas: ViewModelProvider.Factory) {
        fabricas.forEach { fabrica ->
            try {
                fabrica.create(ViewModelAjeno::class.java)
                fail("${fabrica::class.simpleName} debía rechazar un ViewModel desconocido")
            } catch (e: IllegalArgumentException) {
                assertTrue(e.message!!.isNotBlank())
            }
        }
    }

    @Test
    fun accionesCreaSuViewModel() = crea<AccionesViewModel>(AccionesViewModelFactory(mockk(relaxed = true)))

    @Test
    fun aprendeCreaSuViewModel() =
        crea<AprendeViewModel>(AprendeViewModelFactory(mockk<CatalogoRepository>(relaxed = true)))

    @Test
    fun dispositivosCreaSuViewModel() = crea<DispositivosViewModel>(
        DispositivosViewModelFactory(
            mockk<DispositivoRepository>(relaxed = true),
            mockk<PerfilRepository>(relaxed = true),
            mockk<AlmacenImagenes>(relaxed = true)
        )
    )

    @Test
    fun evaluacionCreaSuViewModel() = crea<EvaluacionViewModel>(
        EvaluacionViewModelFactory(
            1L,
            mockk<CatalogoRepository>(relaxed = true),
            mockk<DispositivoRepository>(relaxed = true),
            mockk<EvaluacionRepository>(relaxed = true)
        )
    )

    @Test
    fun historialCreaSuViewModel() = crea<HistorialViewModel>(
        HistorialViewModelFactory(
            mockk<EvaluacionRepository>(relaxed = true),
            mockk<DispositivoRepository>(relaxed = true)
        )
    )

    @Test
    fun inicioCreaSuViewModel() = crea<InicioViewModel>(InicioViewModelFactory(mockk<InicioRepository>(relaxed = true)))

    @Test
    fun perfilCreaSuViewModel() = crea<PerfilViewModel>(PerfilViewModelFactory(mockk<PerfilRepository>(relaxed = true)))

    @Test
    fun postsCreaSuViewModel() {
        val repositorio = mockk<PostsRepository>()
        coEvery { repositorio.obtenerConsejos(any()) } returns emptyList()

        crea<PostsViewModel>(PostsViewModelFactory(repositorio))
    }

    @Test
    fun preferenciasCreaSuViewModel() =
        crea<PreferenciasViewModel>(PreferenciasViewModel.Factory(mockk<PreferenciasRepository>(relaxed = true)))

    @Test
    fun recordatorioCreaSuViewModel() =
        crea<RecordatorioViewModel>(RecordatorioViewModelFactory(mockk<GestorRecordatorios>(relaxed = true)))

    @Test
    fun resultadoCreaSuViewModel() {
        val evaluaciones = mockk<EvaluacionRepository>(relaxed = true)
        coEvery { evaluaciones.obtenerDetalle(any()) } returns null

        crea<ResultadoViewModel>(
            ResultadoViewModelFactory(1L, evaluaciones, mockk<CatalogoRepository>(relaxed = true))
        )
    }

    @Test
    fun solicitudDetalleCreaSuViewModel() = crea<SolicitudDetalleViewModel>(
        SolicitudDetalleViewModelFactory(1L, mockk<SolicitudRepository>(relaxed = true))
    )

    @Test
    fun solicitudesCreaSuViewModel() = crea<SolicitudesViewModel>(
        SolicitudesViewModelFactory(
            mockk<SolicitudRepository>(relaxed = true),
            mockk<PerfilRepository>(relaxed = true)
        )
    )

    @Test
    fun todasLasFabricasRechazanUnViewModelDesconocido() {
        rechazaAjenos(
            AccionesViewModelFactory(mockk(relaxed = true)),
            AprendeViewModelFactory(mockk<CatalogoRepository>(relaxed = true)),
            DispositivosViewModelFactory(mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true)),
            EvaluacionViewModelFactory(1L, mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true)),
            HistorialViewModelFactory(mockk(relaxed = true), mockk(relaxed = true)),
            InicioViewModelFactory(mockk<InicioRepository>(relaxed = true)),
            PerfilViewModelFactory(mockk<PerfilRepository>(relaxed = true)),
            PostsViewModelFactory(mockk<PostsRepository>(relaxed = true)),
            PreferenciasViewModel.Factory(mockk<PreferenciasRepository>(relaxed = true)),
            RecordatorioViewModelFactory(mockk<GestorRecordatorios>(relaxed = true)),
            ResultadoViewModelFactory(1L, mockk(relaxed = true), mockk(relaxed = true)),
            SolicitudDetalleViewModelFactory(1L, mockk<SolicitudRepository>(relaxed = true)),
            SolicitudesViewModelFactory(mockk<SolicitudRepository>(relaxed = true), mockk<PerfilRepository>(relaxed = true))
        )
    }
}
