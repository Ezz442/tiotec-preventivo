# TT-29 · Conexión de la app con los microservicios propios

La app consume **solicitudes-service** (puerto 8081) y **dispositivos-service** (puerto 8082), creados en TT-28 (`backend/`), con Retrofit, corrutinas y MVVM.

## Enfoque: el servicio manda, Room es la copia local

- Las pantallas **siempre leen de Room** (`Flow`), así que la lista aparece al instante y también sin conexión.
- Al abrir una pantalla se pide la lista al servicio (`sincronizar()`) y se actualiza Room.
- **Escribir exige servicio**: crear, editar, cambiar estado, archivar o eliminar primero se hace en el servicio y, si resulta bien, se refleja en Room. Si falla, se muestra el error y no se guarda nada local.
- Room usa **los mismos ids que el servicio**, por lo que las claves foráneas (evaluaciones → dispositivo) no cambian y no se necesitan columnas extra.
- **Datos que no se sincronizan** (quedan solo en el teléfono): perfil, foto del dispositivo, evaluaciones, resultados, acciones, recordatorios, historial de evaluaciones y preferencias.

| Dato | Dónde vive | Cómo se usa |
|---|---|---|
| Solicitudes e historial de estados | solicitudes-service (copia en Room) | CRUD completo desde Perfil → Solicitudes |
| Dispositivos (nombre, sistema operativo, archivado) | dispositivos-service (copia en Room) | CRUD desde Inicio → Mis dispositivos |
| Foto, evaluaciones y su historial | Room (solo local) | Evaluar, resultados, historial |

## Endpoints usados

| Pantalla / acción | Método y ruta |
|---|---|
| Solicitudes: abrir lista | `GET /api/solicitudes` |
| Solicitudes: abrir detalle | `GET /api/solicitudes/{id}` |
| Solicitudes: nueva solicitud | `POST /api/solicitudes` |
| Solicitudes: cambiar estado | `PATCH /api/solicitudes/{id}/estado` |
| Solicitudes: eliminar | `DELETE /api/solicitudes/{id}` |
| Dispositivos: abrir lista | `GET /api/dispositivos` |
| Dispositivos: agregar | `POST /api/dispositivos` |
| Dispositivos: editar | `PUT /api/dispositivos/{id}` |
| Dispositivos: eliminar sin evaluaciones | `DELETE /api/dispositivos/{id}` |
| Dispositivos: "eliminar" con evaluaciones (se archiva) | `PATCH /api/dispositivos/{id}/archivado` |

Rutas, cuerpos y errores en `backend/*/README.md`.

## Estructura

```
data/remote/       ConfigRed (URLs), ServiciosRemotos (Retrofit), SolicitudesApi, DispositivosApi,
                   model/ (DTOs), Mapeadores (DTO → entidad Room), ErrorRemoto (errores con mensaje de usuario)
data/local/dao/    SincronizacionDao (@Upsert con ids del servicio)
repository/        SolicitudRepositorySincronizado, DispositivoRepositorySincronizado
                   (extienden los repositorios locales y agregan la llamada al servicio)
viewmodel/         ahora llaman a sincronizar() y muestran ErrorRemoto
ui/components/     AvisoSincronizacion (cargando / error + Reintentar)
```

## Estados de la interfaz

- **Cargando:** "Actualizando desde el servicio…" mientras llega la respuesta.
- **Error:** mensaje del servicio (400, 404, 409) o "No hay conexión con el servicio…" con botón **Reintentar**; se siguen viendo los datos guardados.
- **Vacío:** "Todavía no hay solicitudes." / lista de dispositivos vacía.

## Cómo probar

1. Iniciar MySQL y los dos servicios (`backend/README.md`).
2. Instalar la app **limpia** (desinstalar o borrar datos): los dispositivos creados antes de TT-29 solo existen en el teléfono y sus ids podrían coincidir con los del servicio.
3. Emulador: las URLs ya apuntan a `10.0.2.2`. Teléfono físico: cambiar `ConfigRed.kt` a la IP del computador.
4. Crear un dispositivo y una solicitud en la app y comprobar la fila en MySQL (`tiotec_dispositivos.dispositivo`, `tiotec_solicitudes.solicitud`).
5. Apagar un servicio y abrir la pantalla: aparece el error con Reintentar y siguen visibles los datos guardados.

## Decisiones y límites

- La base Room pasa a la **versión 2** con una migración que borra solo las solicitudes simuladas anteriores (esquema sin cambios). Al compilar, Room genera `app/schemas/.../2.json`; subirlo con el commit.
- `usesCleartextTraffic` está activo porque los servicios locales usan HTTP. Para publicar de verdad habría que usar HTTPS.
- La sincronización no borra dispositivos locales que el servicio no tenga, para no perder evaluaciones.
- Reglas de keep de R8 en `app/src/main/keepRules/rules.keep` para que el APK release lea el JSON.
