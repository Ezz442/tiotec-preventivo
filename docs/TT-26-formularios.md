# TT-26 · Formularios y estado centralizados en ViewModels

Rúbrica: **IE 2.1.2** (15 %) · **IE 2.2.1** (10 %). Dependencias: TT-04, TT-05.

## 1. Cómo se maneja cada formulario

Todos los formularios siguen el mismo flujo:

```
Composable ──onCampoChange()──▶ ViewModel ──validar()──▶ ValidadorFormularios / ValidadorRecordatorio
    ▲                              │
    └──── collectAsState() ◀── StateFlow<UiState>  (valor del campo + error por campo + guardando + mensaje)
```

- El estado vive en un `MutableStateFlow` privado del ViewModel y se expone como `StateFlow`.
- La pantalla lo observa con `collectAsState()` y solo **muestra**: no valida nada.
- Cada cambio llama a un evento (`onAliasChange`, `onNombreChange`, `onFechaChange`…), que guarda el valor y borra el error de ese campo.
- Al guardar, el ViewModel valida con funciones puras de `domain/` y copia el error de cada campo a su UiState.
- El resultado (bien o mal) se muestra en un **Snackbar**. Las acciones que borran o descartan datos se confirman con un **AlertDialog**. Los errores por campo nunca se reemplazan por el Snackbar.

## 2. Formularios y reglas

| Formulario | Modelo y estado | Campos | Reglas | Mensaje y confirmación |
|---|---|---|---|---|
| **Perfil** | `PerfilUiState` · `ValidacionPerfil` | Alias (texto) · experiencia (selección) | Alias obligatorio, 2 a 30 caracteres después de normalizar espacios, **sin correo, teléfono ni RUT** · experiencia obligatoria | Snackbar "Perfil guardado correctamente." · error de guardado bajo el botón |
| **Dispositivo** | `DispositivosUiState` · `ValidacionDispositivo` | Nombre (texto) · sistema operativo (selección) | Nombre obligatorio, 2 a 30 caracteres, sin datos personales, **no repetido entre los activos** (Room) · sistema obligatorio · necesita perfil | Snackbar al registrar, actualizar, eliminar o archivar · AlertDialog al eliminar · **AlertDialog "¿Descartar cambios?"** al cancelar con cambios |
| **Evaluación** | `EvaluacionUiState` | Una respuesta por pregunta (selección) | Cada pregunta exige respuesta antes de avanzar · revisar exige las 12 · no se guarda dos veces | AlertDialog al salir sin guardar · diálogo de resultado guardado |
| **Recordatorio** | `FormularioRecordatorio` · `ValidadorRecordatorio` | Fecha · hora | Fecha y hora obligatorias · **la hora depende de la fecha**: fecha y hora deben quedar en el futuro · fecha dentro del próximo año | Snackbar programado, reprogramado o cancelado · **AlertDialog "¿Cancelar este recordatorio?"** |
| **Solicitud** | `SolicitudesUiState` | Categoría (selección) | Categoría obligatoria · necesita perfil | Snackbar "Solicitud simulada guardada…" · AlertDialog al cambiar de estado |
| **Ajustes** | `PreferenciasUiState` | Tema · avisos | Opciones cerradas: no hay valores inválidos | Snackbar si una preferencia no se pudo guardar |

No se agregaron campos personales: la app solo pide un alias ficticio, el nombre de un dispositivo de prueba, selecciones y fechas. La regla de "sin correo, teléfono ni RUT" existe justamente para evitar que se ingresen datos reales.

## 3. Qué cambió en TT-26

| Cambio | Archivos |
|---|---|
| Reglas de perfil, dispositivo y solicitud reunidas en `ValidadorFormularios` (puro, sin Android), con los mensajes como constantes | `domain/ValidadorFormularios.kt` |
| Los ViewModels usan el validador en lugar de reglas escritas en cada uno | `PerfilViewModel`, `DispositivosViewModel`, `SolicitudesViewModel` |
| Alias y nombre: espacios normalizados ("Tía    Demo" → "Tía Demo") y rechazo de correos, teléfonos y RUT (7 dígitos o más, o `@`) | `ValidadorFormularios` |
| Contador de caracteres bajo alias y nombre ("8/30") | `PerfilScreen`, `DispositivoFormulario` |
| Perfil: el resultado pasa de texto a **Snackbar** (`consumirMensaje`) | `PerfilViewModel`, `PerfilScreen`, `TioTecApp` |
| Dispositivo: **AlertDialog** para descartar cambios sin guardar (`cancelarFormulario` → `descartarCambios` / `seguirEditando`) | `DispositivosViewModel`, `DispositivosUiState`, `DispositivosScreen`, `DispositivosRuta` |
| Recordatorio: **AlertDialog** antes de cancelar (`pedirCancelacion` / `volverDeCancelacion`) | `RecordatorioViewModel`, `RecordatorioDialogo`, `AccionesRuta` |

## 4. Pruebas

| Prueba | Qué comprueba |
|---|---|
| `ValidadorFormulariosTest` (12) | Campos vacíos con un error por campo; límites 1/2/30/31; largo medido sin espacios; normalización; correo, teléfono y RUT rechazados; números cortos permitidos ("iPhone 15"); categoría obligatoria; detección de cambios al crear y al editar; contador. |
| `PerfilViewModelTest` (+2) | Alias con correo, teléfono o RUT no se guarda; el mensaje del Snackbar se consume una vez. |
| `DispositivosViewModelTest` (+4, 1 actualizada) | Cancelar con cambios pide confirmación y descartar no guarda; cancelar sin cambios cierra directo; *Seguir editando* conserva lo escrito; nombre con correo o teléfono rechazado; espacios normalizados al guardar. |
| `RecordatorioViewModelTest` (+2) | Cancelar pide confirmación y *Volver* no borra nada; un recordatorio nuevo no tiene nada que cancelar. |

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

## 5. Prueba manual

1. **Perfil:** guardar vacío → dos errores con ícono. Escribir `tia@correo.cl` → "Usa un alias ficticio: no ingreses correos, teléfonos ni RUT.". Escribir `Tía    Demo` y elegir experiencia → Snackbar "Perfil guardado correctamente." y el campo queda "Tía Demo". El contador cambia al escribir.
2. **Dispositivo:** agregar con `+56 9 1234 5678` → error de dato personal. Escribir un nombre y tocar **Cancelar** → "¿Descartar cambios?". *Seguir editando* conserva el texto; *Descartar* cierra sin guardar. Abrir **Editar** y cancelar sin cambios → cierra sin preguntar.
3. **Recordatorio:** en Mis acciones, abrir un recordatorio ya programado → **Cancelar recordatorio** → "¿Cancelar este recordatorio?". *Volver* lo mantiene; *Sí, cancelar* lo borra y muestra el Snackbar.
4. **Solicitud:** guardar sin categoría → error bajo las categorías; con categoría → Snackbar.
5. Comprobar que en ningún caso el Snackbar reemplaza a un error por campo.
