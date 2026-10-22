# TT-12 · Resultados y recomendaciones locales

**Responsable:** Enzo Morales · **Revisión:** Gerard Zambrano
**Alcance:** Evaluación Parcial 2 · datos locales, sin servidor ni red.
**Dependencias:** TT-07 (Room), TT-10 (reglas), TT-11 (evaluación guiada).
**Rúbrica:** IE 2.2.1.

> Los resultados son una **orientación según las respuestas**. La app no revisa ni escanea el dispositivo; la pantalla lo dice siempre.

## 1. Flujo

1. Mis dispositivos → **Evaluar dispositivo** → responder → **Finalizar evaluación** (TT-11).
2. `EvaluacionViewModel` calcula con `ReglasRiesgo.evaluar` (TT-10) y guarda con `EvaluacionRepository.guardarEvaluacion` (TT-07): evaluación, 12 respuestas, 4 resultados por categoría y las acciones, en una sola transacción.
3. En "Evaluación guardada", **Ver resultados** abre `resultado/{evaluacionId}`. La evaluación sale de la pila: **Volver** lleva a Mis dispositivos.
4. Desde resultados:
   - **Aprender cómo hacerlo** → `contenido/{contenidoId}` (detalle mínimo; TT-15 lo amplía).
   - **Ver mis acciones** → pestaña Mis acciones (checklist de TT-13).

## 2. Qué muestra la pantalla

| Sección | Contenido |
|---|---|
| Encabezado | Nombre y sistema operativo del dispositivo (copia guardada en la evaluación) y fecha. |
| Nivel general | Ícono + texto del nivel ("Riesgo alto") + frase de qué hacer primero. |
| Aviso | "Este resultado es una orientación según tus respuestas. La app no revisa ni escanea tu dispositivo." |
| Por categoría | Ícono + nivel, puntaje de riesgo "X de máximo" y la explicación guardada. |
| Recomendaciones | Prioridad y categoría, título, descripción, **la respuesta que la activó** ("Por tu respuesta "No" a: …") y botón *Aprender cómo hacerlo*. |

**Riesgo con texto e ícono, no solo color:**

| Nivel | Ícono | Color (claro / oscuro) |
|---|---|---|
| Riesgo bajo | ✔ círculo (`ic_riesgo_bajo`) | `tertiary` (verde) |
| Riesgo medio | ⚠ triángulo (`ic_riesgo_medio`) | ámbar `#8A5100` / `#FFB870` |
| Riesgo alto | ! círculo (`ic_riesgo_alto`) | `error` |

El ícono es decorativo para el lector de pantalla (`contentDescription = null`) porque el texto de al lado ya dice el nivel. Los títulos de sección están marcados como encabezados (`semantics { heading() }`).

## 3. Lógica y estado

| Archivo | Rol |
|---|---|
| `domain/ResultadoEvaluacion.kt` | `ResultadoEvaluacionMapper`: convierte `EvaluacionConDetalle` (Room) en `ResultadoEvaluacion`. Lógica pura, sin Android. |
| `domain/ReglasRiesgo.kt` | `ordenarRecomendaciones` y `puntajeMaximo`, ahora compartidos por TT-10 y TT-12. |
| `viewmodel/ResultadoViewModel.kt` | `ResultadoUiState` (cargando / error / resultado) en `StateFlow`, observado con `collectAsState()`. Reintento si falla la lectura. |
| `ui/screens/ResultadoScreen.kt` | Solo dibuja el estado. Previews: resultado, sin recomendaciones, error. |
| `ui/components/NivelRiesgoIndicador.kt` | Ícono + texto + color de cada nivel. Previews claro y oscuro. |
| `ui/screens/ContenidoScreen.kt` | Detalle mínimo de contenido del catálogo local. |

Reglas del mapper:

- **Usa los niveles guardados**, no los recalcula: si en el futuro cambian las reglas, el historial no cambia.
- **Recomendaciones relacionadas con respuestas:** se obtienen de las respuestas guardadas con `recomendacionesPara` y se ordenan igual que en TT-10 (prioridad y luego categoría con más riesgo). Cada una lleva la pregunta (copia guardada) y la respuesta que la activó.
- **Enlace con el checklist:** cada recomendación se une con su `AccionEntity` por `recomendacionId` (`accionId`, `estadoAccion`) para TT-13.
- **Solo lectura:** el mapper y el ViewModel no escriben en Room.

## 4. Sin duplicados

- Guardar ocurre una sola vez (TT-11 bloquea los botones después de guardar, y la tabla `accion` tiene índice único `evaluacionId + recomendacionId`).
- Ver resultados o abrir el historial (TT-14) solo hace `obtenerDetalle` / `observarHistorial`: **no inserta nada**.
- `ResultadoRoomTest` lo comprueba consultando 5 veces y contando evaluaciones, resultados, respuestas y acciones.

## 5. Pruebas

| Prueba | Dónde | Qué comprueba |
|---|---|---|
| `ResultadoEvaluacionMapperTest` (9) | JVM | Datos de la evaluación, categorías en orden con puntaje máximo, mismo orden de recomendaciones que las reglas, respuesta que activó cada recomendación, enlace con acciones, sin acciones, todo "Sí", niveles guardados, consultas repetidas iguales. |
| `ResultadoViewModelTest` (3) | JVM | Carga, evaluación inexistente, error con reintento. |
| `ResultadoRoomTest` (3) | Emulador | Resultado guardado junto a su evaluación y acciones; consultar 5 veces no crea duplicados; id inexistente. |

```powershell
.\gradlew.bat :app:testDebugUnitTest
```
`ResultadoRoomTest`: con el emulador encendido, clic derecho sobre el archivo → **Run 'ResultadoRoomTest'**.

### Prueba manual

1. Evaluar un dispositivo respondiendo varias "No" / "No lo sé" → **Finalizar** → **Ver resultados**.
2. Comprobar ícono + texto en el nivel general y en cada categoría, y el aviso de orientación.
3. Comprobar que cada recomendación dice por qué respuesta aparece y que las de prioridad alta van primero.
4. **Aprender cómo hacerlo** abre el contenido; **Volver** regresa a resultados.
5. **Volver** desde resultados lleva a Mis dispositivos (no a la evaluación).
6. Database Inspector: una sola fila nueva en `evaluacion`, 4 en `resultado_categoria` y una acción por recomendación, aunque se abra la pantalla varias veces.

## 6. Pendiente para otras tareas

- **TT-13:** la pestaña Mis acciones aún es un placeholder; el botón ya navega a ella.
- **TT-14:** el historial abrirá esta misma ruta `resultado/{evaluacionId}`.
- **TT-15:** reemplazar o ampliar `ContenidoScreen` con la lista y filtros de Aprende.
- **TT-27:** revisar contraste del ámbar y unificar íconos de error del resto de pantallas.
