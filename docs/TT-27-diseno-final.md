# TT-27 · Diseño final adaptado al alcance local

Rúbrica: **IE 2.1.1** (15 %). Dependencias: TT-03, TT-05.

La primera parte de TT-27 (bocetos y documento de TT-03 al alcance local, sin API ni administrador) se integró en el **PR #5**. Este documento registra las **mejoras pendientes de la revisión** y cómo verificarlas.

## 1. Mejoras realizadas

| # | Pendiente de la revisión | Qué se hizo | Archivos |
|---|---|---|---|
| 1 | Reemplazar los símbolos ⌂ ✓ ? ○ de la navegación | Íconos vectoriales Material: `home`, `checklist`, `school`, `person`. La etiqueta visible se mantiene y el ícono es decorativo (`contentDescription = null`), así TalkBack no lee dos veces el destino. Se usan en la `NavigationBar` (Compact) y en el `PermanentNavigationDrawer` (Medium/Expanded). | `res/drawable/ic_nav_*.xml`, `TioTecApp.kt` |
| 2 | Cambiar el `TextButton` "← Volver" | `IconButton` con la flecha Material `arrow_back` y descripción accesible **"Volver"**. El vector tiene `android:autoMirrored="true"`: en idiomas de derecha a izquierda la flecha se invierte sola. | `res/drawable/ic_volver.xml`, `TioTecApp.kt` |
| 3 | Completar los esquemas claro y oscuro | Se definieron `primaryContainer`, `secondaryContainer`/`onSecondaryContainer` (opción elegida, chips), `tertiaryContainer`, `error`/`errorContainer`, `surfaceVariant`/`onSurfaceVariant`, `outline`/`outlineVariant` y los cinco `surfaceContainer*` (tarjetas, barras, diálogos), con la paleta azul. El color de **riesgo medio** pasó de la pantalla a `Color.kt`. Ningún color queda con el morado por defecto de Material. | `ui/theme/Color.kt`, `ui/theme/Theme.kt`, `NivelRiesgoIndicador.kt` |
| 4 | Títulos repetidos entre la barra superior y el contenido | "Historial" y "Solicitudes" se repetían: ahora la barra dice **"Historial de evaluaciones"** y **"Solicitudes de asistencia"**, y se quitó el título duplicado del contenido (queda la descripción). "Mis dispositivos" sigue apareciendo una sola vez (TT-05). El título de la barra usa una línea con "…" para que el texto aumentado no tape la flecha. | `TioTecApp.kt`, `HistorialScreen.kt`, `SolicitudesScreen.kt` |
| 5 | Previews y evidencia de teléfono/tablet en claro/oscuro | Previews nuevos: **Compact oscuro**, **Compact con texto al 150 %** y **Medium** (tablet vertical), además de Compact y Expanded que ya existían. La sección 4 indica qué capturas tomar. | `TioTecApp.kt` |

## 2. Adaptación por tamaño (sin cambios, verificada)

| Tamaño | Ancho | Navegación |
|---|---|---|
| **Compact** (se diseña primero) | < 600 dp · teléfono | `NavigationBar` inferior con 4 destinos |
| **Medium** | 600–839 dp · tablet vertical, plegable | `PermanentNavigationDrawer` lateral |
| **Expanded** | ≥ 840 dp · tablet horizontal | `PermanentNavigationDrawer` lateral |

- El tamaño se calcula **una sola vez** en `MainActivity` con `calculateWindowSizeClass(this)` y se pasa a `TioTecApp`.
- No se usa `BoxWithConstraints` para la estructura.
- Resultados e Historial limitan el ancho del contenido a 720 dp, para que en tablet no queden líneas demasiado largas.
- Durante la evaluación se ocultan la barra y el menú, para no salir por error.

## 3. Contraste

`ContrasteTemaTest` comprueba los pares de colores de texto en **claro y oscuro** con la fórmula de WCAG.

| Par (texto sobre fondo) | Claro | Oscuro |
|---|---|---|
| Texto sobre tarjeta (`onSurface` / `surfaceContainerHighest`) | 11,1:1 | 9,8:1 |
| Opción elegida (`onSecondaryContainer` / `secondaryContainer`) | 13,3:1 | 7,2:1 |
| Ayuda (`onSurfaceVariant` / `surface`) | 9,3:1 | 9,3:1 |
| Error sobre tarjeta | 5,0:1 | 7,0:1 |
| Riesgo bajo / medio / alto sobre tarjeta | 5,0 / 5,0 / 5,0:1 | 7,1 / 7,0 / 7,0:1 |
| Botón (`onPrimary` / `primary`) | 8,8:1 | 7,8:1 |

Todos superan **4,5:1** (AA para texto normal). Los bordes (`outline`) superan 3:1.

## 4. Verificación y evidencia

### En Android Studio
Abrir `ui/navigation/TioTecApp.kt` → vista **Split** y revisar los previews *Navegación Compact*, *Compact oscuro*, *Compact texto 150 %*, *Medium* y *Expanded*. En `NivelRiesgoIndicador.kt`, revisar *Niveles claro* y *Niveles oscuro*.

### En el emulador (capturas para Trello)
1. **Teléfono claro:** Inicio, Mis acciones, Aprende, Perfil. Los íconos se ven en la barra inferior y el destino elegido se marca.
2. **Teléfono oscuro** (Perfil → Ajustes → Oscuro): las mismas pantallas, más una evaluación con una opción elegida y un resultado con los tres niveles.
3. **Tablet** (emulador *Pixel Tablet* o *Medium Tablet*), en vertical y en horizontal: aparece el menú lateral con íconos y etiquetas.
4. **Volver:** entrar a Mis dispositivos, Historial, Ajustes, Solicitudes y un contenido de Aprende. La flecha de la barra regresa a la pantalla anterior, igual que el botón atrás del sistema.
5. **Títulos:** Historial y Solicitudes muestran el título **una sola vez**, en la barra superior.
6. **Texto aumentado:** Ajustes del teléfono → Pantalla → **Tamaño de fuente al máximo**. Recorrer Perfil, un formulario de dispositivo y una evaluación: todo se puede leer y desplazar, y los botones no se tapan.
7. **Teclado:** en Perfil y Agregar dispositivo, el teclado no tapa el campo ni el botón Guardar.
8. **TalkBack:** con TalkBack activo, tocar un destino de la barra (lee "Inicio", no "Inicio Inicio") y la flecha (lee "Volver").
