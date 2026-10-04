# Artemis para pruebas Android TB1

Instalación local de [google/artemis](https://github.com/google/artemis), commit fijado `351ca8422f7b5b54e80a9c1ce03a222e02415b6b` (versión 1.0), en `../../../.tools/artemis` respecto del proyecto Android. Entorno virtual Python 3.12.14, dependencias de `uv.lock` instaladas con `uv sync --frozen --no-dev`. Helper de accesibilidad v1.2.0 instalado únicamente en `emulator-5554`. scrcpy portable oficial v4.1 (SHA256 verificado) y FFmpeg 7.1 provisto por imageio-ffmpeg completan el espejo y la grabación. No se incluyeron estas dependencias ni la configuración local en Git.

Resultado: recorrido aprobado, **35 aserciones en 112 pasos**, 720 lecturas y reinicio offline comprobado. Evidencia: [UI](evidence/artemis/ui-results.json), [resumen](evidence/artemis/summary.json) e [instalación](evidence/artemis/installation.json). No fue una ejecución autónoma Flash/Pro: no hay clave configurada.

## Uso local

```sh
scripts/artemis.sh --version
scripts/artemis.sh doctor --json
scripts/artemis.sh helper status --serial emulator-5554 --json
scripts/artemis.sh ui --port 8000 --no-open
```

Consola: http://localhost:8000. El wrapper agrega al PATH los binarios locales y el ADB del SDK. Ajustar `TB1_ARTEMIS_ROOT` y `ANDROID_HOME` en otro equipo. El servidor se limita a loopback.

La consola, el controlador de dispositivo y el helper funcionan sin clave de IA. Los perfiles autónomos Flash/Pro necesitan credenciales de un proveedor: el diagnóstico no los considera listos mientras falte esa configuración. La clave debe ingresarse localmente en `.tools/artemis/.env` o en el asistente `scripts/artemis.sh init`; no pegarla en el chat ni guardarla en Git. La prueba determinista descrita abajo no invoca modelos externos.

## Recorrido determinista real mediante Artemis

```sh
TB1_E2E_DRIVER=artemis python3 scripts/verify-backend-journey.py
```

El harness usa MySQL local aislado y .NET Debug/Development, crea una base nueva y carga lecturas por el comando explícito de demostración. Compila e instala exclusivamente APK Debug. Conserva la base para diagnóstico y detiene su API al terminar.

`verify-artemis-ui.py` utiliza el `UnifiedMobileController` y Accessibility Helper reales de Artemis para leer pantallas, localizar etiquetas y bounds observados, tocar, desplazar, ingresar texto, capturar y reiniciar la app. No usa nodos Compose de prueba, mocks del backend ni llamadas HTTP para crear los recursos del recorrido. Usa la jerarquía completa porque el filtro de percepción omite contenedores de scroll. Distingue títulos y botones mediante sus acciones, espera el foco y la actualización real del texto, y consulta el estado de Android para cerrar únicamente el teclado visible; esto evita la carrera entre las acciones de accesibilidad y la recomposición de formularios. No registra contraseñas ni valores ingresados en el resultado JSON.

Alcance: confirmación incorrecta, registro exitoso/duplicado, credenciales incorrectas/login, alta y edición de perfil, parcela y selección, códigos inválido/desconocido/ocupado y asociación válida, indicadores SIMULATED, siete y treinta días con 720 lecturas, detalle, consulta offline y reinicio, logout y aislamiento visual de una segunda cuenta. El host comprueba además el reinicio sin red tras el recorrido.

Los resultados se guardan en `docs/evidence/artemis`: `ui-results.json` contiene aserciones y pasos, `summary.json` describe el backend aislado y el reinicio, y los PNG muestran pantallas reales. Los logs se mantienen fuera de Git. La evidencia Compose previa queda en `docs/evidence/backend-journey` y no se atribuye a Artemis.

El harness rechaza teléfonos físicos porque limpia datos de la app antes de cada ejecución. `TB1_ANDROID_SERIAL` selecciona un emulador explícito. El helper queda instalado en él; retirarlo con `scripts/artemis.sh helper uninstall --serial emulator-5554` cuando no se necesite. Wi-Fi/datos se reactivan al finalizar.

## Instalación reproducible en otro equipo

Clonar el repositorio en `.tools/artemis`, hacer checkout del commit indicado y ejecutar `uv sync --frozen --no-dev --python 3.12`. Instalar ADB mediante el SDK y scrcpy/FFmpeg oficiales para el sistema operativo. Para compilar su consola web, usar Node compatible, `npm ci --prefix apps/showcase_ui` y `npm run build --prefix apps/showcase_ui -- --configuration development`. Instalar el helper en el emulador elegido con `artemis helper install --serial <serial>`.

No se ejecutó el instalador global de reglas de Artemis ni se reemplazó AGENTS.md. La configuración MCP oficial puede generarse con `scripts/artemis.sh mcp --generate-config codex`; registrar ese servidor en Codex requiere recargarlo para que sus herramientas estén disponibles. La CLI y estas pruebas funcionan sin ese registro.

Artemis sobre un emulador no acredita la ejecución en teléfono físico. El cierre Android TB1 requiere repetir el recorrido mediante clics en un teléfono; TalkBack queda fuera de esta aceptación por decisión del equipo.
