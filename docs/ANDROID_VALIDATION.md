# Validación Android TB1

Implementación validada en Debug sobre Pixel 8 API 36 (Android 16), con backend .NET/MySQL local aislado. No se ha publicado el servicio. La prueba en teléfono físico mediante clics permanece pendiente; TalkBack queda fuera de la aceptación acordada con el equipo; no se declara cerrada la aceptación completa del TB1.

## Resultados

| Control | Resultado | Evidencia |
|---|---|---|
| Compilación | APK Debug generado | `artifacts/TerraTech-TB1-debug.apk`; hash y tamaño en [summary.json](evidence/checks/summary.json) |
| Unitarias | 32 aprobadas, cero fallos/omitidas | [XML de pruebas](evidence/checks/) |
| Instrumentadas independientes | 14 aprobadas, cero fallos/omitidas | [instrumented-results.xml](evidence/checks/instrumented-results.xml) |
| Integración real | Un recorrido de extremo a extremo aprobado | [resultado y capturas](evidence/backend-journey/), [summary.json](evidence/backend-journey/summary.json) |
| Artemis UI black-box | Un recorrido adicional aprobado: 35 aserciones, 112 pasos; sin LLM | [ui-results.json](evidence/artemis/ui-results.json), [resumen](evidence/artemis/summary.json), [guía](ARTEMIS_TESTING.md) |
| Reinicio offline | Proceso cerrado y reabierto sin red; sesión, selección y lecturas restauradas | [cold-restart.xml](evidence/backend-journey/cold-restart.xml), [captura](evidence/backend-journey/cold-restart.png) |
| Lint Debug | Cero errores; 28 observaciones reportadas | [SARIF](evidence/checks/lint-results.sarif) |
| Arquitectura | Dominio/aplicación sin imports de Android, Hilt, Room, Retrofit o Gson | Inspección de imports y exploración Codegraph |

Las 14 instrumentadas independientes incluyen UI/idiomas/accesibilidad automática, Room y sesión cifrada. La prueba del recorrido se ejecuta por separado; no se suma dos veces. Artemis aporta otra ejecución independiente por el árbol completo de accesibilidad y el controlador de dispositivo, sin API de pruebas Compose ni llamadas a modelos. Sus 35 aserciones pertenecen a un solo recorrido, no a 35 casos JUnit. Las observaciones de Lint incluyen sugerencias de versiones y recursos sin uso; cero errores no significa cero advertencias. No se infiere un porcentaje funcional o cobertura de líneas a partir de estos resultados.

## Recorrido ejecutado

Desde UI: confirmación inválida y registro correcto, correo duplicado, credenciales incorrectas y login, alta/edición de perfil con conversión ha/m², creación/selección de parcela, código inválido/desconocido, asociación correcta y rechazo de sensor ocupado. Después: indicadores con unidades/fecha/SIMULATED, histórico de siete y treinta días, 720 muestras persistidas, detalle y consulta sin red. Se comprueba aislamiento al acceder con una segunda cuenta.

Además se comprueban los límites de antigüedad, el orden de UTC con distinta precisión, errores HTTP, rechazo de estado visual de otra cuenta, conservación de caché al reautenticar al mismo usuario y purga al cambiar de usuario o cerrar sesión. No se inventa un umbral de humedad si todavía no existe una referencia descargada.

## Artefactos locales de entrega

`artifacts/TerraTech-TB1-debug.apk` contiene la compilación Debug configurada para el backend local en 55023. `artifacts/TerraTech-TB1-emulator.mp4` registra el recorrido automatizado en emulador; no acredita ejecución en teléfono físico ni sustituye la exposición del equipo. `artifacts/TerraTech-TB1-android.zip` empaqueta el código versionado, documentación, evidencia, APK y video para compartir. Estos binarios se mantienen fuera de Git. Los nombres oficiales de entrega todavía requieren NRC y nombre de startup.

Para repetir el video, usar `TB1_RECORD_VIDEO=true python3 scripts/verify-backend-journey.py`.

## Repetir

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:connectedDebugAndroidTest
python3 scripts/verify-backend-journey.py
```

El segundo comando requiere el backend hermano compilado Debug y MySQL local aislado; los paths configurables están en el README. Crea y conserva una base nueva de prueba; utiliza comandos explícitos de demostración en Development. El APK del harness apunta a localhost:55024 a través de la dirección del emulador. El APK de entrega apunta al backend local en 55023; para cambiar la URL, reconstruir siguiendo el README.

Para la aceptación física: conectar Android por USB, configurar `adb reverse` y la URL indicados en el README, repetir registro/perfil/parcela/sensor/histórico, apagar la conexión y reiniciar el proceso. Revisar texto ampliado y navegación mediante clics. TalkBack no es un requisito de este cierre. Registrar dispositivo/versión/resultado en esta matriz antes de marcarlo verificado.

La [matriz de historias](ANDROID_TB1_MATRIX.md) relaciona criterios con pruebas. La apariencia fue revisada sobre capturas reales del emulador y conserva verde/ámbar, tarjetas redondeadas y jerarquía sans serif. La fuente original exacta de los PNG no está identificada.
