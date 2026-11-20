# dispositivos-service · microservicio de dispositivos de prueba (TT-28)

Segundo microservicio propio de **TíoTec Preventivo** (el primero es [`solicitudes-service`](../solicitudes-service/README.md)). Guarda los dispositivos ficticios que se evalúan en la app: nombre y sistema operativo. La foto del dispositivo se queda en el teléfono. Spring Boot 3.5 (Java 17+), base de datos **MySQL** propia y API REST con CRUD completo.

> Igual que la app, el servicio **no recibe datos personales**: rechaza nombres con correos o números de 7 o más dígitos.

## Ejecutar

Mismos requisitos y variables que `solicitudes-service` (JDK 17+, Maven 3.9, MySQL 8; `DB_USER`, `DB_PASSWORD`, `DB_HOST`, `DB_PORT`).

```bash
cd backend/dispositivos-service
mvn spring-boot:run        # inicia en http://localhost:8082
mvn test                   # pruebas de los endpoints (H2 en memoria)
```

- Base **`tiotec_dispositivos`**, creada sola si no existe. Respaldo sin MySQL: `-Dspring-boot.run.profiles=h2` (consola en <http://localhost:8082/h2-console>, JDBC URL `jdbc:h2:file:./data/tiotec-dispositivos`).
- Desde el emulador Android: `http://10.0.2.2:8082/`.

## Arquitectura

```
cl.duoc.tiotec.dispositivos
├── controller/   DispositivoController   traduce HTTP ↔ servicio
├── service/      DispositivoService      reglas de negocio y transacciones
├── repository/   DispositivoRepository   Spring Data JPA
├── model/        Dispositivo, SistemaOperativo
├── dto/          DispositivoRequest, ArchivadoRequest, DispositivoResponse
└── exception/    ApiExceptionHandler     un solo formato de error
```

### Entidad

| Entidad | Campos |
|---|---|
| `Dispositivo` | `id`, `nombre` (único sin distinguir mayúsculas), `sistemaOperativo` (`ANDROID`, `IOS`, `WINDOWS`, `MACOS`, `OTRO`), `archivado`, `creadoEn`, `actualizadoEn` |

Las fechas son milisegundos desde epoch, igual que en Room. **Reglas:** el nombre no se repite (409); un dispositivo archivado no se puede editar hasta desarchivarlo (409); archivar uno ya archivado, o desarchivar uno activo, devuelve 409.

## Endpoints

Base: `/api/dispositivos`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| POST | `/api/dispositivos` | Crea un dispositivo activo | 201 + `Location`, 400, 409 |
| GET | `/api/dispositivos?sistemaOperativo=&archivado=` | Lista, más recientes primero; filtros opcionales | 200, 400 |
| GET | `/api/dispositivos/{id}` | Detalle | 200, 404 |
| PUT | `/api/dispositivos/{id}` | Edita nombre y sistema (solo si no está archivado) | 200, 400, 404, 409 |
| PATCH | `/api/dispositivos/{id}/archivado` | Archiva (`true`) o desarchiva (`false`) | 200, 400, 404, 409 |
| DELETE | `/api/dispositivos/{id}` | Elimina el dispositivo | 204, 404 |

### Ejemplos

```bash
curl -i -X POST http://localhost:8082/api/dispositivos \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Telefono de Ana","sistemaOperativo":"ANDROID"}'
```
```json
{ "id": 1, "nombre": "Telefono de Ana", "sistemaOperativo": "ANDROID", "archivado": false,
  "creadoEn": 1763600000000, "actualizadoEn": 1763600000000 }
```

```bash
curl "http://localhost:8082/api/dispositivos?sistemaOperativo=ANDROID&archivado=false"
curl -X PUT http://localhost:8082/api/dispositivos/1 -H "Content-Type: application/json" \
  -d '{"nombre":"Telefono de Ana 2","sistemaOperativo":"IOS"}'
curl -X PATCH http://localhost:8082/api/dispositivos/1/archivado -H "Content-Type: application/json" \
  -d '{"archivado":true}'
curl -i -X DELETE http://localhost:8082/api/dispositivos/1
```

### Errores

Mismo formato que `solicitudes-service` (`timestamp`, `status`, `error`, `mensaje`, `ruta` y `campos` en validación): **400** campos o valores inválidos, **404** no existe, **409** nombre repetido o estado que no lo permite.

## Pruebas

`DispositivoControllerTest` (MockMvc + H2 en memoria): creación y normalización del nombre, validaciones por campo, correo/teléfono rechazados, sistema desconocido, nombre repetido sin distinguir mayúsculas, 404, filtros, edición, archivar/desarchivar y eliminación.
