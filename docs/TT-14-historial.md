# TT-14 · Historial preventivo local

**Responsable:** Enzo Morales · **Revisión:** Gerard Zambrano
**Alcance:** Evaluación Parcial 2 · datos locales, sin servidor ni red.
**Dependencias:** TT-07 (Room), TT-11 (evaluación), TT-12 (resultados), TT-13 (acciones).
**Rúbrica:** IE 2.1.1 / IE 2.3.1.

## 1. Flujo

1. Inicio → **Historial de evaluaciones** (`historial`).
2. Lista de evaluaciones, la más reciente primero, con dispositivo, sistema operativo, fecha y nivel (ícono + texto).
3. Si hay evaluaciones de más de un dispositivo, aparecen filtros: **Todos** y uno por dispositivo, con su cantidad.
4. **Ver detalle** abre `resultado/{evaluacionId}` (pantalla de TT-12). **Volver** regresa al historial.
5. En el detalle, **Ver las 12 respuestas** muestra las respuestas guardadas, agrupadas por categoría (con animación `AnimatedVisibility`).
6. Sin evaluaciones: mensaje y botón **Ir a Mis dispositivos**.

## 2. Datos históricos sin sobrescribir

Cada evaluación guarda **copias** en Room (TT-04/TT-07), y el historial muestra esas copias:

| Dato | Dónde se copia | Qué pasa si cambia después |
|---|---|---|
| Nombre y sistema operativo del dispositivo | `evaluacion.nombreDispositivo`, `sistemaOperativoDispositivo` | Editar el dispositivo no cambia evaluaciones anteriores; solo las nuevas usan el dato nuevo. |
| Texto de la pregunta | `respuesta.textoPregunta` | Si el catálogo cambia en otra versión, la evaluación antigua muestra el texto que se respondió. |
| Nivel y explicación | `evaluacion.nivelGlobal`, `resultado_categoria` | La pantalla usa los niveles guardados; no los recalcula con reglas nuevas. |
| Dispositivo eliminado con evaluaciones | Se **archiva** (TT-07) | Sale de Mis dispositivos, pero sus evaluaciones siguen en el historial. |

El filtro muestra el nombre **más reciente** del dispositivo; cada tarjeta, el nombre que tenía al evaluarse.

## 3. Código

| Archivo | Rol |
|---|---|
| `domain/Historial.kt` | `HistorialLogica`: ordenar, filtros por dispositivo y filtrar. Sin Android. |
| `viewmodel/HistorialViewModel.kt` | Observa `observarHistorial()` de Room (una evaluación nueva aparece sola); filtro por dispositivo; error con reintento. Solo lectura. |
| `ui/screens/HistorialScreen.kt` | Lista, filtros, vacío, carga y error. Previews de los tres estados. |
| `domain/ResultadoEvaluacion.kt` | Se agrega `respuestas` (en orden del cuestionario, con el texto guardado). |
| `ui/screens/ResultadoScreen.kt` | Sección *Tus respuestas*. |
| `ui/screens/InicioScreen.kt` · `ui/navigation/TioTecApp.kt` | Botón y ruta `historial`. |

## 4. Pruebas

| Prueba | Dónde | Qué comprueba |
|---|---|---|
| `HistorialLogicaTest` (5) | JVM | Orden por fecha, filtros con nombre reciente y cantidad, filtrar, nombre antiguo conservado, vacío. |
| `HistorialViewModelTest` (6) | JVM | Vacío, evaluación nueva aparece sola, filtro y "Todos", dispositivo inexistente, filtro que desaparece, error con reintento. |
| `ResultadoEvaluacionMapperTest` (+1) | JVM | Respuestas en orden del cuestionario con el texto guardado. |
| `HistorialRoomTest` (5) | Emulador | Vacío; varias evaluaciones por fecha y dispositivo; editar dispositivo no sobrescribe; eliminar archiva y conserva; historial igual al cerrar y reabrir la base. |

```powershell
.\gradlew.bat :app:testDebugUnitTest
```
`HistorialRoomTest`: con el emulador encendido, clic derecho sobre el archivo → **Run 'HistorialRoomTest'**.

### Prueba manual

1. Sin evaluaciones: Inicio → Historial → mensaje vacío y botón a Mis dispositivos.
2. Evaluar dos dispositivos (uno dos veces) → Historial muestra 3, la más reciente arriba, y filtros por dispositivo.
3. Editar el nombre de un dispositivo y volver al Historial: las evaluaciones antiguas mantienen el nombre anterior.
4. **Ver detalle** → resultados y *Ver las 12 respuestas*. **Volver** regresa al historial.
5. Eliminar un dispositivo con evaluaciones (se archiva) → su historial sigue visible.
6. Cerrar la app desde Recientes y abrirla: el historial sigue igual.
