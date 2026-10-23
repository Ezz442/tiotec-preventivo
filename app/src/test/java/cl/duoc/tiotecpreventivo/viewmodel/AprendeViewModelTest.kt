package cl.duoc.tiotecpreventivo.viewmodel

import cl.duoc.tiotecpreventivo.data.catalogo.CatalogoLocal
import cl.duoc.tiotecpreventivo.data.catalogo.CatalogoRepositoryLocal
import cl.duoc.tiotecpreventivo.data.catalogo.Categoria
import cl.duoc.tiotecpreventivo.data.catalogo.Contenido
import cl.duoc.tiotecpreventivo.repository.CatalogoRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** TT-15: filtros de Aprende y estados de carga/error desde el ViewModel, con el catálogo local. */
class AprendeViewModelTest {

    @Test
    fun alAbrirMuestraTodosLosContenidosYLasCuatroCategorias() {
        val estado = AprendeViewModel(CatalogoRepositoryLocal()).uiState.value

        assertFalse(estado.cargando)
        assertNull(estado.error)
        assertNull(estado.categoriaSeleccionada)
        assertEquals(Categoria.entries.toList(), estado.categorias)
        assertEquals(CatalogoLocal.contenidos, estado.contenidos)
    }

    @Test
    fun filtrarPorCategoriaMuestraSoloEsaCategoria() {
        val viewModel = AprendeViewModel(CatalogoRepositoryLocal())

        Categoria.entries.forEach { categoria ->
            viewModel.seleccionarCategoria(categoria)
            val estado = viewModel.uiState.value
            assertEquals(categoria, estado.categoriaSeleccionada)
            assertTrue(categoria.name, estado.contenidos.isNotEmpty())
            assertTrue(categoria.name, estado.contenidos.all { it.categoria == categoria })
        }
    }

    @Test
    fun volverATodosMuestraTodoOtraVez() {
        val viewModel = AprendeViewModel(CatalogoRepositoryLocal())
        viewModel.seleccionarCategoria(Categoria.RESPALDOS)

        viewModel.seleccionarCategoria(null)

        assertEquals(CatalogoLocal.contenidos.size, viewModel.uiState.value.contenidos.size)
    }

    @Test
    fun categoriaSinContenidosQuedaVacia() {
        val sinRecuperacion = object : CatalogoRepository by CatalogoRepositoryLocal() {
            override fun contenidos(categoria: Categoria?): List<Contenido> =
                CatalogoLocal.contenidos.filter {
                    it.categoria != Categoria.RECUPERACION && (categoria == null || it.categoria == categoria)
                }
        }
        val viewModel = AprendeViewModel(sinRecuperacion)

        viewModel.seleccionarCategoria(Categoria.RECUPERACION)

        assertTrue(viewModel.uiState.value.contenidos.isEmpty())
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun errorAlLeerElCatalogoPermiteReintentar() {
        var falla = true
        val catalogo = object : CatalogoRepository by CatalogoRepositoryLocal() {
            override fun contenidos(categoria: Categoria?): List<Contenido> {
                if (falla) error("Catálogo no disponible")
                return CatalogoLocal.contenidos
            }
        }
        val viewModel = AprendeViewModel(catalogo)
        assertEquals("No pudimos cargar los contenidos.", viewModel.uiState.value.error)

        falla = false
        viewModel.reintentar()

        assertNull(viewModel.uiState.value.error)
        assertEquals(CatalogoLocal.contenidos.size, viewModel.uiState.value.contenidos.size)
    }
}
