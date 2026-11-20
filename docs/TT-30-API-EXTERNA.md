# TT-30 — Consumo de API externa con Retrofit

**API:** JSONPlaceholder, `GET https://jsonplaceholder.typicode.com/posts?_limit=10` (pública, sin clave, solo lectura).
**Dónde se ve:** Aprende → botón "Consejos de la comunidad (API externa)" → pantalla Comunidad.

## Estructura (de la red a la pantalla)
| Capa | Archivo |
|---|---|
| Interfaz Retrofit | `data/remote/PostsApi.kt` |
| DTO (JSON) | `data/remote/model/PostDto.kt` |
| Manejo de errores | `data/remote/LlamadaApiExterna.kt` (reusa `ErrorRemoto`) |
| Repositorio | `repository/PostsRepository.kt` (DTO → `ConsejoComunidad`) |
| ViewModel | `viewmodel/PostsViewModel.kt`, `PostsUiState.kt`, `PostsViewModelFactory.kt` |
| UI | `ui/screens/ComunidadScreen.kt` (cargando, error + Reintentar, vacío, lista) |
| Cableado | `AppContainer.postsRepository`, `ConfigRed.URL_API_EXTERNA`, ruta `comunidad` en `TioTecApp` |

El cliente se crea con el mismo `ServiciosRemotos.crearApi(...)` que los microservicios (mismo OkHttp y tiempos de espera).

## API externa vs. microservicio propio
| | API externa (TT-30) | Microservicios (TT-28/29) |
|---|---|---|
| Quién la controla | un tercero (JSONPlaceholder) | nosotros |
| Datos | ajenos, de solo lectura | de TíoTec (solicitudes, dispositivos) |
| Persistencia | ninguna (memoria) | MySQL + copia en Room |
| Si falla | mensaje y Reintentar; el resto de la app sigue igual | banner de sin conexión; se muestra la copia local |
| Formato de error | desconocido → mensajes propios por código | `ErrorApiDto` propio |
| Red | HTTPS | HTTP local (`adb reverse`) |

No interfiere con lo local: no usa Room ni los servicios, y si no hay internet solo falla esta pantalla.

## Pruebas
`PostsRepositoryTest` (conversión, vacío, sin red, 404/429/5xx, JSON malo) y `PostsViewModelTest` (carga, vacío, error, reintento), con APIs falsas, sin red real.
