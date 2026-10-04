# Guía de desarrollo Android de TerraTech

Estado: preparación documental. El plan vigente está en [ANDROID_TB1_PLAN.md](ANDROID_TB1_PLAN.md); las librerías y funciones todavía no se implementaron.

## 1. Estado inicial comprobado

| Elemento | Valor observado |
|---|---|
| Nombre | TerraTech |
| Paquete y applicationId | com.novatech.terratech |
| Interfaz inicial | Empty Activity, Compose, Greeting |
| minSdk | 29 |
| compileSdk / targetSdk | 37 / 37 |
| AGP declarado | 9.4.1 |
| Kotlin declarado para el plugin Compose | 2.2.10 |
| Compose BOM declarado | 2026.02.01 |
| Compatibilidad Java declarada | 11 |
| Repositorio Git en esta carpeta | Inicializado el 4 de octubre; remoto AppMovil-Dato/App-Android-Terratech |

Los valores son los del proyecto creado, no una validación de compatibilidad. No se ha ejecutado Gradle ni compilado la app durante esta preparación. Revisar SDK, JDK, Gradle, AGP, Kotlin y procesadores antes de instalar librerías. No copiar automáticamente las versiones de EasyStore, cuyo catálogo usa valores diferentes.

## 2. Requisitos que condicionan las decisiones

El enunciado establece Kotlin para Android nativo y Domain-Driven Design para el diseño de la solución. El proyecto completo debe incluir:

- Almacenamiento local de información.
- Acceso a un recurso interno del dispositivo.
- Integración con el servicio REST propio.
- Integración con un servicio externo de terceros.
- Un feature de aprendizaje autónomo que use una tecnología, biblioteca o servicio distinto de lo usado en clase, con justificación y evidencia del aprendizaje.
- Demostración en un dispositivo físico.
- Internacionalización en inglés y español latinoamericano, con inglés por defecto; accesibilidad y una experiencia consistente.

Estos requisitos generales no implican que todos deban quedar terminados en TB1. El alcance concreto de TB1 debe reflejar las historias comprometidas en el Sprint 1 y los requisitos de la rúbrica. Ver [checklist](TB1_CHECKLIST.md).

Hay libertad para elegir librerías adicionales, siempre que se justifiquen y respeten la arquitectura y tecnologías del curso. Usar Hilt o Retrofit del ejemplo no acredita por sí solo aprendizaje autónomo si ya se impartieron en clase.

## 3. Tecnologías propuestas

| Tecnología | Propósito | Evidencia / estado |
|---|---|---|
| Kotlin | Lenguaje Android | Requisito; proyecto creado |
| Compose + Material 3 | Interfaz | Ya configurados; presentes en EasyStore |
| ViewModel + StateFlow | Estado de presentación, patrón MVVM | Presentes en EasyStore; pendientes en TerraTech |
| Coroutines / Flow | Trabajo asíncrono y datos reactivos | Ejemplo y diapositivas |
| Dagger Hilt | Inyección de dependencias | Implementado en EasyStore; pendiente en TerraTech |
| KSP | Procesamiento para Hilt/Room | Propuesto; validar compatibilidad de versiones |
| Retrofit + OkHttp | Consumo del backend, interceptores y token | Propuesto; Retrofit presente en EasyStore |
| Gson o Kotlin Serialization | JSON | Elegir una estrategia; EasyStore utiliza Gson |
| Navigation Compose | Navegación | Presente en EasyStore; pendiente en TerraTech |
| Room | Datos locales estructurados y caché | Aparece en guía y diapositivas; no está en EasyStore |
| DataStore | Preferencias y estado ligero | Aparece en guía; definir tratamiento seguro de sesión aparte |
| Coil | Imágenes remotas | Presente en EasyStore; usar si las pantallas lo requieren |
| WorkManager | Sincronización persistente en segundo plano | Aparece en diapositivas; incorporar cuando el alcance offline lo necesite |

MVVM es un patrón de presentación, no una biblioteca. Hilt utiliza Dagger; no se propone configurar simultáneamente dos sistemas independientes de inyección.

Las versiones de Hilt, KSP, Room, Navigation y Retrofit se seleccionarán al iniciar el desarrollo y comprobar la compatibilidad con el proyecto. No hay versiones nuevas instaladas por esta preparación.

Referencias oficiales para consultar al implementar:

- [Hilt](https://developer.android.com/training/dependency-injection/hilt-android).
- [Arquitectura offline-first](https://developer.android.com/topic/architecture/data-layer/offline-first).
- [Room](https://developer.android.com/training/data-storage/room).

## 4. Arquitectura de referencia

La guía de arquitectura organiza los bounded contexts directamente bajo el paquete raíz, sin una carpeta genérica `features/`. Los contextos deben derivarse del negocio y del informe, no de los nombres de las pantallas.

Estructura propuesta, pendiente de reconciliar los nombres de contextos del informe con los ocho módulos existentes en el backend:

```text
com.novatech.terratech
├── iam/
├── profile/
├── monitoring/
├── analytics/
├── notification/
├── stock/
├── commercial/
├── community/
└── core/                 # Red, base de datos, DI, navegación y diseño compartido
```

Crear únicamente los contextos necesarios para el alcance autorizado. No crear paquetes vacíos para simular una implementación completa. Los contextos de Android no tienen que replicar mecánicamente cada módulo del backend.

Cada contexto mantiene las cuatro capas de la guía:

```text
monitoring/
├── presentation/
│   ├── ui/
│   ├── viewmodel/
│   └── state/
├── application/
│   ├── usecase/
│   └── service/
├── domain/
│   ├── entity/
│   ├── valueobject/
│   └── repository/
└── infrastructure/
    ├── local/
    ├── remote/
    ├── implementation/
    ├── mapper/
    └── di/
```

Reglas:

1. Las dependencias apuntan hacia dominio; dominio no depende de presentación, aplicación ni infraestructura.
2. Dominio y aplicación contienen Kotlin independiente de Android y sin imports de persistencia/red. Evitar APIs de Hilt específicas de Android en estas capas.
3. Reglas e invariantes del negocio residen en entidades y objetos de valor. La guía exige value objects para toda variable con reglas de validación; los datos sin invariantes pueden mantener tipos simples.
4. Interfaces de repositorio pertenecen a dominio; sus implementaciones a infraestructura.
5. DTOs del API y entidades de Room no salen a presentación/aplicación. Se mapean a modelos de dominio.
6. ViewModels invocan acciones de aplicación y exponen estado inmutable a Compose; no contienen llamadas Retrofit o DAO directas.
7. Las diapositivas añaden separación de comandos y consultas (CQRS) y eventos entre contextos. La guía y EasyStore muestran UseCases. Conservar las cuatro capas y diseñar acciones de alta cohesión; resolver el grado de CQRS antes de implementar, sin asumir que obliga a bases separadas o a un bus complejo.

## 5. Qué aprovechar de EasyStore

Referencia útil para Hilt, Retrofit, conversión DTO → dominio, ViewModels con StateFlow, pantallas Compose, componentes, tema, Coil y navegación.

Adaptaciones necesarias:

- Sustituir `pe.edu.upc.easystore` por el paquete de TerraTech.
- Organizar contextos en raíz: el ejemplo usa `features/`, mientras la guía pide contextos directos.
- Cambiar DummyJSON por el backend TerraTech y sus DTOs reales.
- Implementar autenticación: LoginScreen solo tiene campos, sin integración ni sesión.
- Implementar Room; el ejemplo no contiene persistencia local.
- Distinguir errores HTTP/red de listas vacías y recursos inexistentes. El repositorio del ejemplo puede ocultar errores devolviendo `emptyList()` o `null`.
- Definir estados de carga, éxito, vacío, error y datos locales desactualizados.
- Sustituir textos fijos por recursos traducibles y revisar accesibilidad.
- Reemplazar los tests de plantilla por pruebas de reglas y flujos relevantes.

EasyStore fue revisado estáticamente; no se comprobó su compilación. No debe tratarse como una plantilla validada lista para copiar.

## 6. Decisiones pendientes antes de comenzar

- [x] Recorrido backend aprobado: registro, login, perfil, parcela, asociación de sensor y lecturas 7/30; Android añadirá Room. Confirmar con el equipo los IDs definitivos de las HU.
- [ ] Alinear contextos, diagramas y nombres del informe con Android y backend.
- [ ] Acordar wireframes, mockups, user flows y navegación en Figma.
- [ ] Resolver qué datos se guardan en Room y qué comportamiento se ofrece sin red.
- [ ] Definir caché, actualización, conflictos y sincronización según el alcance real.
- [ ] Elegir recurso del dispositivo y servicio externo que aporten al negocio.
- [ ] Elegir y justificar el feature de aprendizaje autónomo; registrar qué se enseñó en clase.
- [x] Contrato backend actualizado en BACKEND_CONTRACT.md; JWT y propiedad validados.
- [ ] Elegir URL de despliegue e integrar Android; servidor de revisión local disponible en localhost:55023.
- [ ] Elegir estrategia de sesión, vencimiento, cierre y almacenamiento de tokens.
- [ ] Verificar SDK/JDK/Gradle y compatibilidad de dependencias; trabajar solo con Debug.
- [x] Inicializar Git y conectar el repositorio autorizado. Flujo main/develop/feature definido en el plan; asignación de responsables pendiente.

## 7. Verificación futura

No se ejecutó ninguno de estos pasos en esta preparación:

- Compilación: `./gradlew :app:assembleDebug`.
- Unit tests: `./gradlew :app:testDebugUnitTest`.
- Lint: `./gradlew :app:lintDebug`.
- Pruebas en dispositivo: `./gradlew :app:connectedDebugAndroidTest`, cuando exista una suite y un dispositivo configurado.

Priorizar pruebas de validaciones, mappers, estados de ViewModel, sesión, aislamiento entre usuarios, persistencia y flujos core. No confundir tests de plantilla o respuestas exitosas del API con cobertura funcional del producto.
