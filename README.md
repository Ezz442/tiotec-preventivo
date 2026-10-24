# TíoTec Preventivo

App Android de **prevención digital** para personas no especialistas. Guía una evaluación sobre hábitos de seguridad de un dispositivo, muestra el **nivel de riesgo por categoría** con recomendaciones priorizadas y ayuda a darles seguimiento con un **checklist, recordatorios e historial**. Todo funciona **sin conexión**, con datos guardados en el teléfono.

Proyecto en dupla para la asignatura **DSY1105 · Desarrollo de Aplicaciones Móviles**, Evaluación Parcial 2, basado en el caso *TíoTec Preventivo*.

> ⚠️ Todos los perfiles, dispositivos y solicitudes son **ficticios**. La app **orienta según las respuestas**: no revisa, no escanea ni accede a la configuración real del teléfono, y no pide datos personales ni sensibles.

- **Tablero Trello:** <https://trello.com/b/TVOC98PJ>
- **Pull requests:** <https://github.com/Ezz442/tiotec-preventivo/pulls?q=is%3Apr>

---

## 1. Problema y usuarios

Muchas personas pierden fotos, documentos o el acceso a sus cuentas porque no tienen copias de seguridad, no actualizan sus equipos o no saben cómo recuperar una cuenta. La información técnica disponible suele ser difícil de entender.

**Usuario:** una persona sin formación técnica que quiere saber qué tan protegido está un dispositivo y qué hacer primero. Usa un **alias ficticio** y registra **dispositivos de prueba**.

**Qué resuelve la app:** un cuestionario breve en cuatro áreas (**respaldos, actualizaciones, autenticación y recuperación de cuentas**), un resultado explicado en lenguaje simple y un plan de acciones con fecha de revisión.

## 2. Integrantes

| Integrante | GitHub | Tareas principales |
|---|---|---|
| Enzo Morales | [@Ezz442](https://github.com/Ezz442) | TT-02 repositorio · TT-04 modelo local · TT-06 catálogo · TT-07 Room · TT-08 DataStore · TT-10 reglas de riesgo · TT-12 resultados · TT-14 historial · TT-16 recordatorios · TT-18 cámara · TT-21 verificación · TT-22 README |
| Gerard Zambrano | [@gjjzc](https://github.com/gjjzc) | TT-03 diseño y navegación · TT-05 base Compose/MVVM · TT-09 perfil y dispositivos · TT-11 evaluación guiada · TT-13 checklist · TT-15 contenidos · TT-17 solicitudes · TT-19 galería · TT-27 diseño |

Cada tarea se desarrolló en su rama y la revisó el otro integrante antes de integrarla (ver sección 7).

## 3. Funciones implementadas

| Función | Qué hace | Pantalla | Tarea · PR |
|---|---|---|---|
| Perfil ficticio | Alias (2 a 30 caracteres) y nivel de experiencia, con errores por campo | Perfil | TT-09 · [#10](https://github.com/Ezz442/tiotec-preventivo/pull/10) |
| Preferencias | Tema claro/oscuro/sistema y avisos, guardados con DataStore | Perfil → Ajustes | TT-08 · [#9](https://github.com/Ezz442/tiotec-preventivo/pull/9) |
| Dispositivos | Crear, editar y eliminar con confirmación; nombre no repetido; archivado si tiene evaluaciones | Inicio → Mis dispositivos | TT-09 · [#10](https://github.com/Ezz442/tiotec-preventivo/pull/10) |
| Foto con **cámara** | Activity Result API (`TakePicture`) + FileProvider; reemplazar y quitar | Tarjeta del dispositivo | TT-18 · [#19](https://github.com/Ezz442/tiotec-preventivo/pull/19) |
| Imagen de **galería** | Selector de fotos (`PickVisualMedia`) con copia local de la imagen | Tarjeta del dispositivo | TT-19 · [#20](https://github.com/Ezz442/tiotec-preventivo/pull/20) |
| Evaluación guiada | 12 preguntas, progreso, validación por pregunta, revisión antes de finalizar, confirmación de salida | Evaluar dispositivo | TT-11 · [#12](https://github.com/Ezz442/tiotec-preventivo/pull/12) |
| Reglas de riesgo | Puntaje por categoría, prácticas clave, nivel bajo/medio/alto y nivel global | — (lógica) | TT-10 · [#11](https://github.com/Ezz442/tiotec-preventivo/pull/11) |
| Resultados | Nivel con **ícono + texto + color**, explicación, recomendaciones priorizadas y la respuesta que las activó | Resultados | TT-12 · [#13](https://github.com/Ezz442/tiotec-preventivo/pull/13) |
| Checklist de acciones | Pendientes/Completadas, cambio de estado persistente, contador animado | Mis acciones | TT-13 · [#14](https://github.com/Ezz442/tiotec-preventivo/pull/14) |
| Historial | Evaluaciones por fecha y dispositivo, filtros, detalle con las respuestas guardadas | Inicio → Historial | TT-14 · [#15](https://github.com/Ezz442/tiotec-preventivo/pull/15) |
| Contenidos educativos | Filtros por categoría y lectura; también se abren desde las recomendaciones | Aprende | TT-15 · [#16](https://github.com/Ezz442/tiotec-preventivo/pull/16) |
| Recordatorios | Fecha y hora validadas, **notificación local** con WorkManager, permiso en Android 13+, abre Mis acciones al tocar | Mis acciones | TT-16 · [#17](https://github.com/Ezz442/tiotec-preventivo/pull/17) |
| Asistencia simulada | Solicitudes con categoría obligatoria, estados con confirmación e historial | Perfil → Solicitudes | TT-17 · [#18](https://github.com/Ezz442/tiotec-preventivo/pull/18) |
| Sin conexión | El catálogo vive en el código y el manifiesto **no** pide permiso de internet | Toda la app | TT-06 · [#7](https://github.com/Ezz442/tiotec-preventivo/pull/7) |

### Fuera del alcance o pendiente

Para no declarar como implementado algo que es solo diseño:

- **Retirado del alcance EP2:** API REST/Retrofit, sincronización remota y paneles de administrador/supervisor. Se reemplazaron por Room, DataStore y los recursos nativos cámara y galería (ver `docs/TT-04-modelo-local.md`).
- **Diseñado en TT-03 pero no implementado así:** pantalla de bienvenida del primer uso; "última evaluación" y botón *Evaluar dispositivo* directo en Inicio (se evalúa desde Mis dispositivos); pantalla separada de recordatorios y recordatorio de *próxima evaluación* (los recordatorios se programan desde cada acción). El detalle está en la sección final de `docs/TT-03-diseno-navegacion.md`.
- **En curso:** TT-25 animaciones (hoy hay dos: contador de Mis acciones y respuestas del detalle), TT-26 formularios centralizados, TT-27 ajustes de diseño, TT-20 y TT-23 pruebas del recorrido completo.

## 4. Tecnologías y arquitectura

| Tema | Uso en el proyecto |
|---|---|
| Lenguaje | Kotlin 2.2.10 |
| UI | Jetpack Compose (BOM 2026.02.01) y **Material Design 3**, con esquema de color claro y oscuro |
| Navegación | Navigation Compose 2.9.7, rutas con argumentos y grafo anidado |
| Adaptación | `WindowSizeClass`: `NavigationBar` en Compact y `PermanentNavigationDrawer` en Medium/Expanded |
| Arquitectura | **MVVM**: Composables → ViewModel (`StateFlow` + `UiState`) → Repositorio → Room / DataStore / archivos |
| Estado | `MutableStateFlow` privado, `StateFlow` público y `collectAsState()` en la pantalla |
| Persistencia | **Room 2.8.5** (SQLite, transacciones, esquema exportado en `app/schemas`) y **DataStore Preferences 1.2.1** |
| Recursos nativos | Cámara (`TakePicture` + FileProvider), galería (`PickVisualMedia`), notificaciones (WorkManager 2.10.1 + `POST_NOTIFICATIONS`) |
| Pruebas | JUnit 4, kotlinx-coroutines-test y AndroidX Test (Room en emulador) |

```
app/src/main/java/cl/duoc/tiotecpreventivo/
├── data/            catálogo local, entidades y DAOs de Room, DataStore, imágenes, AppContainer
├── domain/          reglas puras sin Android: riesgo, resultados, historial, validación de recordatorios
├── repository/      acceso a datos que usan los ViewModels
├── viewmodel/       un ViewModel + UiState + Factory por pantalla
├── notificaciones/  canal, aviso y WorkManager
└── ui/              navegación, pantallas, componentes y tema
```

Ningún Composable accede a Room directamente: los ViewModels reciben los repositorios desde `AppContainer`, que se crea en `TioTecApplication`.

## 5. Requisitos y ejecución local

**Requisitos**

- Android Studio reciente con soporte para Android Gradle Plugin 9.4 (incluye el JDK necesario).
- Android SDK con la plataforma de `targetSdk` 37 (Android Studio la ofrece al sincronizar).
- Emulador o teléfono con **Android 7.0 (API 24)** o superior. Para probar el permiso de notificaciones, usar **Android 13 (API 33)** o superior.
- No se necesita internet para usar la app, solo para la primera sincronización de Gradle.

**Ejecutar**

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/Ezz442/tiotec-preventivo.git
   ```
2. Abrir la carpeta en Android Studio y esperar la sincronización de Gradle.
3. Ejecutar la configuración **app** en el emulador o teléfono.
4. Primer uso sugerido: Perfil → crear perfil → Inicio → Mis dispositivos → agregar un dispositivo → *Evaluar dispositivo*.

**Pruebas**

```powershell
# Pruebas en la JVM (sin emulador)
.\gradlew.bat :app:testDebugUnitTest
# Pruebas de Room y DataStore en el emulador (encendido)
.\gradlew.bat :app:connectedDebugAndroidTest
# Compilar y probar desde una copia limpia del repositorio
powershell -ExecutionPolicy Bypass -File scripts\verificar-copia-limpia.ps1 [-ConEmulador]
```

Resultado registrado en TT-21: **164 pruebas JVM, 0 fallos** desde copia limpia, y 27 pruebas en emulador (ver `docs/TT-21-verificacion.md`).

## 6. Relación con los indicadores de la rúbrica EP2

| Indicador | Qué se demuestra | Dónde verlo en la app | Pruebas | Evidencia |
|---|---|---|---|---|
| **IE 2.1.1** · Diseño visual y navegación | Material 3, tema claro/oscuro, riesgo con ícono + texto, NavigationBar o Drawer según el tamaño | Inicio, Resultados, Ajustes; rotar o usar tablet | Previews de cada pantalla | `docs/TT-03-diseno-navegacion.md`, `docs/TT-03-bocetos.html`, PR #3, #5, #6 |
| **IE 2.1.2** · Formularios validados | Errores por campo con ícono y mensaje, validación en el ViewModel, confirmaciones | Perfil, Dispositivos, Evaluación, Recordatorios, Solicitudes | `PerfilViewModelTest`, `DispositivosViewModelTest`, `EvaluacionViewModelTest`, `RecordatorioViewModelTest`, `ValidadorRecordatorioTest`, `SolicitudesViewModelTest` | PR #10, #12, #17, #18 |
| **IE 2.2.1** · Lógica y gestión de estado | ViewModel + StateFlow, reglas de riesgo puras, transiciones de acciones y solicitudes | Evaluación → Resultados → Mis acciones | `ReglasRiesgoTest`, `ResultadoEvaluacionMapperTest`, `AccionesViewModelTest`, `HistorialViewModelTest` | `docs/TT-10-reglas-riesgo.md`, `docs/TT-12-resultados-recomendaciones.md`, PR #11, #13, #14 |
| **IE 2.2.2** · Animaciones y retroalimentación | ⚠️ **Parcial:** contador animado (`AnimatedContent`), respuestas con `AnimatedVisibility`, Snackbars y carga solo durante operaciones reales | Mis acciones, detalle de resultado | — | PR #14, #15; se amplía en TT-25 |
| **IE 2.3.1** · Almacenamiento local | Room con transacciones, archivado, historial sin sobrescribir, DataStore e imágenes en almacenamiento privado | Historial, Ajustes, fotos tras cerrar y abrir | `RepositoriosRoomTest`, `HistorialRoomTest`, `ResultadoRoomTest`, `ImagenesReaperturaTest`, `PreferenciasRepositoryTest` | `docs/TT-07-room-persistencia.md`, `docs/TT-08-preferencias-datastore.md`, `docs/TT-14-historial.md`, `docs/TT-21-verificacion.md` |
| **IE 2.3.2** · Repositorio y documentación | Ramas por tarea, PR con revisión cruzada, Trello, README y documentos por tarea | GitHub y Trello | Script de copia limpia | Este README, sección 7 |
| **IE 2.4.1** · Recursos nativos | **Cámara**, **galería** y **notificaciones locales**, con cancelación, denegación y "no disponible" sin bloquear | Tarjeta del dispositivo, Mis acciones | `DispositivosFotoViewModelTest`, `DispositivosGaleriaViewModelTest`, `AlmacenImagenesLocalTest`, `GestorRecordatoriosTest` | `docs/TT-18-camara.md`, `docs/TT-16-recordatorios.md`, PR #17, #19, #20 |

Las capturas y videos de cada tarea están adjuntos en su tarjeta de Trello y en la conversación de su PR.

## 7. Trazabilidad: PR, commits y defectos

### Pull requests

Cada PR lo revisó el integrante que no era autor antes del merge.

| PR | Tarea | Autor |
|---|---|---|
| [#1](https://github.com/Ezz442/tiotec-preventivo/pull/1) | TT-02 README inicial | Enzo |
| [#2](https://github.com/Ezz442/tiotec-preventivo/pull/2) | TT-02 acceso de Gerard al repositorio | Gerard |
| [#3](https://github.com/Ezz442/tiotec-preventivo/pull/3) | TT-03 diseño de pantallas y bocetos | Gerard |
| [#4](https://github.com/Ezz442/tiotec-preventivo/pull/4) | TT-04 modelo local | Enzo |
| [#5](https://github.com/Ezz442/tiotec-preventivo/pull/5) | TT-27 bocetos y navegación al alcance local | Gerard |
| [#6](https://github.com/Ezz442/tiotec-preventivo/pull/6) | TT-05 base Compose, Material 3 y MVVM | Gerard |
| [#7](https://github.com/Ezz442/tiotec-preventivo/pull/7) | TT-06 catálogo local y datos ficticios | Enzo |
| [#8](https://github.com/Ezz442/tiotec-preventivo/pull/8) | TT-07 CRUD y persistencia con Room | Enzo |
| [#9](https://github.com/Ezz442/tiotec-preventivo/pull/9) | TT-08 preferencias con DataStore | Enzo |
| [#10](https://github.com/Ezz442/tiotec-preventivo/pull/10) | TT-09 perfil y dispositivos | Gerard |
| [#11](https://github.com/Ezz442/tiotec-preventivo/pull/11) | TT-10 cuestionario y reglas de riesgo | Enzo |
| [#12](https://github.com/Ezz442/tiotec-preventivo/pull/12) | TT-11 evaluación guiada | Gerard |
| [#13](https://github.com/Ezz442/tiotec-preventivo/pull/13) | TT-12 resultados y recomendaciones | Enzo |
| [#14](https://github.com/Ezz442/tiotec-preventivo/pull/14) | TT-13 checklist de acciones | Gerard |
| [#15](https://github.com/Ezz442/tiotec-preventivo/pull/15) | TT-14 historial | Enzo |
| [#16](https://github.com/Ezz442/tiotec-preventivo/pull/16) | TT-15 contenidos educativos (+ pruebas de Enzo en la revisión) | Gerard |
| [#17](https://github.com/Ezz442/tiotec-preventivo/pull/17) | TT-16 recordatorios y notificaciones | Enzo |
| [#18](https://github.com/Ezz442/tiotec-preventivo/pull/18) | TT-17 solicitudes simuladas | Gerard |
| [#19](https://github.com/Ezz442/tiotec-preventivo/pull/19) | TT-18 cámara | Enzo |
| [#20](https://github.com/Ezz442/tiotec-preventivo/pull/20) | TT-19 galería | Gerard |
| [#21](https://github.com/Ezz442/tiotec-preventivo/pull/21) | TT-21 verificación | Enzo |

### Defectos encontrados en revisión y resueltos

No se usaron *issues* de GitHub: los defectos se registraron en la revisión del PR y en la tarjeta de Trello.

| Tarea | Defecto | Lo encontró | Corrección |
|---|---|---|---|
| TT-09 | Textos y botones superpuestos en carga/error/sin perfil; espacio de más bajo la barra | Enzo (revisión de código) | PR #10, commit "corregir superposicion y espacio superior en dispositivos" |
| TT-11 | Conflicto de merge por ramas mezcladas en distinto orden | GitHub | PR #12, merge de `main` en la rama |
| TT-14 | El filtro del historial mostraba el nombre antiguo al renombrar sin reevaluar | Gerard (prueba manual) | PR #15, commit "mostrar en el filtro el nombre actual del dispositivo" + prueba de regresión |
| TT-21 | El script de copia limpia fallaba en PowerShell 5.1 (tildes) y sin `local.properties` | Enzo (ejecución) | PR #21 |

### Commits técnicos de ambos

Historial completo en [commits](https://github.com/Ezz442/tiotec-preventivo/commits/main), o filtrado por autor: [Enzo](https://github.com/Ezz442/tiotec-preventivo/commits/main?author=Ezz442) · [Gerard](https://github.com/Ezz442/tiotec-preventivo/commits/main?author=gjjzc).

- **Enzo:** "TT-07: agregar DAO y base de datos con transacciones y regla de archivado" · "TT-10: reglas de riesgo por categoría y recomendaciones priorizadas" · "TT-16: mostrar la notificación local a la hora del recordatorio" · "TT-18: tomar la foto con TakePicture y mostrarla en la tarjeta".
- **Gerard:** "TT-09: implementar CRUD de dispositivos con validaciones y confirmacion" · "TT-11: implementar evaluacion guiada con revision y guardado en Room" · "TT-13: implementar checklist persistente y contador real en inicio" · "TT-19: seleccionar imágenes de galería para dispositivos".

## 8. Documentación por tarea

| Documento | Contenido |
|---|---|
| [`docs/TT-03-diseno-navegacion.md`](docs/TT-03-diseno-navegacion.md) · [`TT-03-bocetos.html`](docs/TT-03-bocetos.html) | Pantallas, navegación, estados, criterios visuales y estado de implementación |
| [`docs/TT-04-modelo-local.md`](docs/TT-04-modelo-local.md) | Modelo de datos local y cambios desde la versión con API |
| [`docs/TT-06-datos-ficticios.md`](docs/TT-06-datos-ficticios.md) | Catálogo de preguntas, recomendaciones y contenidos |
| [`docs/TT-07-room-persistencia.md`](docs/TT-07-room-persistencia.md) | Room, transacciones y archivado |
| [`docs/TT-08-preferencias-datastore.md`](docs/TT-08-preferencias-datastore.md) | Preferencias con DataStore |
| [`docs/TT-10-reglas-riesgo.md`](docs/TT-10-reglas-riesgo.md) | Puntajes, umbrales y prácticas clave |
| [`docs/TT-12-resultados-recomendaciones.md`](docs/TT-12-resultados-recomendaciones.md) | Pantalla de resultados |
| [`docs/TT-14-historial.md`](docs/TT-14-historial.md) | Historial sin sobrescribir |
| [`docs/TT-16-recordatorios.md`](docs/TT-16-recordatorios.md) | Recordatorios, permisos y WorkManager |
| [`docs/TT-18-camara.md`](docs/TT-18-camara.md) | Cámara, FileProvider y almacenamiento privado |
| [`docs/TT-21-verificacion.md`](docs/TT-21-verificacion.md) | Matriz de pruebas y resultados |

## 9. Flujo de trabajo

- `main` contiene siempre una versión que compila.
- Cada tarea se trabaja en una rama `feat/TT-XX-descripcion` o `fix/TT-XX-descripcion`.
- Los cambios se integran mediante Pull Request revisado por el otro integrante.
- Las tareas, criterios y evidencias se gestionan en Trello con el identificador TT-XX.
- No se suben `local.properties`, claves ni datos reales (`.gitignore`).

## 10. Estado

Funcionalidad principal implementada e integrada en `main` (TT-01 a TT-19). En curso: TT-20 a TT-27 (pruebas del recorrido, animaciones, formularios centralizados, ajustes de diseño y entrega).
