# Preparación y entrega TB1

Fuente: enunciado, páginas 3–5 y 34; rúbrica, hoja TB1. El hito se llama Stage Review y presenta el Sprint 1. Corresponde a la semana 7, antes de iniciar la primera sesión de esa semana. No se deduce una fecha de calendario sin el horario del curso.

## Producto

- [ ] Landing responsive publicada y accesible, con propuesta de valor, CTA y contacto.
- [ ] Backend desplegado al 70% según el enunciado; para máxima calificación, más del 70% de endpoints funcionales del alcance, token y documentación pública Swagger/OpenAPI con descripciones y ejemplos.
- [ ] Matriz que justifique cobertura: historia → criterios → endpoint → prueba → evidencia. Contar 60 operaciones existentes no demuestra 70% del alcance vigente.
- [ ] App ejecutable en dispositivo físico, con pantallas core y flujos prioritarios del Sprint.
- [ ] UI coherente con Material Design, mockups, wireflows y user flows.
- [ ] Prototipo interactivo y evidencia de sus flujos.

## Informe

- [ ] Corregir y mejorar AV1; conservar contenido acumulado.
- [ ] Adaptar carátula oficial, curso, NRC, equipo, integrantes y período 202620. La carátula de IdeaForge contiene datos de ejemplo.
- [ ] Actualizar registro de versiones e índice.
- [ ] Actualizar Project Report Collaboration Insights con capturas y análisis de aportes reales.
- [ ] Actualizar Student Outcome ABET 7 por integrante y conclusiones del grupo.
- [ ] Completar capítulo III: estilo, arquitectura de información, landing desktop/móvil, wireframes, mockups, wireflows, user flows y prototipo.
- [ ] Completar capítulo IV: herramientas, repositorios, GitFlow, convenciones, configuración y evidencias de implementación/despliegue.
- [ ] Documentar Sprint 1: planning, goal y métricas, líderes/colaboradores, backlog y tareas estimadas entre 4 y 8 horas, con To Do / In Process / To Review / Done.
- [ ] Incluir evidencias de desarrollo, pruebas, ejecución, documentación de servicios, despliegue y colaboración.
- [ ] Actualizar conclusiones, bibliografía y anexos.
- [ ] Enlazar exposición TB1 en el anexo Videos de Exposiciones.
- [ ] Revisar conversión PDF, enlaces, diagramas, formato y requisitos de redacción del enunciado.

## Archivos de entrega

| Tipo | Formato |
|---|---|
| Informe | PDF |
| Presentación | PPTX y PDF |
| Reporte de participación del Team Leader, calificación 0–20 por integrante | DOCX y PDF |
| Artefactos y proyectos de software | ZIP |
| Exposición, máximo 15 minutos | MP4 |

Nombres: `upc-pre-202620-1acc0238-<NRC>-<startup>-<tipo>-tb1.<extensión>`. Tipos: `report`, `keynote`, `performance`, `artifacts`, `expo`. Confirmar NRC y nombre de startup antes de generar archivos.

Subir al Aula Virtual; artefactos, software y exposición también al OneDrive que indique el docente. Las primeras versiones de App Validation, About-the-Product y About-the-Team corresponden a AV2. Eso no elimina las evidencias de ejecución y prototipado requeridas para TB1.

## Bloqueos conocidos

- Android solo contiene la pantalla de plantilla.
- Los capítulos III y IV del informe siguen pendientes según la revisión inicial.
- Backend: propiedad y JWT corregidos y probados con dos cuentas; mantener esa protección al integrar Android.
- Lecturas/histórico disponibles: datos de demostración identificados como SIMULATED; integrar pantallas y Room.
- El backend tiene reportes de valores enviados por el cliente; no sustituye un motor de análisis desde sensores.
- Alinear MySQL del backend con PostgreSQL mencionado en TS03 del informe.
- Revisar alcance: comentarios sobre perfiles no equivalen a reseñas sobre productos.
- Adaptar referencias antiguas de Aplicaciones Web; en Móviles TB1 presenta Sprint 1.

## Recorrido aprobado para desarrollar Android

Registro → login → perfil → parcela → asociación de sensor → última lectura → histórico 7/30 y detalle → consulta de caché offline. Sin IA, recomendaciones, verificación de correo ni recuperación de contraseña en TB1.

| Historia / criterio | Endpoint | Backend | Android |
|---|---|---|---|
| US06 / TS05 Registro | POST /api/v1/authentication/sign-up | 201; confirmPassword obligatorio y exacto; validación; duplicado 409 | Pendiente |
| US07 Login | POST /api/v1/authentication/sign-in; GET /api/v1/users/me | JWT 8 h y usuario existente | Pendiente |
| US09 Perfil | GET/PUT /api/v1/profiles/me | Propio; ubicación y superficie m² | Pendiente |
| Parcela nueva | CRUD /api/v1/fields | Propia; cropName | Pendiente |
| US11 Selección | GET /api/v1/devices; /api/v1/fields/{fieldId}/devices | Solo propios | Pendiente |
| US17 Asociación | POST /api/v1/devices/register | Registro por código disponible; sin duplicados concurrentes | Pendiente |
| US10 / US12 Medición e histórico | GET /api/v1/devices/{id}/readings/latest y /readings?days=7\|30 | UTC, unidades, SIMULATED, antigüedad y estados vacíos | Pendiente |
| US12 Detalle | GET /api/v1/devices/{deviceId}/readings/{readingId} | Propio, persistido; mismo DTO que histórico | Pendiente |
| Offline nuevo | Room sobre el contrato identificado y fechado | Contrato preparado | Pendiente |

Contrato actualizado: [BACKEND_CONTRACT.md](BACKEND_CONTRACT.md). Matriz detallada y evidencia de 49 pruebas disponibles en el backend, `docs/TB1_MATRIX.md` y `docs/VALIDATION.md`. No convertir la cobertura de líneas ni el número de operaciones en porcentaje funcional de la rúbrica. El despliegue permanece pendiente.
