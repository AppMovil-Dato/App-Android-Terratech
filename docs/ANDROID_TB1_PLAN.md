# Plan de desarrollo Android para TB1

Fecha: 4 de octubre de 2026, America/Lima. Estado: recorrido implementado. El cierre y sus evidencias se siguen en ANDROID_VALIDATION.md y ANDROID_TB1_MATRIX.md; validación física pendiente. Las fases originales se conservan como criterios de aceptación.

## Resultado que presentaremos

Registro → login → completar perfil → crear/elegir parcela → asociar sensor por código → consultar indicadores → histórico 7/30 días → detalle de lectura → volver a consultar los datos descargados sin conexión.

Historias: US06, US07, US09, US17, US11, US10, US12 y nueva HU de consulta sin conexión. El backend ya cubre el recorrido; ver [contrato](BACKEND_CONTRACT.md) y [OpenAPI](backend-openapi.snapshot.json). Almacenamiento y pantallas implementados; verificar el alcance real con la matriz y resultados actuales.

## Decisiones de arquitectura

Kotlin, Compose y Material 3; presentación MVVM con ViewModel, StateFlow y Coroutines. Un módulo Gradle `app` inicialmente, con contextos `iam`, `profile`, `monitoring` y `core` directamente bajo `com.novatech.terratech`. No crear contextos vacíos para funciones posteriores.

Cada contexto de negocio tendrá `presentation`, `application`, `domain` e `infrastructure`. Dominio y aplicación serán Kotlin puro, sin SDK Android, Room ni Retrofit. Interfaces de repositorio en dominio; implementación y mapeo en infraestructura. ViewModels llaman casos de uso; DTOs y entidades Room no cruzan hacia UI. Los datos con reglas de negocio tendrán value objects (Email, Password, SensorCode, AreaM2); la confirmación se valida como entrada del registro sin persistir contraseñas.

La guía local exige explícitamente value objects para variables con reglas de validación. Separaremos comandos (registrar, editar, asociar) y consultas (listar, leer) en casos de uso; no necesitamos un bus CQRS para TB1. Reutilizaremos patrones de EasyStore adaptados a los contextos de TerraTech. Su autenticación no está integrada y su repositorio transforma errores en listas vacías: no copiar ese comportamiento.

| Herramienta | Decisión y uso |
|---|---|
| Hilt + KSP | Inyección en infraestructura y presentación; repositorios enlazados mediante módulos |
| Retrofit + OkHttp + Gson | API TerraTech, interceptores de sesión y mapeo de Problem Details; misma estrategia JSON que EasyStore |
| Room + Flow | Fuente de datos observada por UI para parcelas, sensores y lecturas descargadas |
| DataStore | Preferencias, IDs seleccionados y metadatos ligeros; no guardar el token en texto plano |
| Android Keystore | Proteger material de sesión persistido; excluirlo de copias de seguridad y registros |
| Navigation Compose | Grafo de sesión y grafo principal, con selección y detalle |
| Compose Canvas | Gráfica de humedad y anillo; acompañarlos con valores, leyenda y detalle accesibles |
| WorkManager | Pospuesto: TB1 requiere lectura offline y actualización manual/al abrir, sin cola de escrituras |
| Coil | Solo si incorporamos imágenes reales; no necesario para indicadores ni formularios |

Las versiones están fijadas y verificadas en `libs.versions.toml`: AGP 9.4.1, SDK 37, Kotlin Compose 2.4.20, Compose BOM 2026.09.00 y JDK de daemon 25. Los resultados Debug se registran en ANDROID_VALIDATION.md. Todas las compilaciones y pruebas Android serán **Debug**.

## Interfaz y navegación

Mantener la identidad de [las referencias](design/VISUAL_DIRECTION.md), incluyendo colores, composición y jerarquía tipográfica. Material 3 servirá de base con tokens propios, sin colores dinámicos del sistema que sustituyan la paleta TerraTech.

Barra inferior: **Inicio, Parcelas, Perfil**. Inicio resume el sensor seleccionado, su última muestra y acceso al histórico. Parcelas lista parcelas propias, permite crear una y abre sus sensores. Perfil consulta/edita nombre, teléfono, fundo, ubicación y superficie. Registro de sensor dentro de la parcela; selección explícita cuando haya varios dispositivos.

| Pantalla | Contenido y estados esenciales |
|---|---|
| Registro / login | Nombre, correo, contraseña y confirmación exacta; errores por campo, duplicado, credenciales inválidas |
| Perfil inicial / edición | Datos personales y terreno; correo visible no editable; conversión ha ↔ m² sin cambiar la unidad del API |
| Parcelas / crear parcela | Cultivo, superficie y coordenadas manuales; cargando, vacío, error o caché |
| Sensores de parcela / asociar | Código TT-XXXXXX, nombre; selección; desconocido, ocupado, parcela ajena/inexistente |
| Inicio / detalle de sensor | Humedad %, temperatura del suelo °C y N/P/K ppm; fecha, SIMULATED y aviso >30 min |
| Histórico / detalle de lectura | Selector 7/30, gráfica temporal real, lista accesible y detalle por ID |
| Consulta sin conexión | Datos disponibles, fecha de medición y descarga, reintentar conexión, vacío si nunca se descargó |

Usar estados de carga, contenido, vacío, error y contenido local con aviso. No interpretar falta de red como cero sensores o cero humedad. Etiqueta visible de demostración para SIMULATED; no llamarlo telemetría en vivo ni mostrar conexión física verificada. El umbral mínimo es una referencia de humedad, no una recomendación agronómica.

## Datos offline y sesión

Room guarda parcelas, dispositivos y muestras con usuario propietario, IDs remotos y fechas UTC. Clave de lectura por usuario/dispositivo/lectura; metadatos `downloadedAt` y rango consultado. UI observa Room; actualización remota mapea y escribe transaccionalmente antes de emitir contenido. Descargar ambos rangos según navegación y conservar las muestras ya obtenidas: un histórico de siete días no debe borrar el de treinta.

Hay dos antigüedades distintas: `recordedAt` del sensor y `downloadedAt` de la caché. Recalcular el aviso >30 minutos usando la fecha del sensor y el reloj del dispositivo; no congelar el booleano descargado. Guardar/mostrar UTC internamente y convertir a hora local al presentar.

Sin red se permite leer lo previamente descargado por la sesión local, también tras reiniciar la app. Registro, login inicial, edición y asociaciones requieren red en TB1; no simular éxito ni encolar escrituras. Sin descarga anterior mostrar explicación y estado vacío. Vencimiento de JWT no borra inmediatamente la caché: al recuperar red, exigir autenticarse para actualizar si la sesión venció. Una respuesta 401 se distingue de fallo de conectividad y no permite nuevas solicitudes privadas.

Cerrar sesión elimina token, selección y caché privada. Al cambiar de cuenta no puede aparecer información de la anterior, ni brevemente. No persistir contraseñas, no imprimir JWT ni activar logs HTTP de credenciales. Tratar la restauración de sesión y la limpieza de caché como flujos probados, incluyendo fallos parciales.

## Orden de implementación y aceptación

| Fase | Trabajo | Se acepta cuando |
|---|---|---|
| 0. Base verificable | Validar SDK/JDK/Gradle, ensamblar plantilla Debug, fijar dependencias compatibles, Hilt, red, Room, navegación y configuración de API | assembleDebug, testDebugUnitTest y lintDebug pasan; configuration local no se versiona |
| 1. Sistema visual | Tokens, fuente empaquetada una vez identificada, tarjetas, campos, botones, indicadores, avisos, layouts y previews | Comparación con capturas, texto ampliado y contraste; sin valores ficticios en pantallas integradas |
| 2. Cuenta y perfil | US06/07/09, sesión, errores API, alta/edición del perfil | Recorrido con cuenta nueva, duplicado, confirmación desigual, 401 y persistencia del perfil |
| 3. Parcelas y sensores | US11/17, CRUD mínimo del recorrido, selección y registro | Dos cuentas aisladas, parcela vacía, código desconocido/ocupado y asociación válida |
| 4. Indicadores e histórico | US10/12, última muestra, rango 7/30, detalle, gráfica con datos reales | Unidades, UTC, orden, límites de rango, ausencia de lecturas y datos antiguos comprobados |
| 5. Offline y cierre | Room, consulta tras reiniciar/sin red, refresco, aislamiento, traducciones y accesibilidad | Flujo físico completo conectado y en modo avión, sin fuga entre cuentas; evidencia y APK Debug |

Cada fase entrega una porción demostrable y pruebas; no acumular todas las pruebas al final. La app no queda lista para TB1 solo porque compile o por tener un porcentaje de cobertura.

## Pruebas previstas

- Unitarias: validaciones/confirmación, conversión ha/m², mappers y códigos de error, límites 7/30 y antigüedad, estados de ViewModel, expiración de sesión.
- Integración Android: Retrofit con MockWebServer para 400/401/404/409 y fallos de red; Room real con pruebas instrumentadas de persistencia, deduplicación y aislamiento por usuario.
- Compose: registro y edición, selección, vacío/error, indicadores, histórico/detalle y aviso sin conexión, navegación y logout.
- Dispositivo físico: cuenta → parcela → sensor → lecturas → descargar → modo avión → reinicio → consulta; reconexión y logout/cambio de cuenta.
- Contrato: consumidores ajustados al OpenAPI final, confirmPassword obligatorio, detalle por ID y ruta de selección `GET /api/v1/fields/{fieldId}/devices`.

Comandos: `./gradlew :app:assembleDebug`, `./gradlew :app:testDebugUnitTest`, `./gradlew :app:lintDebug`, `./gradlew :app:connectedDebugAndroidTest` cuando haya dispositivo. Registrar resultados por historia y criterios en una matriz Android aparte de la matriz del backend.

## Integración y entrega

Configurar BASE_URL por entorno sin credenciales en Git. En emulador, localhost del equipo se accede normalmente mediante 10.0.2.2; en dispositivo físico usar adb reverse para el servidor local o un host alcanzable. Permitir HTTP solo para Debug si la demostración usa la API local; el destino desplegado usará HTTPS. El proveedor sigue pendiente: la integración local se puede desarrollar ahora.

Repositorio: https://github.com/AppMovil-Dato/App-Android-Terratech.git. Base de preparación en main; crear develop al cerrar esta preparación y ramas feature/* para cada porción, mediante PR hacia develop y estabilización hacia main. No versionar local.properties, .idea, .gradle, build, secretos ni el índice de Codegraph.

Cierre TB1 Android: APK Debug ejecutable en dispositivo físico, pruebas aprobadas, datos reales del backend marcados como simulados, offline demostrable y matriz con capturas/video. Informe, presentación, landing y despliegue continúan como entregables del equipo; ver [checklist](TB1_CHECKLIST.md).

## Alcance posterior y preguntas abiertas

IA, recomendaciones, electroválvulas, clima, mercado, push, correo verificado y recuperación de contraseña quedan fuera del flujo TB1. No mostrar botones funcionales de esos módulos. El recurso interno, servicio externo y feature de aprendizaje autónomo del proyecto completo deberán seleccionarse con evidencia de qué se enseñó en clase; no asumir que Hilt/Room acreditan ese requisito. Sus decisiones no bloquean este recorrido.

La familia exacta de fuente requiere el archivo de diseño o metadatos originales: el ZIP contiene PNG, sin archivos de fuente. El toolchain ya está validado; fechas del Sprint y asignación de responsables corresponden a la organización del equipo.

## Fuentes

- Guía local `example app/mobile-arquitecture-guide.pdf`, reglas DDD y cuatro capas.
- EasyStore local, referencia de Hilt/Retrofit/Compose/StateFlow.
- [Arquitectura offline-first oficial](https://developer.android.com/topic/architecture/data-layer/offline-first).
- [Hilt oficial](https://developer.android.com/training/dependency-injection/hilt-android).
- [Fuentes en Compose](https://developer.android.com/develop/ui/compose/text/fonts).
