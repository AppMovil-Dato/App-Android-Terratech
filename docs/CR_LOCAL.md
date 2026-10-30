# CR local — Android

Se reemplazaron los avisos de cuenta basados en cadenas por `AccountMessage`, con selección exhaustiva de textos. Se retiró el identificador constante sin uso de `Recommendation` y se corrigió la selección visible de recomendaciones: última lectura del dispositivo actual, dentro de su vigencia y sin fechas futuras. No se modifican las reglas agronómicas ni el esquema Room.

Tres pruebas nuevas fallaron contra el comportamiento anterior y pasan con la corrección: descarte de recomendaciones históricas o de otro dispositivo, lecturas vencidas/futuras y ausencia de lecturas. La navegación, la sesión pendiente y las migraciones conservan sus pruebas instrumentadas existentes.

Se quitaron los comentarios del código revisado. Los DTOs siguen en infraestructura, el estado tipado en presentación y las reglas de selección conservan el aislamiento entre usuarios/dispositivos. Estilo, modularidad y whitespace revisados.

Resultados exclusivamente Debug: [83 unitarias y 28 instrumentadas, sin fallos](evidence/cr-local/summary.json); lint sin errores y con 43 advertencias. El baseline tenía 80 unitarias y 44 advertencias. No se afirma aceptación en teléfono físico.

Este CR permanece local y no se publicó ni desplegó. El APK publicado anteriormente y la evidencia histórica de Sprint 2 corresponden a la revisión anterior. Los cambios actuales no envían correos reales en las pruebas.
