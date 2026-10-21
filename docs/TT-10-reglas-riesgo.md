# TT-10 · Cuestionario local y reglas de riesgo

**Responsable:** Enzo Morales · **Revisión técnica del contenido:** Gerard Zambrano
**Alcance:** Evaluación Parcial 2 · lógica local, sin servidor ni red.
**Rúbrica:** IE 2.2.1 (lógica y estado separados de la UI).

> El resultado es una **orientación según las respuestas** de la persona. La app no revisa, no escanea y no accede a la configuración real del dispositivo.

## 1. Cuestionario

Las 12 preguntas del catálogo de TT-06 (`data/catalogo/CatalogoLocal.kt`) cubren las cuatro áreas del caso, 3 por categoría. Cada una está redactada para que **"Sí" sea la buena práctica** y se responde con **Sí / No / No lo sé**.

| Categoría | Preguntas | Práctica clave |
|---|---|---|
| Respaldos | RES-01, RES-02, RES-03 | RES-01 · tener copia de seguridad |
| Actualizaciones | ACT-01, ACT-02, ACT-03 | ACT-01 · instalar actualizaciones del sistema |
| Autenticación | AUT-01, AUT-02, AUT-03 | AUT-01 · bloqueo de pantalla · AUT-02 · verificación en dos pasos |
| Recuperación de cuentas | RCU-01, RCU-02, RCU-03 | RCU-01 · datos de recuperación actualizados |

Las **prácticas clave** (`Pregunta.clave = true`) son las que tienen recomendación de prioridad **Alta**: sin ellas, el resto de las prácticas de la categoría pierde casi todo su efecto.

## 2. Puntajes

Más puntaje = más riesgo.

| Respuesta | Pregunta normal | Práctica clave |
|---|---|---|
| Sí | 0 | 0 |
| No lo sé | 1 | 2 |
| No | 2 | 3 |

- "No lo sé" cuenta como riesgo (menor que "No") porque una práctica que no se puede confirmar probablemente no se está aplicando.
- Puntaje máximo por categoría: **7** (una clave) o **8** (Autenticación, dos claves).

## 3. Nivel por categoría

1. `porcentaje = puntaje × 100 / máximo de la categoría` (división entera).
2. Umbrales:

| Porcentaje | Nivel |
|---|---|
| 0 – 33 | Riesgo bajo |
| 34 – 66 | Riesgo medio |
| 67 – 100 | Riesgo alto |

3. **Piso por práctica clave:** si una práctica clave se responde con "No" o "No lo sé", la categoría queda **al menos en riesgo medio**, aunque el porcentaje sea menor.

**Nivel global:** el peor nivel entre las cuatro categorías (`NivelRiesgo` está ordenado de bajo a alto).

### Ejemplos (respuestas ficticias)

| Caso | Puntaje | % | Nivel |
|---|---|---|---|
| Respaldos: todo Sí | 0/7 | 0 | Bajo |
| Actualizaciones: solo ACT-02 = No | 2/7 | 28 | Bajo |
| Respaldos: solo RES-01 = No lo sé | 2/7 | 28 | **Medio** (piso por práctica clave) |
| Autenticación: solo AUT-01 = No | 3/8 | 37 | Medio |
| Respaldos: RES-02 y RES-03 = No | 4/7 | 57 | Medio |
| Recuperación: RCU-01 y RCU-02 = No | 5/7 | 71 | Alto |
| Cualquier categoría: todo No lo sé | 4/7 o 5/8 | 57 o 62 | Medio |
| Cualquier categoría: todo No | 7/7 u 8/8 | 100 | Alto |

## 4. Recomendaciones priorizadas

- Cada pregunta respondida con **No** o **No lo sé** activa su recomendación (regla de TT-06).
- Orden: **prioridad** (Alta → Media → Baja); dentro de la misma prioridad, primero la **categoría con más riesgo**; si empatan, el orden del catálogo.
- Cada recomendación trae `diasParaRevision` (7 / 14 / 30) para crear la acción en TT-13.

## 5. Respuestas incompletas

- `ReglasRiesgo.evaluar` devuelve `ResultadoReglas.Incompleta(faltantes)` si falta cualquier pregunta. **No calcula resultados parciales.**
- `faltantes` viene en el orden del cuestionario, para que la pantalla (TT-11) lleve a la primera pregunta pendiente.
- Las respuestas con IDs que no existen en el catálogo se ignoran y no cuentan como respondidas.

## 6. Explicación del resultado

Cada `ResultadoCalculado.explicacion` dice, en lenguaje simple:

- el nivel y cuántas buenas prácticas aplica la persona (por ejemplo, *"Riesgo medio en Respaldos: aplicas 1 de 3 buenas prácticas."*);
- si falta una práctica clave y por qué eso sube el nivel;
- cuántas respuestas fueron "No lo sé" y que se cuentan como riesgo hasta confirmarlas.

`ResultadoReglas.Completa.aviso` contiene el texto fijo que la pantalla de resultados (TT-12) debe mostrar: *"Este resultado es una orientación según tus respuestas. La app no revisa ni escanea tu dispositivo."* Además del color, la UI debe mostrar el texto del nivel y un ícono (criterio de TT-12).

## 7. Dónde está el código

| Archivo | Contenido |
|---|---|
| `domain/ReglasRiesgo.kt` | Reglas puras (sin Android ni Compose): `evaluar`, `preguntasFaltantes`, `resultadoCategoria`, umbrales y aviso. |
| `data/catalogo/CatalogoLocal.kt` | `PUNTAJES_NORMAL`, `PUNTAJES_CLAVE` y qué preguntas son clave. |
| `data/catalogo/CatalogoModelos.kt` | Campo `Pregunta.clave`. |

Uso previsto en TT-11/TT-12, desde el ViewModel:

```kotlin
when (val resultado = reglas.evaluar(respuestas)) {
    is ResultadoReglas.Incompleta -> // marcar error en la primera pregunta de resultado.faltantes
    is ResultadoReglas.Completa -> evaluacionRepository.guardarEvaluacion(
        dispositivoId, respuestas, resultado.resultados
    )
}
```

`EvaluacionRepository` ya guarda el puntaje de cada respuesta con `pregunta.puntajes`, que son los mismos que usa `ReglasRiesgo`, y calcula el nivel global igual (peor categoría).

## 8. Pruebas

`app/src/test/java/cl/duoc/tiotecpreventivo/domain/ReglasRiesgoTest.kt` (JVM, sin emulador) comprueba con respuestas ficticias:

- todo Sí → bajo, sin recomendaciones; todo No → alto, todas las recomendaciones; todo "No lo sé" → medio;
- práctica normal sin aplicar → sigue bajo; práctica clave con No o "No lo sé" → al menos medio; clave + otra → alto;
- caso mixto con las cuatro categorías en distintos niveles y nivel global = el peor;
- orden de recomendaciones por prioridad y luego por categoría con más riesgo;
- respuestas incompletas y vacías → `Incompleta` con las faltantes en orden; IDs desconocidos ignorados;
- umbrales exactos (33/34 y 66/67), puntajes iguales a los del catálogo y explicación con aviso de orientación.

`CatalogoLocalTest` suma: puntajes crecientes Sí < No lo sé < No, y cada práctica clave corresponde a una recomendación de prioridad Alta.

```bash
./gradlew :app:testDebugUnitTest --tests "cl.duoc.tiotecpreventivo.domain.*" --tests "cl.duoc.tiotecpreventivo.data.catalogo.*"
```

## 9. Revisión técnica del contenido (Gerard)

- [ ] Las 5 prácticas clave son las correctas para un usuario no especialista.
- [ ] Los umbrales dan resultados razonables en los ejemplos de la sección 3.
- [ ] Las explicaciones se entienden sin conocimientos técnicos.
- [ ] El aviso de orientación es claro.
