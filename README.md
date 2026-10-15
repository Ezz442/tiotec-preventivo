# TíoTec Preventivo

MVP Android de prevención digital para personas no especialistas. La app guía una evaluación preventiva sobre el uso de dispositivos, muestra el nivel de riesgo por categoría con recomendaciones priorizadas y ayuda a dar seguimiento a las acciones con recordatorios e historial.

Proyecto en dupla para la asignatura **DSY1105**, basado en el caso *TíoTec Preventivo*.

> ⚠️ Todos los perfiles, dispositivos y solicitudes son **ficticios**. La app no realiza diagnósticos ni escaneos reales, y no almacena datos personales ni sensibles.

## Integrantes

| Integrante | GitHub |
|---|---|
| Enzo Morales | [@Ezz442](https://github.com/Ezz442) |
| Gerard Zambrano | _pendiente_ |

## Tecnologías

- Kotlin y Android Studio
- Jetpack Compose y Material Design 3
- Arquitectura MVVM
- Room (persistencia local)
- Retrofit (consumo de API REST)
- Notificaciones locales

## Funcionalidades previstas

- Perfil ficticio y registro de dispositivos de prueba
- Evaluación preventiva guiada por categorías
- Resultados de riesgo y recomendaciones priorizadas
- Checklist de acciones y seguimiento
- Historial de evaluaciones
- Contenidos educativos
- Recordatorios y notificaciones
- Asistencia simulada
- Gestión de contenidos (administrador) y revisión técnica

## Cómo ejecutar

1. Clonar el repositorio:
```bash
   git clone https://github.com/Ezz442/tiotec-preventivo.git
```
2. Abrir la carpeta en Android Studio y esperar la sincronización de Gradle.
3. Ejecutar la configuración `app` en un emulador o dispositivo (mínimo Android 7.0, API 24).

_La API de prueba y sus instrucciones de arranque se documentarán en TT-06._

## Flujo de trabajo

- `main` contiene siempre una versión estable que compila.
- Cada tarea se trabaja en una rama `feat/TT-XX-descripcion` o `fix/TT-XX-descripcion`.
- Los cambios se integran mediante Pull Request, revisado por el otro integrante.
- Las tareas se gestionan en Trello con el identificador TT-XX.

## Estado

🚧 En desarrollo. Proyecto Android base creado (TT-02).