# Refactor UX de TerraTech — TB1

La app no presenta onboarding, tutorial, carrusel, checklist de configuración ni guía de bienvenida. El primer inicio utiliza las mismas pantallas y acciones que cualquier usuario posterior. Se mantienen los verdes, superficies claras, tarjetas redondeadas y la familia sans serif ya utilizada en la app.

## Comportamiento

- Registro: errores junto a cada campo, mostrar/ocultar contraseña, acciones de teclado y autocompletado. El backend devuelve sesión JWT en el 201 y se entra directamente. Si se usa un servidor anterior, Android inicia sesión automáticamente con los datos recién ingresados; la contraseña no se almacena. Si ese login falla, se informa que la cuenta ya existe y se permite iniciar sesión sin repetir el registro.
- Inicio: parcelas y sensor elegido, indicadores fechados e histórico. Una cuenta sin parcelas tiene una acción para añadir una parcela; no ve indicadores vacíos de sensores que aún no existen.
- Perfil: edición de datos personales y del fundo con validación local. El backend requiere un perfil propietario para crear parcelas; si falta se abre ese formulario y, al guardar, queda disponible la acción para crear la parcela.
- Parcela: formulario de datos, ubicación en Google Maps y revisión antes de guardar. Son etapas del formulario, no un tutorial. Se puede marcar un punto e indicar el área o dibujar 3–100 vértices con área calculada. Hay búsqueda de localidades, deshacer, limpiar, confirmación de descarte y conservación del borrador al recrear la actividad. Las coordenadas manuales quedan como opción secundaria.
- Al crear una parcela se abre su detalle con sus sensores. Al asociar un sensor se abren sus indicadores. Las pestañas Inicio, Parcelas y Perfil abren siempre su destino correspondiente.
- La consulta offline conserva los contornos, parcelas, sensores, lecturas y sesión anteriores. Las escrituras requieren conexión. Los mosaicos de Google Maps y la búsqueda geográfica requieren los servicios de Google/red; no se promete descarga offline de cartografía.

La superficie dibujada es una estimación geográfica, no una medición catastral. El servidor valida el polígono, calcula su área en m² y guarda todos los vértices; el cliente muestra hectáreas. Se rechazan cruces, duplicados, puntos colineales, coordenadas inválidas y áreas fuera de rango.

## Google Maps Debug

Dependencia: Maps Compose 9.0.0. SDK habilitado en el proyecto GCP existente. Clave de desarrollo restringida a Maps SDK for Android, paquete `com.novatech.terratech` y certificado SHA-1 Debug:

```
67:9F:D4:CE:93:E4:6F:9C:23:5D:45:EF:9E:3F:A9:28:7F:FE:1C:2C
```

La clave se configura exclusivamente en el `local.properties` ignorado:

```
MAPS_API_KEY=TU_CLAVE_LOCAL
```

También se acepta la variable local `TERRATECH_MAPS_API_KEY`. No incluir valores reales en Git. En otro equipo se debe registrar el SHA-1 de su certificado Debug con `./gradlew :app:signingReport`. La clave solo se incorpora a la variante Debug; no se prepara una clave de producción. Sin clave, el formulario ofrece entrada manual de coordenadas.

## Persistencia y contratos

Backend: migración incremental `FieldBoundary`, columna nullable `fields.boundary`, datos antiguos conservados. Registro 201 añade `token` y `expiresAt` manteniendo los campos públicos. Crear/consultar/actualizar parcelas admite `boundary: [{latitude, longitude}]`. Omitir el contorno en una actualización antigua lo preserva; enviar `[]` lo elimina.

Room: versión 2, migración 1→2 agrega `boundaryJson` con valor por defecto `[]`. No se elimina la base local al actualizar. Snapshot de API actualizado en `docs/backend-openapi.snapshot.json`.

## Validación reproducible

```
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:connectedDebugAndroidTest
TB1_EVIDENCE_DIR=docs/evidence/ux/backend-journey python3 scripts/verify-backend-journey.py
TB1_E2E_DRIVER=artemis TB1_EVIDENCE_DIR=docs/evidence/ux/artemis python3 scripts/verify-backend-journey.py
python3 scripts/check-modularity.py --backend ../../BackEnd
```

Los harnesses usan exclusivamente un emulador y MySQL/.NET locales aislados. Limpian la app de prueba del emulador, generan usuarios temporales y lecturas SIMULATED explícitas. Verifican cuatro vértices persistidos en MySQL y reinicio offline. Artemis usa clics/teclado, sin TalkBack ni modelos externos. Los resultados finales se conservan bajo `docs/evidence/ux`; los logs no se publican. Una prueba en emulador no sustituye la revisión en teléfono físico.

## Referencias consultadas

- [Patrones de navegación de Android](https://developer.android.com/design/ui/mobile/guides/layout-and-content/layout-and-nav-patterns): tres destinos del mismo nivel, etiquetas y acciones contextualizadas.
- [Google Maps Compose](https://github.com/googlemaps/android-maps-compose): mapa nativo, marcadores, polígonos y cámara.
- [Configurar claves de Maps Android](https://developers.google.com/maps/documentation/android-sdk/get-api-key): restricciones por paquete y certificado.

Se revisó el catálogo de skills disponible. No ofrecía una skill específica de UX Android; las decisiones se apoyan en la documentación oficial y en la verificación de la app real.

## Resultado final

Compilación Debug y lint aprobados; 39 pruebas unitarias Android, 19 instrumentadas y 69 del backend aprobadas. Los recorridos Compose y Artemis pasaron con registro directo, dibujo de cuatro vértices, registro de sensor, histórico y reinicio sin conexión. Evidencias: `docs/evidence/ux/validation.json`.

Backend actualizado en `https://terratech-api.lucemz.com/`, revisión Cloud Run `backend-terratech-git-00007-qb5`. APK local: `artifacts/TerraTech-TB1-UX-debug.apk` (ignorado en Git). No se incorpora onboarding ni checklist.
