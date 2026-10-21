# TT-08 · Preferencias locales con DataStore

**Responsable:** Enzo Morales · **Revisión:** Gerard Zambrano
**Alcance:** Evaluación Parcial 2 · preferencias simples en el teléfono, sin red.
**Base:** sección 7 de `docs/TT-04-modelo-local.md`. Reemplaza la antigua integración Retrofit/API.

## 1. Qué va en DataStore y qué va en Room

| Dato | Dónde | Motivo |
|---|---|---|
| Tema (Sistema / Claro / Oscuro) | DataStore | Valor simple clave-valor |
| Primera ejecución (bienvenida y aviso de datos ficticios) | DataStore | Valor simple |
| Preferencia de avisos | DataStore | Valor simple; el permiso del sistema se revisa en TT-16 |
| Perfil, dispositivos, evaluaciones, acciones, recordatorios, solicitudes | Room (TT-07) | Registros con relaciones y CRUD |

Archivo en el teléfono: `files/datastore/preferencias_tiotec.preferences_pb` (interno de la app).

## 2. Estructura

| Archivo | Rol |
|---|---|
| `data/preferences/Preferencias.kt` | `Preferencias` y `Tema` (con `usarModoOscuro`) |
| `data/preferences/PreferenciasDataStore.kt` | Un único `DataStore` por app (`Context.preferenciasDataStore`) |
| `repository/PreferenciasRepository.kt` | `preferencias: Flow<Preferencias>`, `setTema`, `setAvisosActivos`, `marcarPrimeraEjecucionCompleta` |
| `viewmodel/PreferenciasViewModel.kt` | `uiState: StateFlow<PreferenciasUiState>` con `stateIn`; eventos `onTemaChange`, `onAvisosChange`, `onBienvenidaVista`, `onMensajeMostrado`; atrapa errores al guardar |
| `ui/screens/AjustesScreen.kt` | `AjustesRuta` observa con `collectAsState()` y muestra el Snackbar; `AjustesScreen` sin estado, con previews claro y oscuro |
| `data/AppContainer.kt` | Agrega `preferenciasRepository` |
| `MainActivity.kt` | Crea el ViewModel y aplica el tema guardado |
| `ui/navigation/TioTecApp.kt` · `ui/screens/PerfilScreen.kt` | Ruta `ajustes` y botón **Ajustes** en Perfil |

Flujo: `AjustesScreen` → evento → `PreferenciasViewModel` → `PreferenciasRepository` → DataStore → `Flow` → `StateFlow` → `collectAsState()` → la pantalla se redibuja.

## 3. Sin bloquear la interfaz y errores al guardar

- Las lecturas son `Flow` y las escrituras son funciones `suspend` lanzadas en `viewModelScope`; DataStore hace la E/S fuera del hilo principal.
- Mientras llega el primer valor, `PreferenciasUiState.cargando = true`. `MainActivity` espera ese primer valor antes de dibujar, para no mostrar un instante el tema equivocado.
- Si el archivo no se puede **leer** (`IOException`), se usan los valores por defecto en vez de cerrar la app.
- Si **guardar** falla (tema, avisos o primera ejecución), el ViewModel atrapa el error y pone un `mensaje` en el UiState; `AjustesRuta` lo muestra en un **Snackbar** y llama a `onMensajeMostrado()`. DataStore no modifica el archivo cuando una escritura falla, así que el `Flow` sigue emitiendo el valor anterior y la pantalla **mantiene la configuración previa**. La app sigue funcionando y el siguiente intento puede guardar normalmente.
- Un valor de tema desconocido vuelve a `SISTEMA`.

| Falla al guardar | Mensaje |
|---|---|
| Tema | "No pudimos guardar el tema. Se mantiene el anterior." |
| Recordatorios | "No pudimos guardar los recordatorios. Se mantiene la configuración anterior." |
| Primera ejecución | "No pudimos guardar tu avance. Volverás a ver la bienvenida." |

## 4. Conexión con la app

- **Tema:** `MainActivity` crea un único `PreferenciasViewModel` (`by viewModels`) y pasa `darkTheme = tema.usarModoOscuro(isSystemInDarkTheme())` a `TioTecPreventivoTheme`. La barra de estado también sigue el tema elegido.
- **Ajustes:** *Perfil* tiene el botón **Ajustes**, que abre la ruta `ajustes` del `NavHost`. Es una pantalla secundaria: la barra superior dice "Ajustes", muestra "← Volver" y la pestaña *Perfil* sigue marcada. Ajustes usa la misma instancia del ViewModel que el tema, así que el cambio se ve al instante.

## 5. Pruebas

| Prueba | Dónde | Comprueba |
|---|---|---|
| `PreferenciasRepositoryTest` (6) | JVM, sin emulador | Valores por defecto; guardar y leer; cambiar una sin alterar las demás; **cerrar y reabrir el DataStore conserva los valores**; tema desconocido → Sistema; `usarModoOscuro` |
| `PreferenciasViewModelTest` (5) | JVM, sin emulador | Guardado correcto sin mensaje; **si falla guardar tema, avisos o primera ejecución, se mantiene el valor anterior y aparece el mensaje**; el mensaje se limpia y el siguiente guardado funciona |
| `AjustesPersistenciaTest` (1) | Emulador | Perfil → Ajustes → elegir Oscuro → **cerrar la app y volver a abrirla** → Oscuro sigue seleccionado |

```bash
./gradlew :app:testDebugUnitTest            # JVM
./gradlew :app:connectedDebugAndroidTest    # con el emulador encendido
```

`AjustesPersistenciaTest` cierra y reabre la pantalla dentro del mismo proceso. El reinicio completo (archivo leído desde cero) lo cubre `PreferenciasRepositoryTest`. Comprobación manual adicional: elegir Oscuro, cerrar la app desde recientes, abrirla y verificar que sigue oscura.
