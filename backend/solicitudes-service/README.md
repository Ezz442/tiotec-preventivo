# solicitudes-service · microservicio de solicitudes de asistencia (TT-28)

Primer microservicio propio de **TíoTec Preventivo** (el segundo es [`dispositivos-service`](../dispositivos-service/README.md)), hecho en Spring Boot 3.5 (Java 17+), con base de datos **MySQL** propia y API REST con CRUD completo. Reemplaza el almacenamiento local de las *solicitudes de asistencia* de la app: el alias y la categoría viajan al servicio y el servicio guarda el estado y el historial.

> Igual que la app, el servicio **no recibe datos personales**: solo un alias ficticio y una categoría. Rechaza correos y números de 7 o más dígitos (teléfonos, RUT).

## Ejecutar

Requisitos: JDK 17 o superior, Maven 3.9 y **MySQL 8** en marcha (o abrir la carpeta en IntelliJ / VS Code / Spring Tools).

```bash
# Windows PowerShell; en Linux/macOS usar export DB_USER=root
$env:DB_USER = "root"
$env:DB_PASSWORD = "<tu clave de MySQL>"    # nunca se escribe en el repositorio

cd backend/solicitudes-service
mvn spring-boot:run        # inicia en http://localhost:8081
mvn test                   # pruebas de los endpoints (H2 en memoria, no necesitan MySQL)
```

- La base **`tiotec_solicitudes`** se crea sola si no existe (`createDatabaseIfNotExist=true`) y las tablas las genera Hibernate (`ddl-auto=update`). Se pueden ver y consultar desde MySQL Workbench o DBeaver.
- Variables opcionales: `DB_HOST` (por defecto `localhost`), `DB_PORT` (`3306`), `DB_USER` (`root`), `DB_PASSWORD` (vacía).
- **Respaldo sin MySQL** (por ejemplo, si el equipo de la defensa no lo tiene): `mvn spring-boot:run -Dspring-boot.run.profiles=h2` usa H2 en archivo, con consola en <http://localhost:8081/h2-console> (JDBC URL `jdbc:h2:file:./data/tiotec-solicitudes`, usuario `sa`).
- **Desde el emulador Android** la URL base es `http://10.0.2.2:8081/` (el emulador ve el computador como `10.0.2.2`). Al ser HTTP, la app necesita `android:usesCleartextTraffic="true"` o una `network_security_config` para ese host. En un teléfono físico se usa la IP del computador en la misma red Wi-Fi.

## Arquitectura

```
cl.duoc.tiotec.solicitudes
├── controller/   SolicitudController      traduce HTTP ↔ servicio
├── service/      SolicitudService         reglas de negocio y transacciones
├── repository/   SolicitudRepository      Spring Data JPA
├── model/        Solicitud, HistorialSolicitud, Categoria, EstadoSolicitud   (entidades JPA)
├── dto/          SolicitudRequest, CambioEstadoRequest, SolicitudResponse, HistorialResponse
└── exception/    ApiExceptionHandler      un solo formato de error para toda la API
```

Flujo de una petición: `Controller` valida el cuerpo (`@Valid`) → `Service` aplica las reglas y guarda con `Repository` → se responde con un DTO (las entidades nunca salen por la API).

### Entidades

| Entidad | Campos | Relación |
|---|---|---|
| `Solicitud` | `id`, `codigo` (DEMO-001…, único), `alias`, `categoria`, `estado`, `creadaEn`, `actualizadaEn` | 1 → N `HistorialSolicitud` (borrado en cascada) |
| `HistorialSolicitud` | `id`, `estado`, `fecha`, `nota` | N → 1 `Solicitud` |

`categoria`: `RESPALDOS`, `ACTUALIZACIONES`, `AUTENTICACION`, `RECUPERACION`. `estado`: `PENDIENTE`, `EN_REVISION`, `RESUELTA`, `CANCELADA` (mismos nombres que los enums de la app). Las fechas son milisegundos desde epoch, igual que en Room.

**Transiciones de estado:** `PENDIENTE → EN_REVISION | CANCELADA`, `EN_REVISION → RESUELTA | CANCELADA`; `RESUELTA` y `CANCELADA` son finales. Solo se pueden editar los datos mientras está `PENDIENTE`.

## Endpoints

Base: `/api/solicitudes`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| POST | `/api/solicitudes` | Crea una solicitud (estado `PENDIENTE`, código automático) | 201 + `Location`, 400 |
| GET | `/api/solicitudes?categoria=&estado=` | Lista, más recientes primero; filtros opcionales | 200, 400 si el valor del filtro no existe |
| GET | `/api/solicitudes/{id}` | Detalle con historial | 200, 404 |
| GET | `/api/solicitudes/{id}/historial` | Solo el historial | 200, 404 |
| PUT | `/api/solicitudes/{id}` | Edita alias y categoría (solo si está `PENDIENTE`) | 200, 400, 404, 409 |
| PATCH | `/api/solicitudes/{id}/estado` | Cambia el estado y agrega un registro al historial | 200, 400, 404, 409 |
| DELETE | `/api/solicitudes/{id}` | Elimina la solicitud y su historial | 204, 404 |

### Ejemplos

```bash
# Crear
curl -i -X POST http://localhost:8081/api/solicitudes \
  -H "Content-Type: application/json" \
  -d '{"alias":"Tio Pepe","categoria":"RESPALDOS"}'
```
```json
{
  "id": 1, "codigo": "DEMO-001", "alias": "Tio Pepe", "categoria": "RESPALDOS",
  "estado": "PENDIENTE", "creadaEn": 1763600000000, "actualizadaEn": 1763600000000,
  "historial": [ { "id": 1, "estado": "PENDIENTE", "fecha": 1763600000000, "nota": "Solicitud creada" } ]
}
```

```bash
# Listar con filtros
curl "http://localhost:8081/api/solicitudes?categoria=RESPALDOS&estado=PENDIENTE"

# Cambiar estado (la nota es opcional, máx. 100 caracteres)
curl -X PATCH http://localhost:8081/api/solicitudes/1/estado \
  -H "Content-Type: application/json" -d '{"estado":"EN_REVISION","nota":"Revisando"}'

# Editar / eliminar
curl -X PUT http://localhost:8081/api/solicitudes/1 \
  -H "Content-Type: application/json" -d '{"alias":"Tio Pepe 2","categoria":"AUTENTICACION"}'
curl -i -X DELETE http://localhost:8081/api/solicitudes/1
```

### Errores

Todos los errores usan el mismo formato; `campos` aparece solo en errores de validación:

```json
{ "timestamp": 1763600000000, "status": 400, "error": "Bad Request",
  "mensaje": "Hay campos con errores.", "ruta": "/api/solicitudes",
  "campos": { "alias": "El alias debe tener entre 2 y 30 caracteres.", "categoria": "Selecciona una categoria." } }
```

| Código | Cuándo |
|---|---|
| 400 | Campos inválidos, categoría/estado inexistente en el cuerpo o en un filtro |
| 404 | La solicitud no existe |
| 409 | Transición de estado no permitida o edición de una solicitud que ya no está pendiente |

## Pruebas

`SolicitudControllerTest` prueba de punta a punta (MockMvc + H2 en memoria): creación con código e historial inicial, validaciones por campo (alias corto, correo, teléfono, categoría inexistente), 404, filtros, transiciones de estado e historial, edición solo en `PENDIENTE` y eliminación.

## Decisiones técnicas

- **MySQL, una base por microservicio**: `tiotec_solicitudes` pertenece solo a este servicio; ningún otro servicio la lee. H2 queda como perfil de respaldo y para las pruebas.
- **Credenciales por variables de entorno**: no hay claves en el repositorio.
- **DTOs separados de las entidades**: la API no expone el modelo de persistencia y las validaciones viven en el DTO.
- **Reglas de negocio en el servicio**: el controlador no decide nada; las transiciones están en `EstadoSolicitud`.
- **`Clock` inyectable**: permite fijar la hora en pruebas.
- **`open-in-view` desactivado**: la lectura del historial ocurre dentro de la transacción del servicio.
