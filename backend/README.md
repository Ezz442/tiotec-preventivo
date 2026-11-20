# TíoTec · microservicios propios (TT-28)

Dos microservicios Spring Boot 3.5 (Java 17+), cada uno con su propia base de datos MySQL y sin dependencias entre sí. La app Android los consume con Retrofit (TT-29).

| Microservicio | Puerto | Base de datos | Recurso | Detalle |
|---|---|---|---|---|
| [`solicitudes-service`](solicitudes-service/README.md) | 8081 | `tiotec_solicitudes` | `/api/solicitudes` (con historial de estados) | Gerard |
| [`dispositivos-service`](dispositivos-service/README.md) | 8082 | `tiotec_dispositivos` | `/api/dispositivos` | Enzo |

**Por qué dos servicios y no uno:** cada recurso tiene su propio ciclo de vida y reglas (estados y transiciones en solicitudes; nombre único y archivado en dispositivos), se despliegan y prueban por separado y ninguno lee la base del otro.

**Ejecutar ambos:** definir `DB_USER` y `DB_PASSWORD`, abrir dos terminales y correr `mvn spring-boot:run` en cada carpeta. Desde `backend/`, `mvn test` prueba los dos. Desde el emulador Android: `http://10.0.2.2:8081/` y `http://10.0.2.2:8082/`.

**Base de datos:** MySQL 8. Las bases se crean solas y las credenciales se leen de variables de entorno, nunca del repositorio. Si el equipo de la defensa no tiene MySQL, ambos servicios parten con H2 usando `-Dspring-boot.run.profiles=h2`.
