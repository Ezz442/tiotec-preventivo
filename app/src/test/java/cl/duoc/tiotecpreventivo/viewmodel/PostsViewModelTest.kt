package cl.duoc.tiotecpreventivo.viewmodel

import cl.duoc.tiotecpreventivo.data.remote.PostsApi
import cl.duoc.tiotecpreventivo.data.remote.model.PostDto
import cl.duoc.tiotecpreventivo.repository.PostsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import java.io.IOException

// #pruebas de la api externa en el ViewModel (cargando, exito, error)
/** TT-30: estados de PostsViewModel (cargando, lista, vacío, error y reintento). */
@OptIn(ExperimentalCoroutinesApi::class)
class PostsViewModelTest {

    private class ApiFalsa(var resultado: () -> List<PostDto>) : PostsApi {
        override suspend fun posts(limite: Int?): List<PostDto> = resultado()
        override suspend fun post(id: Int): PostDto = error("no se usa")
    }

    @Before
    fun antes() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun despues() = Dispatchers.resetMain()

    @Test
    fun alAbrirCargaLosConsejos() {
        val api = ApiFalsa { listOf(PostDto(1, 1, "titulo", "texto")) }

        val estado = PostsViewModel(PostsRepository(api)).uiState.value

        assertFalse(estado.cargando)
        assertNull(estado.error)
        assertEquals(1, estado.consejos.size)
        assertEquals("Titulo", estado.consejos[0].titulo)
    }

    @Test
    fun conListaVaciaMarcaEstadoVacio() {
        val estado = PostsViewModel(PostsRepository(ApiFalsa { emptyList() })).uiState.value

        assertTrue(estado.vacio)
        assertNull(estado.error)
    }

    @Test
    fun sinInternetMuestraErrorYPermiteReintentar() {
        val api = ApiFalsa { throw IOException("sin red") }
        val vm = PostsViewModel(PostsRepository(api))

        assertFalse(vm.uiState.value.cargando)
        assertNotNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.consejos.isEmpty())

        api.resultado = { listOf(PostDto(1, 2, "ok", "ok")) }
        vm.cargar()

        assertNull(vm.uiState.value.error)
        assertEquals(1, vm.uiState.value.consejos.size)
    }

    @Test
    fun errorInesperadoMuestraMensajeGenerico() {
        val vm = PostsViewModel(PostsRepository(ApiFalsa { throw IllegalStateException("bug") }))

        assertEquals("No pudimos cargar los consejos de la comunidad.", vm.uiState.value.error)
    }
}
