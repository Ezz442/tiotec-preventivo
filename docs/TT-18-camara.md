# TT-18 · Cámara para la imagen del dispositivo ficticio

**Responsable:** Enzo Morales · **Revisión:** Gerard Zambrano
**Alcance:** Evaluación Parcial 2 · datos locales, sin servidor ni red.
**Dependencias:** TT-05, TT-07 (`imagenRuta` en Room), TT-09 (Mis dispositivos).
**Rúbrica:** IE 2.4.1 · recurso nativo 1 (TT-19 galería = recurso nativo 2).

> Usa fotos de **objetos de prueba** (una taza, un cuaderno). No fotografiar personas, documentos ni pantallas con datos reales.

## 1. Flujo

1. Mis dispositivos → en la tarjeta, **Tomar foto**.
2. Se abre la **app de cámara del teléfono**. Al aceptar la foto, vuelve a TíoTec y la tarjeta la muestra. Snackbar "Foto guardada."
3. **Reemplazar foto** toma una nueva y borra el archivo anterior.
4. **Quitar foto** pide confirmación, borra el archivo y quita la asociación.
5. Cancelar en la cámara (atrás o ✕) no cambia nada: la foto anterior se mantiene y el archivo temporal se borra.
6. Si el teléfono no tiene cámara, aparece "Este dispositivo no tiene una cámara disponible." y el resto de la app funciona igual.

## 2. Decisiones técnicas

| Tema | Decisión | Por qué |
|---|---|---|
| API | `ActivityResultContracts.TakePicture` (Activity Result API) | Forma moderna de pedir un resultado a otra app; reemplaza `startActivityForResult`. Devuelve `true`/`false`. |
| URI | `FileProvider` (`${applicationId}.archivos`, `res/xml/rutas_archivos.xml`) | Desde Android 7 no se puede compartir una ruta `file://`. FileProvider entrega una URI `content://` con permiso temporal solo para esa foto. |
| Permiso CAMERA | **Se pide en tiempo de ejecución** (`RequestPermission`) antes de abrir la cámara | Se agregó después de TT-18 para mostrar el flujo de permiso. Si se declara CAMERA en el manifiesto, Android lo exige también para `TakePicture`, así que la app revisa `checkSelfPermission` y, si falta, muestra el diálogo del sistema. Si se rechaza, avisa y se puede usar la galería, que no necesita permiso. |
| `uses-feature camera.any` | `required="false"` | La app se puede instalar en equipos sin cámara; en ellos se muestra el aviso. |
| Dónde se guarda | `filesDir/imagenes/disp_{id}_{marca}.jpg` (almacenamiento privado) | No pide permisos de almacenamiento, otras apps no lo ven y se borra al desinstalar. |
| Qué guarda Room | Solo la **ruta relativa** en `dispositivo.imagenRuta` | La base no crece con imágenes; la ruta sigue válida al reabrir la app. |
| Mostrar | `ImagenDispositivo` (componente compartido con TT-19) | Lee el archivo en segundo plano y reducido (`inSampleSize`), para no trabar la lista ni gastar memoria. Si el archivo falta: "Imagen no disponible". |
| Archivos huérfanos | Reemplazar o quitar borra el archivo anterior; eliminar un dispositivo **sin** evaluaciones borra su foto | Si se **archiva** (tiene evaluaciones), la foto se conserva junto a su historial. |
| Seguridad de rutas | `AlmacenImagenes.archivo()` rechaza rutas fuera de `imagenes/` (ej. `../`) | Nunca se borra un archivo que no sea una foto de la app. |

## 3. Código

| Archivo | Rol |
|---|---|
| `data/imagenes/AlmacenImagenes.kt` | Interfaz + `AlmacenImagenesLocal` (java.io): nueva ruta, archivo, existe, borrar. Probable en la JVM. |
| `viewmodel/DispositivosViewModel.kt` | `prepararFoto`, `fotoTomada`, `camaraNoDisponible`, `solicitarQuitarFoto`/`confirmarQuitarFoto`/`cancelarQuitarFoto`; borra la foto al eliminar. |
| `viewmodel/DispositivosUiState.kt` | `capturaFoto`, `dispositivoQuitarFoto`, `procesandoFoto`. |
| `ui/screens/DispositivosRuta.kt` | Launcher `TakePicture`, comprobación de cámara, `FileProvider.getUriForFile`. |
| `ui/components/ImagenDispositivo.kt` | Componente reutilizable de la foto (sin foto / cargando / imagen / no disponible). |
| `ui/screens/DispositivosScreen.kt` | Foto en la tarjeta, botones y diálogo para quitar. |
| `AndroidManifest.xml` · `res/xml/rutas_archivos.xml` | `provider`, `uses-feature`, `queries`. |

### Para TT-19 (galería)

Reutilizar `AlmacenImagenes.nuevaRuta()` para **copiar** la imagen elegida al almacenamiento privado (así no depende de permisos persistentes de la URI), luego `dispositivoRepository.actualizarImagen()` y mostrarla con `ImagenDispositivo`. El reemplazo y el quitar ya borran el archivo anterior, venga de la cámara o de la galería.

## 4. Pruebas

| Prueba | Dónde | Qué comprueba |
|---|---|---|
| `AlmacenImagenesLocalTest` (6) | JVM | Ruta dentro de `imagenes/`, rutas únicas, archivo vacío = cancelada, borrar sin fallar, rechaza `../`, sigue válida al reabrir. |
| `DispositivosFotoViewModelTest` (10) | JVM | Tomar, reemplazar (borra la anterior), cancelar (conserva la anterior), éxito sin archivo, sin cámara no bloquea, no dos fotos a la vez, quitar con confirmación, cancelar quitar, eliminar borra la foto, archivar la conserva. |

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

### Prueba manual (emulador)

El emulador trae una cámara virtual (una sala 3D): sirve como "objeto de prueba".

1. Mis dispositivos → **Tomar foto** → tomar y aceptar → la tarjeta muestra la foto y aparece "Foto guardada.".
2. **Reemplazar foto** → tomar otra → se ve la nueva.
3. **Tomar/Reemplazar** → cancelar en la cámara → se mantiene la anterior y aparece "No se tomó la foto…".
4. **Quitar foto** → Cancelar (no cambia) → Quitar (desaparece).
5. Cerrar la app desde Recientes y abrirla: la foto sigue en la tarjeta.
6. Rotar la pantalla con la cámara abierta y aceptar la foto: se guarda igual.
7. Eliminar un dispositivo **sin** evaluaciones que tenga foto: en **Device Explorer** (`data/data/cl.duoc.tiotecpreventivo/files/imagenes`) el archivo ya no está.
8. **Permiso:** la primera vez que se toca *Tomar foto* aparece "¿Permitir que TíoTec tome fotos y grabe video?". *Permitir* abre la cámara. *No permitir* muestra "Sin permiso de cámara no se puede tomar la foto…" y la app sigue funcionando. Para repetirlo: Ajustes del teléfono → Apps → TíoTec → Permisos → Cámara → No permitir.
9. (Opcional) Emulador sin cámara (AVD Manager → Camera: None): aparece "Este dispositivo no tiene una cámara disponible.".
