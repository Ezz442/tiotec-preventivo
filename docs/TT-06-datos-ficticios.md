# TT-06 · Catálogo local y datos ficticios

**Responsable:** Enzo Morales · **Revisión:** Gerard Zambrano
**Alcance:** Evaluación Parcial 2 · datos locales, sin servidor ni red.
**Base:** sección 3 de `docs/TT-04-modelo-local.md`.

> Todos los datos son de prueba. El catálogo orienta con buenas prácticas generales; no es un diagnóstico ni un escaneo real del dispositivo.

## 1. Qué contiene

| Elemento | Cantidad | Archivo |
|---|---|---|
| Categorías | 4 | `data/catalogo/CatalogoModelos.kt` (enum `Categoria`) |
| Preguntas | 12 (3 por categoría) | `data/catalogo/CatalogoLocal.kt` |
| Recomendaciones | 12 (1 por pregunta) | `data/catalogo/CatalogoLocal.kt` |
| Contenidos educativos | 8 (2 por categoría) | `data/catalogo/CatalogoLocal.kt` |

Acceso: interfaz `repository/CatalogoRepository.kt`, implementada por `data/catalogo/CatalogoRepositoryLocal.kt`. Los ViewModels usan solo la interfaz.

### Preguntas

| ID | Categoría | Pregunta | Recomendación | Prioridad |
|---|---|---|---|---|
| RES-01 | Respaldos | ¿Tienes una copia de seguridad de tus fotos y documentos importantes? | REC-RES-01 | Alta |
| RES-02 | Respaldos | ¿Tus copias de seguridad se hacen de forma automática? | REC-RES-02 | Media |
| RES-03 | Respaldos | ¿Has comprobado alguna vez que puedes recuperar un archivo desde tu copia? | REC-RES-03 | Baja |
| ACT-01 | Actualizaciones | ¿Instalas las actualizaciones del sistema cuando el dispositivo las ofrece? | REC-ACT-01 | Alta |
| ACT-02 | Actualizaciones | ¿Tienes activadas las actualizaciones automáticas de las aplicaciones? | REC-ACT-02 | Media |
| ACT-03 | Actualizaciones | ¿Instalas aplicaciones solo desde la tienda oficial de tu dispositivo? | REC-ACT-03 | Media |
| AUT-01 | Autenticación | ¿Tu dispositivo pide PIN, patrón, contraseña o huella para desbloquearse? | REC-AUT-01 | Alta |
| AUT-02 | Autenticación | ¿Tienes activada la verificación en dos pasos en tu correo principal? | REC-AUT-02 | Alta |
| AUT-03 | Autenticación | ¿Usas una contraseña distinta para cada cuenta importante? | REC-AUT-03 | Media |
| RCU-01 | Recuperación | ¿Tu correo principal tiene un teléfono o correo de recuperación actualizado? | REC-RCU-01 | Alta |
| RCU-02 | Recuperación | ¿Guardaste en un lugar seguro los códigos de respaldo de tu verificación en dos pasos? | REC-RCU-02 | Media |
| RCU-03 | Recuperación | ¿Sabes qué pasos seguir si pierdes el acceso a tu cuenta principal? | REC-RCU-03 | Baja |

## 2. Origen de los datos

- Los textos fueron redactados por el equipo a partir de las cuatro áreas preventivas del caso *TíoTec Preventivo* (respaldos, actualizaciones, autenticación reforzada y recuperación de cuentas) y de recomendaciones generales de seguridad para usuarios no especialistas.
- No se copian textos de terceros ni se usan datos de personas, dispositivos o incidentes reales.
- La revisión técnica del contenido la hacen ambos integrantes en el PR (reemplaza al rol de referente técnico, retirado de EP2).

## 3. Reglas

| Regla | Detalle |
|---|---|
| Datos en código, no en Room | El catálogo no lo edita el usuario. Al vivir en el código no se carga en la base de datos, por lo que **no se duplica ni se borra al reabrir** la app, y no toca los avances guardados en Room (TT-07). |
| IDs estables | Preguntas `RES/ACT/AUT/RCU-NN`; recomendaciones `REC-` + ID de su pregunta; contenidos `CON-` + prefijo de categoría + `-NN`. Un ID publicado no se cambia ni se reutiliza, porque Room guarda `preguntaId` y `recomendacionId` en el historial. Se usa `RCU` (y no `REC`) para Recuperación, para no confundirlo con el prefijo de recomendaciones. |
| Redacción | Cada pregunta está escrita para que **"Sí" sea la buena práctica**. Lenguaje simple, segunda persona, sin tecnicismos innecesarios. |
| Activación | Cada recomendación se activa con **"No" o "No lo sé"**; nunca con "Sí". |
| Puntajes | Definidos en **TT-10** (`docs/TT-10-reglas-riesgo.md`): normal Sí = 0, No lo sé = 1, No = 2; práctica clave Sí = 0, No lo sé = 2, No = 3. |
| Prioridad y revisión | Alta = 7 días, Media = 14 días, Baja = 30 días hasta la fecha de revisión de la acción. |
| Orden | Recomendaciones: Alta → Media → Baja, luego por categoría y orden de la pregunta. Preguntas: por categoría y luego por `orden`. |
| Contenido enlazado | Cada recomendación apunta a un contenido de **su misma categoría** (botón *Aprender cómo hacerlo*). |
| Sin datos personales | No se piden ni se guardan nombres reales, contraseñas, números ni correos. |

## 4. Uso sin conexión

- El catálogo se compila dentro de la app: no hay servidor, URL ni llamadas de red.
- `AndroidManifest.xml` **no declara** el permiso `android.permission.INTERNET`, así que la app no puede conectarse aunque quisiera.
- Verificación manual: activar modo avión en el emulador y recorrer Evaluación, Resultados y Aprende cuando estén implementadas (TT-11, TT-12, TT-15).

## 5. Pruebas

`app/src/test/java/cl/duoc/tiotecpreventivo/data/catalogo/CatalogoLocalTest.kt` comprueba:

- IDs únicos y con la convención; cada categoría con preguntas y contenidos; órdenes sin repetir.
- Cada pregunta con puntaje para las tres respuestas y "Sí" = 0.
- Cada pregunta con su recomendación; cada recomendación apunta a una pregunta y a un contenido existentes de la misma categoría.
- `recomendacionesPara`: todo "Sí" → ninguna; todo "No" → todas, ordenadas por prioridad; "No lo sé" también activa; IDs desconocidos se ignoran.
- Dos aperturas de la app devuelven el mismo catálogo, sin duplicados.
- El manifiesto no pide acceso a internet.

Ejecutar en Android Studio: clic derecho sobre `CatalogoLocalTest` → **Run**, o en la terminal:

```bash
./gradlew :app:testDebugUnitTest --tests "cl.duoc.tiotecpreventivo.data.catalogo.*"
```

## 6. Qué usa cada tarea

| Tarea | Usa |
|---|---|
| TT-10 | Puntajes definitivos, prácticas clave y `domain/ReglasRiesgo.kt` |
| TT-11 | `preguntas()` / `preguntasDe(categoria)` |
| TT-12 | `recomendacionesPara(respuestas)` |
| TT-13 | `prioridad` y `diasParaRevision` para crear acciones |
| TT-15 | `contenidos(categoria)` / `contenido(id)` |
