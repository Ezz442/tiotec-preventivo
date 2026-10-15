# TT-03: Diseño de pantallas y navegación

## Responsables

- Diseño: Gerard Zambrano.
- Revisión: Enzo Morales.
- Estado: propuesta pendiente de revisión.

## Objetivo

Diseñar una aplicación Android sencilla que permita evaluar hábitos de
seguridad digital, comprender los riesgos y realizar acciones preventivas.

Todos los perfiles, dispositivos y solicitudes utilizados serán ficticios.

## Navegación principal

La aplicación tendrá cuatro destinos principales:

- Inicio.
- Mis acciones.
- Aprende.
- Perfil.

Las pantallas secundarias tendrán un botón para volver.
Al volver desde un detalle se conservará el contexto de la pantalla anterior.

## Pantallas

| Pantalla | Contenido y acciones | Acceso |
|---|---|---|
| Inicio | Última evaluación, cantidad de acciones pendientes y botón Evaluar dispositivo. Accesos a dispositivos e historial. | Navegación principal. |
| Perfil | Alias ficticio y nivel de experiencia digital. Crear y editar perfil. | Navegación principal. |
| Dispositivos | Lista de dispositivos ficticios con nombre y sistema operativo. Agregar, editar y seleccionar un dispositivo para evaluarlo. | Inicio. |
| Evaluación | Preguntas sobre respaldos, actualizaciones, autenticación y recuperación de cuentas. Progreso, Anterior, Siguiente y Finalizar. | Evaluar dispositivo, después de seleccionar un equipo. |
| Resultados | Riesgo por categoría, explicación sencilla y recomendaciones ordenadas por prioridad. Botón Ver mis acciones. | Al finalizar una evaluación o consultar el historial. |
| Mis acciones | Acciones con prioridad, estado y fecha de revisión. Filtros Pendientes y Completadas. Permitir cambiar el estado. | Navegación principal y Resultados. |
| Historial | Evaluaciones ordenadas por fecha, con dispositivo y resumen. Acceso al detalle de cada evaluación. | Inicio. |
| Aprende | Contenidos educativos agrupados por categoría. Cada contenido abre una pantalla de lectura. | Navegación principal y recomendaciones. |
| Recordatorios | Lista de recordatorios. Crear, modificar y cancelar avisos asociados a una acción o próxima evaluación. | Perfil y detalle de una acción. |
| Asistencia simulada | Crear solicitudes ficticias con categoría. Consultar fecha, estado y detalle. Aviso visible de simulación. | Perfil. |
| Administración | Listar y gestionar categorías, preguntas, recomendaciones y contenidos educativos. | Selector de rol de demostración en Perfil. |
| Revisión técnica | Consultar contenidos y criterios, registrar observaciones y estado de revisión. | Selector de rol de demostración en Perfil. |

## Primer uso

1. Mostrar una bienvenida breve.
2. Explicar que se utilizarán datos ficticios.
3. Crear un perfil con alias y nivel de experiencia digital.
4. Ir a Inicio.
5. Ofrecer registrar el primer dispositivo de prueba.

No se solicitarán contraseñas reales, documentos de identidad ni datos sensibles.

## Recorrido principal

1. Inicio: pulsar Evaluar dispositivo.
2. Seleccionar un dispositivo registrado o agregar uno.
3. Responder la evaluación preventiva.
4. Revisar las respuestas antes de finalizar.
5. Guardar la evaluación y mostrar sus resultados.
6. Consultar las recomendaciones y las acciones priorizadas.
7. Marcar acciones como completadas.
8. Programar una próxima revisión.
9. Consultar la evaluación desde Historial.

Si el guardado falla, conservar las respuestas y ofrecer Reintentar.
No mostrar una evaluación como guardada hasta confirmar la operación.

## Reglas de navegación

- Sin perfil, guiar primero a la creación del perfil ficticio.
- Sin dispositivos, ofrecer Agregar dispositivo antes de evaluar.
- Al retroceder entre preguntas, conservar las respuestas.
- Al salir de una evaluación sin finalizar, pedir confirmación.
- Consultar un resultado histórico no debe crear una nueva evaluación.
- Desde una recomendación se podrá abrir el contenido educativo relacionado.
- Al volver de ese contenido se regresará al resultado original.
- Los roles de administrador y supervisor serán de demostración.
- El alcance de esos roles se confirmará con el docente y la dupla.

## Estados de interfaz

| Estado | Qué verá el usuario | Acción disponible |
|---|---|---|
| Cargando | Indicador de progreso y texto breve. | Esperar sin duplicar solicitudes. |
| Sin dispositivos | Todavía no tienes dispositivos de prueba. | Agregar dispositivo. |
| Sin evaluaciones | Aún no has realizado una evaluación. | Comenzar evaluación. |
| Sin acciones pendientes | No tienes acciones pendientes. | Consultar completadas o realizar otra evaluación. |
| Sin contenidos | No hay contenido disponible en esta categoría. | Volver a las categorías. |
| Sin recordatorios | Aún no tienes recordatorios. | Crear recordatorio. |
| Sin solicitudes | No has creado solicitudes simuladas. | Crear solicitud. |
| Error de conexión | No pudimos conectar. Inténtalo nuevamente. | Reintentar. |
| Datos locales disponibles | Mostrando información guardada en este dispositivo. | Consultar los datos disponibles. |
| Formulario incompleto | Mensaje junto al campo que requiere corrección. | Corregir el campo. |
| Notificaciones desactivadas | Los recordatorios no podrán mostrar avisos. | Continuar y consultar cómo habilitarlos. |
| Guardado correcto | Confirmación breve de la operación. | Continuar. |

## Criterios visuales y de accesibilidad

- Usar Material Design 3.
- Utilizar fondos claros, texto oscuro y un color principal azul.
- Mantener títulos, botones y espacios consistentes entre pantallas.
- Acompañar los iconos principales con texto.
- Mostrar el riesgo mediante texto e iconos, además del color.
- Usar etiquetas como Riesgo alto, Riesgo medio y Riesgo bajo.
- Permitir que el contenido se adapte al aumento del tamaño de texto.
- Evitar que el teclado oculte los campos y botones del formulario.
- Presentar instrucciones breves y comprensibles.
- Usar desplazamiento vertical cuando el contenido no quepa.
- Comprobar contraste, lectura y facilidad para pulsar los controles.

## Evidencia pendiente

- Bocetos de las pantallas y sus estados principales.
- Revisión del recorrido completo.
- Enlace al pull request de TT-03.
- Revisión de Enzo Morales.