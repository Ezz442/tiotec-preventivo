# TT-07 · CRUD y persistencia local con Room

**Responsable:** Enzo Morales · **Revisión:** Gerard Zambrano
**Alcance:** Evaluación Parcial 2 · base SQLite local mediante Room, sin red.
**Base:** secciones 4, 5 y 9 de `docs/TT-04-modelo-local.md`. Usa el catálogo de TT-06.

## 1. Dependencias

| Librería | Versión | Para qué |
|---|---|---|
| `androidx.room:room-runtime` / `room-ktx` | 2.8.5 | Base de datos, `Flow` y funciones `suspend` |
| `androidx.room:room-compiler` (KSP) | 2.8.5 | Genera el código de los DAO |
| Plugin `com.google.devtools.ksp` | 2.3.11 | Procesador compatible con el Kotlin integrado de AGP 9 |
| Plugin `androidx.room` | 2.8.5 | Exporta el esquema a `app/schemas` |
| `androidx.room:room-testing` (androidTest) | 2.8.5 | Pruebas de base de datos y migraciones |

## 2. Estructura

```
cl.duoc.tiotecpreventivo
├── TioTecApplication.kt          // crea el AppContainer al abrir la app
├── data
│   ├── AppContainer.kt           // una sola base y un repositorio de cada tipo
│   └── local
│       ├── TioTecDatabase.kt     // @Database versión 1, 9 entidades
│       ├── entity                // tablas de TT-04, sección 4
│       └── dao                   // Perfil, Dispositivo, Evaluacion, Accion, Recordatorio, Solicitud
└── repository                    // un repositorio por grupo; lo único que usan los ViewModels
```

Ningún Composable accede a Room: pantalla → ViewModel → repositorio → DAO.
Para obtener un repositorio en una Factory de ViewModel:

```kotlin
val contenedor = (context.applicationContext as TioTecApplication).contenedor
DispositivosViewModel(contenedor.dispositivoRepository)
```

## 3. CRUD de dispositivos y regla del historial

| Operación | Método |
|---|---|
| Crear | `DispositivoRepository.crear(perfilId, nombre, sistemaOperativo)` |
| Leer | `observarActivos()` (excluye archivados) · `obtener(id)` · `observar(id)` |
| Actualizar | `actualizar(id, nombre, sistemaOperativo)` · `actualizarImagen(id, ruta)` |
| Eliminar | `eliminar(id)` → `Eliminado(imagenRuta)`, `Archivado` o `NoEncontrado` |
| Validar nombre | `existeNombreActivo(nombre, excluirId)` (ignora mayúsculas y espacios) |

`eliminar` aplica en **una sola transacción** la regla de TT-04 (4.2):

- **Sin evaluaciones:** se borra el registro. Devuelve la ruta de la imagen para que TT-18/19 borre el archivo.
- **Con evaluaciones:** se marca `archivado`, desaparece de *Mis dispositivos* y el **historial se conserva** (evaluaciones, respuestas, resultados y acciones) con la copia del nombre y sistema operativo de cada evaluación.
- Al archivar se cancelan los recordatorios **programados de ambos tipos**: los de *próxima evaluación* del dispositivo y los de las *acciones* de sus evaluaciones (recordatorio → acción → evaluación → dispositivo). Los ya enviados y los de otros dispositivos no se tocan.

La confirmación con `AlertDialog` la muestra la pantalla antes de llamar a `eliminar` (TT-09).

## 4. Qué se persiste

| Dato | Repositorio | Notas |
|---|---|---|
| Perfil | `PerfilRepository` | Uno solo: `guardar` crea o actualiza |
| Evaluaciones, respuestas y resultados | `EvaluacionRepository` | `guardarEvaluacion` guarda todo en una transacción; recibe los resultados ya calculados (TT-10/12) |
| Acciones | `EvaluacionRepository` (crea) · `AccionRepository` | Se generan con `recomendacionesPara` del catálogo; orden ALTA → MEDIA → BAJA con `CASE` |
| Recordatorios | `RecordatorioRepository` | Fecha en el futuro y título obligatorio; el aviso del sistema es TT-16 |
| Solicitudes | `SolicitudRepository` | Código `DEMO-001`, `DEMO-002`… y registro de historial en cada cambio de estado |

## 5. Cambios del esquema

- `exportSchema = true`: al compilar, Room escribe `app/schemas/cl.duoc.tiotecpreventivo.data.local.TioTecDatabase/1.json`. **Ese archivo se sube al repositorio.**
- Si una entidad cambia: subir `version`, escribir la migración en `TioTecDatabase.MIGRACIONES` y subir el nuevo JSON.
- **No** se usa `fallbackToDestructiveMigration()`, porque borraría perfiles, evaluaciones y acciones al actualizar la app.
- Los enums se guardan como texto con su nombre: renombrar un valor requiere migración.

## 6. Pruebas (instrumentadas, requieren emulador)

| Archivo | Comprueba |
|---|---|
| `RepositoriosRoomTest` | Perfil único; CRUD de dispositivos; nombre repetido; borrar vs. archivar con historial intacto; **al archivar se cancelan los recordatorios de ambos tipos** (acción y próxima evaluación) sin tocar los enviados ni los de otro dispositivo; evaluación completa (respuestas, resultados, acciones, nivel global); rechazo de respuestas incompletas; **reversión de la transacción** si falla; historial más reciente primero; orden de acciones y `completadaEn`; recordatorios (crear, modificar, cancelar, fecha pasada); solicitudes `DEMO-00X` con historial |
| `PersistenciaReaperturaTest` | Datos guardados en un archivo SQLite siguen ahí al **cerrar y reabrir** la base |

Ejecutar con el emulador encendido:

```bash
./gradlew :app:connectedDebugAndroidTest
```

o clic derecho sobre la carpeta `androidTest` → **Run 'All Tests'**.

Comprobación manual (cuando existan las pantallas): crear datos, cerrar la app desde recientes, volver a abrirla y verificar que siguen.
