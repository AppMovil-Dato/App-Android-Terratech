# Matriz de aceptación Android TB1

Estado: implementación del recorrido disponible; evidencia de emulador y controles Debug en [ANDROID_VALIDATION.md](ANDROID_VALIDATION.md). La columna de validación física permanece pendiente hasta probar en un teléfono real. Esta matriz no deriva un porcentaje funcional de cobertura de líneas ni del número de endpoints.

| Historia | Criterios implementados | Prueba / evidencia | Teléfono físico |
|---|---|---|---|
| US06 — Registro | Nombre, correo normalizado, contraseña y confirmación exacta; error de campos y duplicado; volver a login | DomainRulesTest, ApplicationActionsTest, ViewModelStateTest, ApiContractTest y BackendJourneyTest: diferencia, éxito y duplicado desde UI | Pendiente |
| US07 — Login | Credenciales, JWT cifrado, expiración/401, logout y aislamiento entre cuentas | ApiContractTest, SessionAndPersistenceTest, MonitoringStateTest y BackendJourneyTest: credenciales incorrectas, login y segunda cuenta | Pendiente |
| US09 — Perfil | Consulta, alta/edición de datos personales y terreno; correo no editable; conversión ha/m² | DomainRulesTest.profileValidationLivesInDomainAndPreservesTerrainUnits y BackendJourneyTest: alta, edición de nombre y superficie | Pendiente |
| US17 — Sensor | Código TT-XXXXXX, nombre, parcela elegida; códigos inválidos/desconocidos y ocupado | SensorCode, MonitoringStateTest, ApiContractTest y BackendJourneyTest: asociación y rechazo de tres entradas | Pendiente |
| US11 — Selección | Listar parcelas propias, crear parcela, elegirla, listar sensores, vacío y selección persistida | MonitoringStateTest, RoomCacheTest, ComposeScreensTest y BackendJourneyTest; captura fields.png | Pendiente |
| US10 — Indicadores | Humedad %, temperatura del suelo °C, N/P/K ppm, UTC/hora local, source, antigüedad estricta >30 min | DomainRulesTest.readingStaleBoundaryIncludesFractions, ApiContractTest y BackendJourneyTest; sensor.png/home.png | Pendiente |
| US12 — Histórico | Siete días por defecto, treinta días, gráfico/lista cronológicos, intervalo vacío y detalle | DomainRulesTest, RoomCacheTest.chronologicalOrderHandlesMixedIsoUtcPrecision, ComposeScreensTest y BackendJourneyTest: exige 720 muestras en 30 días | Pendiente |
| Consulta sin conexión | Datos descargados, fechas, aviso offline, reintentar; persistencia tras reiniciar; sin escrituras en cola | RoomCacheTest, SessionAndPersistenceTest, MonitoringStateTest, BackendJourneyTest y verificación host de cierre/reapertura del proceso; cold-restart.png/xml | Pendiente |
| Transversal — Arquitectura | Contextos en raíz, cuatro capas, dominio/aplicación Kotlin puro y mapeo DTO/Room | Inspección de código + verificación de imports; Codegraph | No aplica |
| Transversal — Idiomas/accesibilidad | Inglés por defecto, español, texto ampliado, semántica de gráfico y valores listados | ComposeScreensTest con Accessibility Test Framework; revisión visual de capturas; TalkBack manual pendiente | Pendiente |

También se ejecutó el recorrido black-box con Artemis: US06, US07, US09, US17, US11, US10, US12 y consulta offline. [35 aserciones aprobadas](evidence/artemis/ui-results.json), [720 lecturas y reinicio del proceso sin red](evidence/artemis/summary.json). Esta ejecución usa su helper/controlador local, sin LLM, y no reemplaza la validación física.

## Reproducibilidad y límites

Pruebas unitarias en `app/src/test`; instrumentadas en `app/src/androidTest`. El harness guarda resultados y capturas en `docs/evidence/backend-journey`. Las pruebas aisladas del backend también cubren propiedad/JWT y están documentadas en su repositorio; no se cuentan como pruebas Android.

Un emulador demuestra ejecución Android e integración, pero no acredita por sí mismo el requisito de dispositivo físico. No se ha publicado el backend. La fuente exacta de los PNG no está identificada; la implementación conserva la jerarquía, pesos y familia sans serif del sistema, sin afirmar una reproducción exacta de esa fuente.
