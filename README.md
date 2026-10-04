# TerraTech Android

App nativa para el TB1 de Aplicaciones para Dispositivos Móviles, equipo NovaTech. Paquete `com.novatech.terratech`.

Recorrido implementado: registro/confirmación → login → perfil → parcela → asociación de sensor → indicadores → histórico 7/30 → detalle → consulta offline de datos descargados.

Arquitectura DDD con contextos en raíz y cuatro capas; Compose/Material 3, MVVM/StateFlow, Hilt/KSP, Retrofit/OkHttp/Gson, Room y DataStore con sesión cifrada mediante Android Keystore. La interfaz conserva la paleta y composición de las referencias del equipo. Las mediciones de demostración se identifican como `SIMULATED`.

## Ejecutar

Abrir este directorio en Android Studio. SDK 37, minSdk 29; Gradle 9.6.0, AGP 9.4.1, Kotlin Compose 2.4.20 y toolchain JDK 25, gestionado por la configuración del daemon. Las dependencias están fijadas en `gradle/libs.versions.toml`.

La base por defecto para emulador es `http://10.0.2.2:55023/`, con el backend local iniciado aparte. Cambiarla por comando, siempre con `/` al final:

```sh
./gradlew :app:assembleDebug -PTERRATECH_API_URL=https://api.example.com/
./gradlew :app:installDebug
```

La URL de ejemplo es un placeholder, no un backend publicado. Solo Debug permite HTTP. No se versiona `local.properties`; configurar el SDK del equipo en Android Studio.

Para teléfono físico con USB, iniciar el backend en localhost:55023, usar `adb reverse tcp:55023 tcp:55023` y compilar con `-PTERRATECH_API_URL=http://127.0.0.1:55023/`. Mantener el servidor y el USB disponibles durante la demostración conectada.

## Pruebas Debug

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
```

La suite instrumentada usa un emulador/dispositivo con inglés por defecto; el caso de traducción configura español explícitamente. `BackendJourneyTest` se selecciona mediante el harness de integración, no corre automáticamente contra un servidor cualquiera. Las pruebas de accesibilidad automáticas requieren API 34+; las funciones de la app soportan minSdk 29. La prueba en teléfono físico mediante clics sigue siendo un paso de aceptación. La revisión manual con TalkBack queda fuera del cierre TB1 por decisión del equipo; se conservan las comprobaciones automáticas de accesibilidad.

Para el recorrido real, el backend hermano debe estar compilado en **Debug**, con MySQL de revisión aislado en localhost:33308:

```sh
python3 scripts/verify-backend-journey.py
# Opcional: grabar el recorrido del emulador como artefacto local
TB1_RECORD_VIDEO=true python3 scripts/verify-backend-journey.py
```

El harness acepta únicamente un emulador (por defecto `emulator-5554`) y borra los datos de su app de prueba antes de cada recorrido. No usarlo para conservar datos personales del emulador. Crea una base nueva `terratech_android_<uuid>`, ejecuta los comandos explícitos de catálogo y lecturas en Development, inicia la API en localhost:55024, ejecuta la app contra ella y verifica un reinicio real sin red. Apaga temporalmente Wi-Fi/datos del emulador y los habilita al finalizar; detiene la API que creó y conserva la base aislada para diagnóstico. No usa Production ni borra bases existentes.

Paths por defecto corresponden al runtime local de revisión. Personalizar `TB1_DOTNET`, `TB1_MYSQL_CLI`, `TB1_ADB`, `TB1_MYSQL_PORT`, `TB1_ANDROID_API_PORT` y `TB1_ANDROID_SERIAL` cuando haga falta. No hay credenciales de producción ni contraseñas sembradas; las cuentas se crean desde UI para la prueba.

## Pruebas adicionales con Artemis

Artemis está instalado localmente con su consola y helper. Ejecutar `TB1_E2E_DRIVER=artemis python3 scripts/verify-backend-journey.py` para el recorrido determinista por la interfaz Android real; ver [instalación, comandos y límites](docs/ARTEMIS_TESTING.md). Las pruebas autónomas de lenguaje natural requieren una clave de IA local, aún no configurada.

## Empaquetar artefactos Android

Después de generar el APK Debug, grabar el recorrido y guardar los cambios en Git, ejecutar `python3 scripts/package-tb1.py`. Crea `artifacts/TerraTech-TB1-android.zip` con el código versionado, evidencia, APK y video; verifica el hash del APK y guarda un manifiesto. No incluye `.git`, SDK, builds, configuración local ni credenciales. Este ZIP es la porción Android del entregable del equipo; la nomenclatura oficial requiere NRC y startup.

## Offline y seguridad de sesión

Room almacena perfil, parcelas, sensores, muestras y metadatos de descarga separados por usuario. La selección se conserva en DataStore. Sin red se consultan datos previos; registro, edición y asociación requieren servicio disponible. No hay cola de escrituras ni datos inventados. Se muestran fechas de medición/descarga y se recalcula el aviso de lectura antigua (>30 minutos).

JWT cifrado con clave de Android Keystore; contraseñas no persistidas. Reautenticar al mismo usuario conserva su caché; cambiar de usuario o cerrar sesión elimina los datos privados. Un 401 solicita autenticación sin confundirlo con falta de conexión. Datos y sesión excluidos de backup.

## Documentación y evidencia

- [Validación Android](docs/ANDROID_VALIDATION.md) y [matriz TB1](docs/ANDROID_TB1_MATRIX.md).
- [Plan](docs/ANDROID_TB1_PLAN.md), [guía](docs/DEVELOPMENT_GUIDE.md) y [dirección visual](docs/design/VISUAL_DIRECTION.md).
- [Contrato backend](docs/BACKEND_CONTRACT.md) y [snapshot OpenAPI](docs/backend-openapi.snapshot.json).
- [Checklist de entrega](docs/TB1_CHECKLIST.md).

Repositorio: https://github.com/AppMovil-Dato/App-Android-Terratech.git. Ramas `main`, `develop` y `feature/tb1-android`. Usar Codegraph para explorar y compilar exclusivamente Debug.

IA, electroválvulas, clima, mercado, push, verificación de correo y recuperación de contraseña no forman parte de este TB1. No hay backend publicado todavía. Los requisitos de servicio externo, recurso interno y aprendizaje autónomo del proyecto completo se revisan en la guía; no se presentan como implementados por este recorrido.
