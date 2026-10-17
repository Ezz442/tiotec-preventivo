# TT-03: Diseño de pantallas y navegación

## Responsables

* Diseño: Gerard Zambrano.
* Revisión: Enzo Morales.
* Estado: propuesta pendiente de revisión.

## Objetivo

Diseñar una aplicación Android sencilla que permita evaluar hábitos de
seguridad digital, comprender los riesgos y realizar acciones preventivas.

Todos los perfiles, dispositivos y solicitudes utilizados serán ficticios.

## Navegación principal

La aplicación tendrá cuatro destinos principales:

* Inicio.
* Mis acciones.
* Aprende.
* Perfil.

Las pantallas secundarias tendrán un botón para volver.
Al volver desde un detalle se conservará el contexto de la pantalla anterior.

## Pantallas

|Pantalla|Contenido y acciones|Acceso|
|-|-|-|
|Inicio|Última evaluación, cantidad de acciones pendientes y botón Evaluar dispositivo. Accesos a dispositivos e historial.|Navegación principal.|
|Perfil|Alias ficticio y nivel de experiencia digital. Crear y editar perfil.|Navegación principal.|
|Preferencias |Elegir tema y activar o desactivar la preferencia de avisos. Conservar las opciones al cerrar y abrir la app. Explicar cuando falte el permiso de notificaciones.|Perfil.|
|Dispositivos|Lista de dispositivos ficticios. Agregar, editar y eliminar con confirmación. Formulario con nombre, sistema operativo e imagen opcional mediante cámara o galería. Mostrar errores junto a cada campo.<br />|Inicio.|
|Evaluación|Preguntas sobre respaldos, actualizaciones, autenticación y recuperación de cuentas. Progreso, Anterior, Siguiente y Finalizar.|Evaluar dispositivo, después de seleccionar un equipo.|
|Resultados|Riesgo por categoría, explicación sencilla y recomendaciones ordenadas por prioridad. Botón Ver mis acciones.|Al finalizar una evaluación o consultar el historial.|
|Mis acciones|Acciones con prioridad, estado y fecha de revisión. Filtros Pendientes y Completadas. Permitir cambiar el estado.|Navegación principal y Resultados.|
|Historial|Evaluaciones ordenadas por fecha, con dispositivo y resumen. Acceso al detalle de cada evaluación.|Inicio.|
|Aprende|Contenidos educativos agrupados por categoría. Cada contenido abre una pantalla de lectura.|Navegación principal y recomendaciones.|
|Recordatorios|Lista de recordatorios. Crear, modificar y cancelar avisos asociados a una acción o próxima evaluación.|Perfil y detalle de una acción.|
|Asistencia simulada|Crear solicitudes ficticias con categoría. Consultar fecha, estado y detalle. Aviso visible de simulación.|Perfil.|

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

* Sin perfil, guiar primero a la creación del perfil ficticio.
* Sin dispositivos, ofrecer Agregar dispositivo antes de evaluar.
* Al retroceder entre preguntas, conservar las respuestas.
* Al salir de una evaluación sin finalizar, pedir confirmación.
* Consultar un resultado histórico no debe crear una nueva evaluación.
* Desde una recomendación se podrá abrir el contenido educativo relacionado.
* Al volver de ese contenido se regresará al resultado original.
* La evaluación y consulta de datos funcionarán sin conexión a internet.
* Cancelar la cámara o galería conservará los datos del formulario y la imagen anterior.
* La imagen del dispositivo será opcional y podrá reemplazarse o quitarse.
* Eliminar un dispositivo requerirá confirmación.
* Las preferencias se conservarán al cerrar y volver a abrir la app.

## Estados de interfaz

|Estado|Qué verá el usuario|Acción disponible|
|-|-|-|
|Cargando|Indicador de progreso y texto breve.|Esperar sin duplicar solicitudes.|
|Sin dispositivos|Todavía no tienes dispositivos de prueba.|Agregar dispositivo.|
|Sin evaluaciones|Aún no has realizado una evaluación.|Comenzar evaluación.|
|Sin acciones pendientes|No tienes acciones pendientes.|Consultar completadas o realizar otra evaluación.|
|Sin contenidos|No hay contenido disponible en esta categoría.|Volver a las categorías.|
|Sin recordatorios|Aún no tienes recordatorios.|Crear recordatorio.|
|Sin solicitudes|No has creado solicitudes simuladas.|Crear solicitud.|
|Error de guardado local|No pudimos guardar los cambios. Tus datos del formulario se conservan.|Reintentar.|
|Datos locales disponibles|Mostrando información guardada en este dispositivo.|Consultar los datos disponibles.|
|Formulario incompleto|Mensaje junto al campo que requiere corrección.|Corregir el campo.|
|Notificaciones desactivadas|Los recordatorios no podrán mostrar avisos.|Continuar y consultar cómo habilitarlos.|
|Guardado correcto|Confirmación breve de la operación.|Continuar.|
|Sin imagen|Imagen predeterminada y botones Tomar foto y Elegir de galería.|Agregar imagen o continuar sin ella.|
|Captura o selección cancelada|Mantener el formulario y la imagen anterior, si existe.<br />|Continuar o intentar nuevamente.|
|Cámara no disponible o acceso denegado|Explicación breve sin bloquear el formulario.|Elegir de galería o continuar sin imagen.|

## Criterios visuales y de accesibilidad

* Usar Material Design 3.
* Utilizar fondos claros, texto oscuro y un color principal azul.
* Mantener títulos, botones y espacios consistentes entre pantallas.
* Acompañar los iconos principales con texto.
* Mostrar el riesgo mediante texto e iconos, además del color.
* Usar etiquetas como Riesgo alto, Riesgo medio y Riesgo bajo.
* Permitir que el contenido se adapte al aumento del tamaño de texto.
* Evitar que el teclado oculte los campos y botones del formulario.
* Presentar instrucciones breves y comprensibles.
* Usar desplazamiento vertical cuando el contenido no quepa.
* Comprobar contraste, lectura y facilidad para pulsar los controles.



## Animaciones previstas



- Mostrar y ocultar mensajes de validación con AnimatedVisibility.
- Animar el progreso al avanzar en la evaluación.
- Animar el cambio visual de una acción pendiente a completada.
- Mostrar indicadores de carga únicamente durante operaciones reales.

## Adaptación a tamaños de pantalla

- Diseñar primero para tamaño Compact.
- En Compact, utilizar NavigationBar para los cuatro destinos principales.
- En Medium y Expanded, distribuir el contenido aprovechando el espacio y utilizar NavigationDrawer.
- En Compose, consultar calculateWindowSizeClass una vez por pantalla.
- Mantener formularios accesibles con teclado, texto aumentado y desplazamiento vertical.

## Evidencia pendiente de TT-27

- Bocetos actualizados al alcance local de EP2.
- Estados de validación, cámara, galería y preferencias representados.
- Revisión del recorrido completo.
- Enlace al pull request de TT-27.
- Revisión de Enzo Morales.
