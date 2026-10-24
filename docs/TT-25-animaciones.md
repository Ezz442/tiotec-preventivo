# TT-25 · Animaciones útiles y retroalimentación

Rúbrica: **IE 2.2.2** (10 %). Dependencias: TT-05, TT-11, TT-13.

Las animaciones **solo muestran el estado que ya calculó el ViewModel**. No validan, no deciden y no usan esperas artificiales (`delay`). Ningún control queda bloqueado por una animación: los botones se deshabilitan solo mientras la operación real está en curso (`guardando`, `eliminando`, `procesandoFoto`, `actualizando`).

## 1. Qué se animó y por qué

| Dónde | Qué cambia | API de Compose | Para qué sirve |
|---|---|---|---|
| Evaluación guiada | Cambio de pregunta: entra por la derecha al avanzar y por la izquierda al volver | `AnimatedContent` + `slideInHorizontally`/`fadeIn` | Se nota que cambió la pregunta y hacia dónde se avanzó |
| Evaluación guiada | Barra *Respondidas X de 12* | `animateFloatAsState` + `LinearProgressIndicator` | El progreso avanza en lugar de saltar |
| Evaluación, Perfil, Dispositivos | Opción elegida (Sí/No/No lo sé, experiencia, sistema operativo): fondo, borde, grosor y texto | `animateColorAsState` + `animateDpAsState` | La selección se ve con más que el punto del RadioButton |
| Todos los formularios | Error por campo con ícono y texto: aparece y se va | `AnimatedVisibility` (`fadeIn` + `expandVertically`) | El error no "salta"; TalkBack lo lee (`liveRegion`) |
| Botones que guardan | Indicador pequeño junto al texto mientras guarda | `AnimatedVisibility` + `CircularProgressIndicator` | Carga ligada a `guardando`, no a un tiempo fijo |
| Mis acciones | Contador de pendientes: el número sube o baja | `AnimatedContent` + `slideInVertically` | Se ve el efecto de completar una acción |
| Mis acciones | Tarjeta: cambia de color al completarse y sale de *Pendientes* suavemente | `animateColorAsState` + `Modifier.animateItem()` | La acción no desaparece de golpe |
| Mis acciones | Fecha del recordatorio: aparece al programarlo y se va al cancelarlo | `AnimatedVisibility` | Confirma el cambio en la misma tarjeta |
| Resultados | Barra de puntaje por categoría que se llena al abrir, con el color del nivel | `animateFloatAsState` (desde 0) | Compara categorías de un vistazo; el texto sigue diciendo el puntaje |
| Resultados (TT-14) | *Ver las 12 respuestas* | `AnimatedVisibility` | Detalle que se muestra al pedirlo |
| Historial, Aprende, Solicitudes, Mis dispositivos | Tarjetas que entran, salen o se reordenan al filtrar, agregar o eliminar | `Modifier.animateItem()` | El cambio de filtro se entiende sin perder el lugar |

## 2. Código

| Archivo | Rol |
|---|---|
| `ui/components/Movimiento.kt` | Duraciones (150 / 300 / 600 ms), sentido de la transición y fracción de una barra. Sin Compose: se prueba en la JVM. |
| `ui/components/Animaciones.kt` | Componentes reutilizables: `MensajeError`, `MensajeInformativo`, `OpcionSeleccionable`, `GrupoOpciones`, `BarraAnimada`, `TextoConCarga`. Preview con los estados finales. |
| `ui/screens/EvaluacionScreen.kt` | Cambio de pregunta, progreso, opciones y error. |
| `ui/screens/AccionesScreen.kt` | Contador, color de tarjeta, `animateItem` y recordatorio. |
| `ui/screens/ResultadoScreen.kt` | Barra de puntaje por categoría. |
| `PerfilScreen`, `DispositivoFormulario`, `DispositivosScreen`, `SolicitudDialogo`, `RecordatorioDialogo`, `RevisionEvaluacionScreen`, `SolicitudDetalleScreen` | Errores animados e indicador de guardado. |
| `HistorialScreen`, `AprendeScreen`, `SolicitudesScreen` | `animateItem` en las listas. |

Los errores siguen saliendo del ViewModel (`errorAlias`, `errorPregunta`, `errorFecha`…). TT-25 no cambia ninguna validación ni ningún ViewModel.

## 3. Decisiones

- **Duraciones cortas:** 150 ms para colores y mensajes, 300 ms para cambiar de pregunta o de lista y 600 ms solo para las barras de resultados, que se leen una vez. Ninguna pasa de 1 segundo.
- **La pregunta que sale no recibe toques:** durante la transición, las opciones de la pregunta anterior quedan deshabilitadas para que una respuesta no se guarde en la pregunta equivocada.
- **No se tapan mensajes:** los errores empujan el contenido hacia abajo, en lugar de ponerse encima. Los Snackbar existentes no cambian.
- **El color no es la única señal:** la opción elegida mantiene el RadioButton y el nivel de riesgo mantiene ícono + texto. Las barras son decorativas para TalkBack (`clearAndSetSemantics`), porque el texto ya dice el puntaje.
- **Accesibilidad:** si en el teléfono se activa *Quitar animaciones* (Accesibilidad) o la *escala de animación* en 0 (Opciones de desarrollador), Compose omite las animaciones y el contenido aparece de inmediato.

## 4. Pruebas

| Prueba | Dónde | Qué comprueba |
|---|---|---|
| `MovimientoTest` (9) | JVM | Sentido al avanzar, volver y saltar desde la revisión; contador que sube o baja; fracción del progreso y del puntaje; límites 0–1; máximo 0 sin dividir por cero; duraciones ordenadas y menores a 1 s. |

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Las animaciones en sí se revisan en el emulador (sección 5) y en el Preview *Animaciones · estados finales* de `Animaciones.kt`.

### 5. Prueba manual y evidencia

Grabar la pantalla del emulador (botón ⋮ → *Record screen*) en estos recorridos y adjuntar el video o GIF en la tarjeta de Trello:

1. **Evaluación:** responder 3 preguntas → la pregunta entra por la derecha y la barra avanza. *Anterior* → entra por la izquierda. Tocar *Siguiente* sin responder → el error aparece con ícono; al elegir una opción desaparece. Tocar rápido *Siguiente* y una opción durante la transición → la respuesta queda en la pregunta nueva.
2. **Opciones:** al elegir Sí/No/No lo sé, el fondo y el borde cambian suavemente. Lo mismo en Perfil (experiencia) y en Agregar dispositivo (sistema operativo).
3. **Guardar:** en Perfil, *Guardar perfil* muestra el indicador solo mientras guarda y luego aparece el Snackbar *Perfil guardado correctamente.* (desde TT-26). Con el alias vacío, el error aparece bajo el campo.
4. **Resultados:** al finalizar, las barras por categoría se llenan con el color de su nivel.
5. **Mis acciones:** marcar una acción → la tarjeta sale de *Pendientes* y el contador baja. En *Completadas*, desmarcarla → cambia de color y sale de la lista. Programar un recordatorio → la fecha aparece en la tarjeta.
6. **Listas:** en Historial y Aprende, cambiar de filtro → las tarjetas entran y salen sin saltos.
7. **Sin animaciones:** Ajustes del teléfono → Accesibilidad → *Quitar animaciones*. Repetir el paso 1: todo funciona igual, sin transiciones.
8. **Tema oscuro:** Perfil → Ajustes → Oscuro. Repetir los pasos 2 y 4: los colores se leen bien.
