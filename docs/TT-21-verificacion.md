# TT-21 · Verificación de ViewModels, reglas y persistencia local

**Responsable:** Enzo Morales · **Revisión:** Gerard Zambrano
**Alcance:** Evaluación Parcial 2 · datos locales, sin servidor ni API.
**Rúbrica:** IE 2.2.1 / IE 2.3.1.

> TT-21 verifica la **lógica y los datos** (ViewModels, reglas, Room, DataStore, archivos). El recorrido visual completo, cámara/galería reales, notificaciones, teclado, texto grande y tamaños de pantalla se prueban a mano en **TT-20** (Gerard).

## 1. Criterios → pruebas

### Criterio 1 · Reglas de riesgo, validaciones por campo y transiciones (válidos e inválidos)

| Qué | Prueba | Casos |
|---|---|---|
| Reglas de riesgo (TT-10) | `ReglasRiesgoTest` | todo Sí/No/No lo sé, práctica clave, mixto, umbrales 33/34 y 66/67, incompletas, IDs desconocidos |
| Resultado y recomendaciones (TT-12) | `ResultadoEvaluacionMapperTest` · `ResultadoViewModelTest` | orden por prioridad, respuesta que activa cada una, niveles guardados, inexistente, error |
| Perfil (TT-09) | `PerfilViewModelTest` **(nuevo)** | vacíos, 1 y 31 caracteres, 30 sí, error se borra al escribir, trim, un solo perfil, error al guardar |
| Dispositivos (TT-09) | `DispositivosViewModelTest` **(nuevo)** | sin perfil, vacíos, fuera de rango, repetido sin mayúsculas, crear, editar con su nombre, cancelar, eliminar con confirmación, archivar, error |
| Evaluación (TT-11) | `EvaluacionViewModelTest` **(nuevo)** | 12 preguntas, archivado/inexistente, siguiente sin responder, conservar al retroceder, revisar con pendientes, guardar completa, doble envío, error y reintento, confirmar salida |
| Recordatorios (TT-16) | `ValidadorRecordatorioTest` · `RecordatorioViewModelTest` · `GestorRecordatoriosTest` | fecha/hora vacías, pasado, > 1 año, rango; crear/reprogramar/cancelar |
| **Transiciones de acciones** (TT-13) | `AccionesViewModelTest` **(nuevo)** | orden ALTA→MEDIA→BAJA, pendiente → completada → pendiente, `completadaEn`, contador, filtro, inexistente, error |
| **Transiciones de solicitudes** (TT-17) | `SolicitudesViewModelTest` **(nuevo)** | sin perfil, categoría obligatoria, DEMO-001 Pendiente, cerrar sin crear, confirmar/cancelar cambio, historial por cambio, mismo estado, inexistente, error |

### Criterio 2 · Room, DataStore, StateFlow, histórico e imágenes tras reinicio

| Qué | Prueba | Dónde |
|---|---|---|
| CRUD Room y transacciones | `RepositoriosRoomTest` (13) | emulador |
| Reapertura de la base | `PersistenciaReaperturaTest` | emulador |
| Resultados sin duplicados | `ResultadoRoomTest` (3) | emulador |
| Histórico (varias evaluaciones, editar, archivar, reabrir) | `HistorialRoomTest` (6) · `HistorialLogicaTest` · `HistorialViewModelTest` | emulador + JVM |
| **Imágenes tras reinicio** | `ImagenesReaperturaTest` **(nuevo, 2)** · `AlmacenImagenesLocalTest` | emulador + JVM |
| Cámara y galería en el ViewModel | `DispositivosFotoViewModelTest` · `DispositivosGaleriaViewModelTest` **(nuevo)** | JVM |
| DataStore (tema, avisos, reapertura) | `PreferenciasRepositoryTest` · `PreferenciasViewModelTest` · `AjustesPersistenciaTest` | JVM + emulador |
| StateFlow | todos los `*ViewModelTest` leen `uiState.value` después de cada evento | JVM |
| Contador real de Inicio | `InicioRepositoryLocalTest` **(nuevo)** | JVM |
| Sin servidor/API | `CatalogoLocalTest.manifiestoNoPideAccesoAInternet` | JVM |

Las pruebas JVM usan DAOs en memoria (`test/.../repository/FalsosDatos.kt`) que imitan el orden y los filtros de las consultas reales. `ConFalla.falla = true` simula un error de base de datos para probar mensajes y reintentos.

### Criterio 3 · Compilación y pruebas desde copia limpia

```powershell
powershell -ExecutionPolicy Bypass -File scripts\verificar-copia-limpia.ps1
# con el emulador encendido:
powershell -ExecutionPolicy Bypass -File scripts\verificar-copia-limpia.ps1 -ConEmulador
```

El script clona `main` en una carpeta temporal nueva (sin archivos de Android Studio ni compilaciones previas) y ejecuta `clean assembleDebug`, `testDebugUnitTest` y, con `-ConEmulador`, `connectedDebugAndroidTest`. Guarda el registro en `verificacion-TT21.txt` y abre los reportes HTML.

## 2. Resultados

| Ejecución | Dónde | Resultado |
|---|---|---|
| Pruebas JVM sin DataStore (152 pruebas, 19 clases) | Entorno de verificación del PR (Kotlin 2.2.10, JUnit 4.13.2) | ✅ 152/152 |
| `assembleDebug` desde copia limpia (`feat/TT-21-verificacion`) | PC de Enzo · 23-10-2026 | ✅ BUILD SUCCESSFUL (1 min 26 s) |
| `testDebugUnitTest` completo desde copia limpia | PC de Enzo · 23-10-2026 | ✅ **164/164** (0 fallos, 0 omitidas, 2,8 s) |
| `connectedDebugAndroidTest` (27 pruebas en 7 clases) | Emulador de Gerard | ✅ 27/27, 0 fallos y 0 omitidas; ejecución local tras corregir la expectativa de puntajes|

Adjuntar en Trello: captura del reporte `app/build/reports/tests/testDebugUnitTest/index.html` y de `androidTests/connected/debug/index.html`, más el archivo `verificacion-TT21.txt`.

## 3. Defectos encontrados y resueltos (revisión cruzada)

| Tarea | Defecto | Cómo se encontró | Solución |
|---|---|---|---|
| TT-09 | Textos y botones superpuestos en estados de carga/error/sin perfil; espacio de más bajo la barra | Revisión de código (Enzo) | `Column` en cada `item` y `contentWindowInsets` (a0618ec) |
| TT-11 | Conflicto en el PR por ramas mezcladas en otro orden | GitHub | Merge de `main` en la rama (Gerard) |
| TT-14 | El filtro del historial mostraba el nombre antiguo tras renombrar sin reevaluar | Prueba manual (Gerard) | `nombresActuales` + prueba de regresión en JVM y Room (21955cc) |

La prueba en emulador detectó una expectativa desactualizada: esperaba 2 puntos para todas las respuestas «No», pero las preguntas clave valen 3. Se corrigió RepositoriosRoomTest para comparar el puntaje y el texto con el catálogo. Después de la corrección, las 27 pruebas de Android pasaron.

### Observaciones abiertas (no bloquean)

- Solicitudes: *Resuelta* y *Cancelada* pueden volver a *Pendiente* (la simulación lo permite). Propuesta: estados finales en TT-26.
- Cámara: no se corrige la rotación EXIF de las fotos; en el emulador no se nota.
- WorkManager puede atrasar el aviso unos minutos con ahorro de batería (comportamiento del sistema, documentado en TT-16).

## 4. Inventario de pruebas

**JVM (`app/src/test`, sin emulador):** CatalogoLocalTest · AlmacenImagenesLocalTest · InicioRepositoryLocalTest · HistorialLogicaTest · ReglasRiesgoTest · ResultadoEvaluacionMapperTest · ValidadorRecordatorioTest · GestorRecordatoriosTest · PreferenciasRepositoryTest · AccionesViewModelTest · AprendeViewModelTest · DispositivosFotoViewModelTest · DispositivosGaleriaViewModelTest · DispositivosViewModelTest · EvaluacionViewModelTest · HistorialViewModelTest · PerfilViewModelTest · PreferenciasViewModelTest · RecordatorioViewModelTest · ResultadoViewModelTest · SolicitudesViewModelTest.

**Emulador (`app/src/androidTest`):** RepositoriosRoomTest · PersistenciaReaperturaTest · ResultadoRoomTest · HistorialRoomTest · ImagenesReaperturaTest · AjustesPersistenciaTest.

Ejecutar una sola clase: clic derecho sobre el archivo en Android Studio → **Run**.
