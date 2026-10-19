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
| `viewmodel/PreferenciasViewModel.kt` | `uiState: StateFlow<PreferenciasUiState>` con `stateIn` y eventos `onTemaChange`, `onAvisosChange`, `onBienvenidaVista` |
| `ui/screens/AjustesScreen.kt` | `AjustesRuta` observa con `collectAsState()`; `AjustesScreen` sin estado, con previews claro y oscuro |
| `data/AppContainer.kt` | Agrega `preferenciasRepository` |

Flujo: `AjustesScreen` → evento → `PreferenciasViewModel` → `PreferenciasRepository` → DataStore → `Flow` → `StateFlow` → `collectAsState()` → la pantalla se redibuja.

## 3. Sin bloquear la interfaz

- Las lecturas son `Flow` y las escrituras son funciones `suspend` lanzadas en `viewModelScope`; DataStore hace la E/S fuera del hilo principal.
- Mientras llega el primer valor, `PreferenciasUiState.cargando = true` y la pantalla muestra un indicador.
- Si el archivo no se puede leer (`IOException`), se usan los valores por defecto en vez de cerrar la app.
- Un valor de tema desconocido vuelve a `SISTEMA`.

## 4. Pruebas

`app/src/test/java/cl/duoc/tiotecpreventivo/repository/PreferenciasRepositoryTest.kt` corre en la JVM, **sin emulador**:

- valores por defecto sin datos guardados;
- guardar y leer cada preferencia, y que cambiar una no altere las demás;
- **las preferencias sobreviven al cerrar y reabrir** (se detiene el DataStore y se abre otro sobre el mismo archivo);
- tema desconocido → Sistema;
- `Tema.usarModoOscuro` para las tres opciones.

```bash
./gradlew :app:testDebugUnitTest --tests "cl.duoc.tiotecpreventivo.repository.PreferenciasRepositoryTest"
```

## 5. Conexión con la interfaz de TT-05 (después de mergear el PR #6)

TT-08 no modifica `MainActivity`, `Theme.kt` ni `TioTecApp.kt` para no chocar con el PR #6. Al integrarlo:

**Aplicar el tema guardado** en `MainActivity`:

```kotlin
private val preferenciasViewModel: PreferenciasViewModel by viewModels {
    PreferenciasViewModel.Factory(
        (application as TioTecApplication).contenedor.preferenciasRepository
    )
}

// dentro de setContent { ... }
val preferencias by preferenciasViewModel.uiState.collectAsState()
TioTecPreventivoTheme(
    darkTheme = preferencias.tema.usarModoOscuro(isSystemInDarkTheme())
) {
    TioTecApp(windowWidthSizeClass = windowSizeClass.widthSizeClass)
}
```

**Abrir Ajustes** desde Perfil: agregar una ruta `ajustes` en el `NavHost` que muestre `AjustesRuta(viewModel = …)`.

Comprobación manual: cambiar a tema Oscuro, cerrar la app desde recientes, abrirla y verificar que sigue oscura.
