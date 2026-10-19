package cl.duoc.tiotecpreventivo.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import cl.duoc.tiotecpreventivo.data.preferences.Preferencias
import cl.duoc.tiotecpreventivo.data.preferences.Tema
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Lee y guarda las preferencias simples (TT-08). DataStore trabaja fuera del hilo
 * principal: las lecturas son `Flow` y las escrituras son funciones `suspend`.
 */
class PreferenciasRepository(private val dataStore: DataStore<Preferences>) {

    private object Claves {
        val TEMA = stringPreferencesKey("tema")
        val PRIMERA_EJECUCION = booleanPreferencesKey("primera_ejecucion")
        val AVISOS_ACTIVOS = booleanPreferencesKey("avisos_activos")
    }

    /** Emite las preferencias guardadas o los valores por defecto si aún no hay nada. */
    val preferencias: Flow<Preferencias> = dataStore.data
        .catch { error ->
            // Si el archivo no se puede leer, se usan los valores por defecto en vez de cerrar la app.
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { guardadas ->
            Preferencias(
                tema = guardadas[Claves.TEMA].aTema(),
                primeraEjecucion = guardadas[Claves.PRIMERA_EJECUCION] ?: true,
                avisosActivos = guardadas[Claves.AVISOS_ACTIVOS] ?: true
            )
        }
        .distinctUntilChanged()

    suspend fun setTema(tema: Tema) {
        dataStore.edit { it[Claves.TEMA] = tema.name }
    }

    suspend fun marcarPrimeraEjecucionCompleta() {
        dataStore.edit { it[Claves.PRIMERA_EJECUCION] = false }
    }

    suspend fun setAvisosActivos(activos: Boolean) {
        dataStore.edit { it[Claves.AVISOS_ACTIVOS] = activos }
    }

    /** Un valor desconocido (por ejemplo, de una versión antigua) vuelve al tema del sistema. */
    private fun String?.aTema(): Tema =
        Tema.entries.firstOrNull { it.name == this } ?: Tema.SISTEMA
}
