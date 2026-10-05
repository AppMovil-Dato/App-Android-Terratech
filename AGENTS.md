# Instrucciones del proyecto

- Usar Codegraph siempre que sea posible.
- Compilar y ejecutar pruebas exclusivamente de la variante Debug.
- Seguir docs/ANDROID_TB1_PLAN.md y la guía DDD: contextos en raíz y cuatro capas.
- No guardar credenciales, contraseñas, JWT ni configuración local en Git.
- Mantener la identidad visual de docs/design/VISUAL_DIRECTION.md.

- Cada clase, interfaz, data class y value object debe estar en su propio archivo con su nombre.
- Cada componente Compose de nivel superior debe tener un archivo propio; separar estado de pantalla, contenido y componentes reutilizables.
- DTOs, entidades y DAOs pertenecen a la infraestructura de su contexto. No agrupar tipos o pantallas en archivos contenedores.
- Ejecutar `python3 scripts/check-modularity.py` al modificar la organización del código.

- Usar imports explícitos; no escribir nombres de paquetes dentro de firmas o cuerpos ni usar imports con comodines.
- Usar nombres descriptivos para estados, parámetros y variables. Ejecutar `python3 scripts/check-kotlin-style.py` después de editar Kotlin.
