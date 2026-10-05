# Compose y navegación

TerraTech conserva una sola `MainActivity`. La Activity inicia el tema y `TerraTechApp`; no contiene rutas, llamadas HTTP ni formularios.

## Recorrido del código

- `TerraTechApp`: obtiene los ViewModels de sesión compartidos, observa sus estados con lifecycle y decide entre cuenta y contenido privado. Los estados de perfil y monitoreo se filtran por usuario antes de mostrarlos.
- `SignedInApp`: conecta estados y acciones con el controlador, reautenticación y eventos de creación. No dibuja formularios ni decide rutas con textos.
- `SignedInContent`: dibuja Scaffold, indicadores de carga, mensajes, toolbar y barra inferior. Recibe estado y callbacks; no recibe NavController ni ViewModels.
- `TerraTechNavHost`: compone tres subgrafos. Las pantallas reciben datos y callbacks, y cada registro de destino de monitoreo tiene su propio archivo.

## Grafos y destinos

| Subgrafo | Contexto | Destinos |
| --- | --- | --- |
| `HomeGraph` | core/presentation/navigation | `HomeDestination` |
| `FieldsGraph` | monitoring/presentation/navigation | `FieldsDestination`, `CreateFieldDestination`, `FieldSensorsDestination(fieldId)`, `RegisterSensorDestination(fieldId)`, `SensorDestination(deviceId)`, `HistoryDestination(deviceId)`, `ReadingDestination(deviceId, readingId)` |
| `ProfileGraph` | profile/presentation/navigation | `ProfileDestination` |

Hay un NavHost y un NavController para los tres subgrafos privados. Registro y login son modos de `AccountScreen`, fuera del grafo privado; no se agrega onboarding. Login/registro con sesión entra a Inicio; logout elimina el contenido privado. La reautenticación usa `ReauthenticationRoute` y conserva la navegación de la misma cuenta.

Las rutas son objetos o data classes `@Serializable`, uno por archivo. Se usan `composable<Destination>`, `navigate(Destination(...))` y `toRoute<Destination>()`. Los argumentos llevan identificadores, no entidades ni datos personales. `AppNavigator` concentra las operaciones de navegación.

## Pestañas y Atrás

- Inicio, Parcelas y Perfil corresponden a sus grafos; la selección se obtiene de la jerarquía del destino, no de prefijos de texto.
- Histórico y detalle pertenecen a Parcelas, incluso al abrirlos desde Inicio.
- Cambiar de pestaña guarda y restaura la pila de su grafo. Volver a pulsar la pestaña activa abre su raíz.
- Atrás vuelve al destino anterior. Crear parcela mantiene su propio BackHandler para retroceder en el formulario y confirmar el descarte; la barra inferior se oculta en formularios de creación/registro.
- Al crear una parcela o sensor se retira su formulario de la pila y se abre el recurso creado.
- Un cambio de usuario crea otro controlador. La clave `typed-navigation-v1` evita restaurar la pila antigua basada en rutas de texto después de actualizar la app; conserva la caché Room.

## Estado y Compose

Los ViewModels de cuenta/perfil/monitoreo se comparten en el nivel de la Activity: el repositorio de sesión reinicia su estado al cambiar de usuario y `forUser` protege la presentación durante la transición. La selección también se guarda en DataStore y los datos privados en Room.

`CreateFieldRoute` es el único contenedor del ViewModel de búsqueda geográfica. Hilt lo vincula al destino de creación, y `collectAsStateWithLifecycle` observa sus resultados. `CreateFieldScreen` guarda el borrador con `rememberSaveable`; `CreateFieldContent` dibuja el formulario con estado y callbacks. Registrar sensor también separa `RegisterSensorScreen` de `RegisterSensorContent`. Las otras pantallas no obtienen ViewModels ni controladores de navegación.

Al restaurar un destino de sensor se selecciona su parcela y dispositivo por ID. Mientras se completa esa selección, `forField`/`forSensor` impiden mostrar lecturas del recurso anterior. La lectura de detalle se filtra por dispositivo e identificador de lectura.

## Verificación

```
python3 scripts/check-kotlin-style.py
python3 scripts/check-modularity.py
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:connectedDebugAndroidTest
```

`NavigationGraphTest` usa el NavHost real con `TestNavHostController` y callbacks locales: prueba navegación mediante botones, argumentos, jerarquía de pestañas, restauración, Atrás y cambio de cuenta. `MonitoringNavigationTest` comprueba la selección de una parcela diferente al restaurar un sensor y la persistencia del par parcela/dispositivo. El recorrido HTTP/Android de `verify-backend-journey.py` se ejecuta sobre backend/MySQL aislados; Artemis utiliza clics y teclado, sin TalkBack.

Resultados del refactor: [suite Debug](evidence/navigation/checks/summary.json) y [recorrido Artemis](evidence/navigation/artemis/summary.json).

## Referencias oficiales consultadas

- [Rutas tipadas](https://developer.android.com/guide/navigation/design/type-safety).
- [Grafos anidados](https://developer.android.com/guide/navigation/design/nested-graphs).
- [Pruebas de Navigation Compose](https://developer.android.com/guide/navigation/testing/compose).
- [State hoisting en Compose](https://developer.android.com/develop/ui/compose/state-hoisting).
- [Hilt con Navigation Compose](https://developer.android.com/training/dependency-injection/hilt-jetpack).
