# TT-16 · Recordatorios y notificaciones locales

**Responsable:** Enzo Morales · **Revisión:** Gerard Zambrano
**Alcance:** Evaluación Parcial 2 · datos locales, sin servidor ni red.
**Dependencias:** TT-07 (Room), TT-08 (preferencia de avisos), TT-13 (acciones).
**Rúbrica:** IE 2.1.2 / IE 2.4.1 (recurso nativo: notificaciones del sistema).

## 1. Flujo

1. Mis acciones → en una acción **pendiente**, **Programar recordatorio**.
2. Diálogo con **Elegir fecha** (DatePicker de Material 3) y **Elegir hora** (TimePicker, 24 h).
3. **Guardar**:
   - Android 13 o superior sin permiso → se pide `POST_NOTIFICATIONS`.
   - Con o sin permiso, el recordatorio **se guarda** en Room y se programa el aviso. Si no hay permiso, un Snackbar explica cómo activarlo y Mis acciones muestra un aviso; la app no se bloquea.
4. La tarjeta muestra "Recordatorio: fecha y hora" y el botón cambia a **Editar recordatorio** (cambiar fecha/hora o **Cancelar recordatorio**).
5. A la hora programada aparece la notificación **"Recordatorio de TíoTec"** con el título de la acción. Al tocarla, la app abre **Mis acciones** (también si ya estaba abierta).

## 2. Validaciones (en el ViewModel, no en la pantalla)

| Regla | Mensaje |
|---|---|
| Fecha obligatoria | "Elige una fecha." |
| Hora obligatoria | "Elige una hora." |
| Al menos 1 minuto en el futuro | "Elige una fecha y hora en el futuro." |
| Como máximo 365 días | "Elige una fecha dentro del próximo año." |

Cada error se muestra bajo su campo, con ícono, y se borra al corregir ese campo. `RecordatorioRepository` vuelve a validar como última barrera.

## 3. Cuándo NO se muestra el aviso

`RecordatorioWorker` vuelve a leer Room a la hora del aviso y no notifica si:

- el recordatorio se **canceló** o se **reprogramó** (el trabajo anterior se reemplaza: nunca quedan dos);
- la acción ya está **completada**;
- el dispositivo se **archivó** (TT-07 cancela sus recordatorios);
- el usuario apagó **"Avisos"** en Ajustes de la app (DataStore, TT-08);
- el teléfono no permite notificaciones.

En todos los casos el recordatorio queda como `ENVIADO` o `CANCELADO` y deja de aparecer en la tarjeta.

## 4. Código

| Archivo | Rol |
|---|---|
| `domain/ValidadorRecordatorio.kt` | Reglas de fecha/hora y conversión DatePicker (UTC) + TimePicker → hora local. `java.util.Calendar` porque minSdk = 24. |
| `repository/GestorRecordatorios.kt` | Guarda en Room y programa/reprograma/cancela el aviso. Interfaz `ProgramadorAvisos`. |
| `notificaciones/RecordatorioWorker.kt` | `CoroutineWorker` que muestra el aviso; `ProgramadorWorkManager` con `enqueueUniqueWork(REPLACE)`. |
| `notificaciones/Avisos.kt` | Canal, comprobación de permiso y notificación con `PendingIntent` a Mis acciones. |
| `viewmodel/RecordatorioViewModel.kt` | `RecordatoriosUiState` (recordatorio por acción, formulario con errores, guardando, mensaje). |
| `ui/components/RecordatorioDialogo.kt` | Diálogo con DatePicker y TimePicker. |
| `ui/screens/AccionesRuta.kt` | Une AccionesViewModel y RecordatorioViewModel, pide el permiso y muestra el Snackbar. |
| `AndroidManifest.xml` | Permiso `POST_NOTIFICATIONS`; `launchMode="singleTop"` para recibir el aviso con la app abierta. |
| `MainActivity.kt` · `TioTecApp.kt` | Leen el destino del aviso y navegan a Mis acciones. |
| `libs.versions.toml` · `build.gradle.kts` | WorkManager 2.10.1. |

**¿Por qué WorkManager?** El aviso se programa aunque la app se cierre y se mantiene después de reiniciar el teléfono, sin pedir el permiso de alarmas exactas. Puede llegar con algunos minutos de diferencia si el teléfono está ahorrando batería.

## 5. Pruebas

| Prueba | Dónde | Qué comprueba |
|---|---|---|
| `ValidadorRecordatorioTest` (9) | JVM | Combinar/separar fecha y hora, válido, vacíos, hora pasada, menos de 1 minuto, más de un año, hora fuera de rango. |
| `GestorRecordatoriosTest` (5) | JVM | Crear programa el aviso, reprogramar lo reemplaza, cancelar lo quita, no se reprograma uno cancelado, fecha pasada no guarda nada. |
| `RecordatorioViewModelTest` (7) | JVM | Errores por campo, corregir borra el error, guardar válido, sin permiso guarda igual, editar y reprogramar, cancelar, cerrar sin guardar. |

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

### Prueba manual (emulador con Android 13 o superior)

1. Mis acciones → **Programar recordatorio** → **Guardar** sin fecha: aparecen los errores por campo.
2. Elegir hoy y una hora ya pasada: "Elige una fecha y hora en el futuro."
3. Elegir hoy y **2 minutos más** → Guardar → aceptar el permiso. Snackbar "Recordatorio programado" y la tarjeta muestra la hora.
4. Ir a Inicio (o salir de la app) y esperar: llega la notificación. Tocarla abre **Mis acciones**.
5. **Editar recordatorio** → cambiar la hora → solo llega un aviso, a la nueva hora.
6. **Cancelar recordatorio** → no llega ningún aviso.
7. Denegar el permiso (o desinstalar y volver a instalar y elegir "No permitir"): el recordatorio se guarda, aparece el mensaje para activarlas y la app sigue funcionando.
8. Programar uno, marcar la acción como completada: no llega aviso.
9. Cerrar la app desde Recientes y esperar: el aviso llega igual.

Si el aviso tarda, en Android Studio: **App Inspection → Background Task Inspector** muestra el trabajo `recordatorio-N` y su hora.
