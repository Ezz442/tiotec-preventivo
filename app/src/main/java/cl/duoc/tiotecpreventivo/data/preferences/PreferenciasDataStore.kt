package cl.duoc.tiotecpreventivo.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/** Un solo DataStore por app, guardado en el archivo `preferencias_tiotec.preferences_pb`. */
val Context.preferenciasDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "preferencias_tiotec"
)
