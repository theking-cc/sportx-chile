# Siguiente etapa: conexión al MySQL existente

La migración de datos todavía no se ha ejecutado. Primero se necesita el archivo .sql del esquema existente o la estructura de tablas para adaptar el backend sin reemplazar el trabajo del estudiante.

## Preparación disponible

- Usuarios, productos, pedidos y consultas tienen operaciones centralizadas en repository.js.
- El panel administrativo permite exportar los datos locales actuales.
- Los pedidos conservan precio, nombre y cantidad de cada línea como historial.
- Las claves antiguas permanecen disponibles en su origen; no se eliminan.

## Orden de trabajo

1. Respaldar localStorage del origen anterior y exportar la versión actual. No publicar contraseñas o datos personales reales en GitHub.
2. Revisar tablas, claves, tipos y relaciones del esquema MySQL del estudiante.
3. Implementar API de usuarios/autenticación, productos, pedidos y consultas. Elegir el backend conforme a la pauta del siguiente encargo.
4. Implementar hashing de contraseñas y autorización del lado servidor. Nunca confiar en el rol enviado por el cliente.
5. Importar datos con validación de RUN/correo/códigos duplicados y transformar IDs si el esquema lo requiere.
6. Confirmar compras con transacción de base de datos y control de stock concurrente.
7. Sustituir repository.js por llamadas HTTP conservando las interfaces de operación; adaptar los estados de carga y error del frontend.
8. Verificar CRUD, pedidos, roles y persistencia contra MySQL.

## Correspondencia propuesta para revisar

| Datos locales | Entidad backend sugerida |
|---|---|
| users | usuarios y roles |
| products | productos y categorías |
| orders | pedidos |
| orders.items | detalle_pedido |
| messages | consultas |
| cart | carrito de cliente o estado del frontend |

Son nombres orientativos: deben ajustarse al esquema ya creado. No existe conexión directa navegador → MySQL en esta versión.
