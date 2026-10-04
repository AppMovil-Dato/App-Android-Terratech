# Dirección visual TerraTech

Origen: cinco PNG de Terra-tech-app.zip y la captura de modo sin conexión, agregados por el equipo. Referencias originales preservadas en `reference/`. No son pantallas Android implementadas.

## Identidad que conservaremos

Verde agrícola para navegación, acciones y estados; ámbar para atención y datos antiguos; fondo gris muy claro; superficies blancas; texto oscuro; tarjetas de esquinas suaves, bordes/sombras discretos y espacios amplios. Iconos lineales, badges compactos, cifras grandes y etiquetas legibles. Mantener anillo de humedad y gráfica con relleno suave.

Tokens iniciales aproximados de las capturas, pendientes de comparación en dispositivo (no colores oficiales extraídos de un archivo de diseño):

| Token | Valor inicial |
|---|---|
| Primary | #2E7D32 |
| PrimaryContainer | #E7F3E8 |
| Warning | #F59E0B |
| WarningContainer | #FFF6E5 |
| Background | #F1F5F9 |
| Surface | #FFFFFF |
| TextPrimary | #19271E |
| TextSecondary | #607168 |
| Border | #DCE5DF |

Escala de espaciado 4/8/12/16/24/32 dp; tarjetas 16–20 dp de radio; campos 12 dp; acciones principales 48–56 dp de alto y área táctil mínima 48 dp. Texto en sp y soporte de escalado. El color se acompaña de etiqueta/icono; no usar verde/ámbar como única señal.

## Tipografía

Las capturas usan una familia sans serif con títulos y cifras en peso semibold/bold. No hay fuentes en el ZIP y no podemos identificar con certeza su nombre por un PNG. Mantener jerarquía y pesos; identificar la familia antes de prometer fidelidad exacta. Referencia inicial: títulos 22–24 sp, números 28–32 sp, cuerpo 16 sp, labels 12–14 sp; ajustar al texto ampliado. Empaquetar los archivos de fuente con su licencia en res/font para que el estilo funcione sin conexión, en vez de depender de una descarga al primer uso.

## Adaptación de las referencias

| Referencia | Conservar | Adaptar a TB1 |
|---|---|---|
| [Inicio](reference/home.png) | Saludo, tarjetas, tendencia y barra inferior | Humedad, temperatura del suelo, N/P/K y sensor elegido; acceso al histórico |
| [Parcelas](reference/fields.png) | Tarjetas, cultivo, indicadores, botón de alta | Lista y detalle propios; sustituir mapa ornamental por información real hasta tener un mapa soportado |
| [Sensor](reference/sensor-detail.png) | Anillo, fecha, badges y botón de histórico | SIMULATED, unidades, lectura antigua; no batería, señal LoRa, confianza IA o control de riego |
| [Perfil](reference/profile.png) | Jerarquía, tarjeta personal, lista de opciones y logout | Nombre real, iniciales si no hay foto, terreno y caché; sin certificación ficticia |
| [Offline](reference/offline.png) | Banner ámbar, tarjetas y reintentar | Texto simple «Sin conexión · datos guardados»; fecha de lectura/descarga; sin riego en cola, tanque o clima |
| [Mercado](reference/market-future.png) | Referencia de estilo para etapas posteriores | Sin pestaña Catálogo para TB1 |

No renderizar la captura completa como fondo de una pantalla: los componentes deben responder a datos, accesibilidad y tamaños de Android. No recrear barras de estado iOS de las imágenes. Inglés por defecto y español latinoamericano mediante recursos; la demostración puede usar español. Preparar formulario de registro/login e histórico coherentes con este mismo sistema, aunque no estén entre los PNG.

Validación visual prevista: comparación lado a lado con referencias, pantallas pequeñas y grandes, fuente ampliada, teclado abierto, estados vacíos/error/offline, contraste, lector de pantalla y dispositivo físico. Los previews con datos de muestra deben distinguirse de la ejecución integrada.
