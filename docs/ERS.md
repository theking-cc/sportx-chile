# Especificación de Requisitos de Software - SportX Chile

Versión 2.0 - 7 de octubre de 2026

Estudiante, docente e integrantes: completar en la entrega privada. Asignatura: Desarrollo Fullstack II (DSY1104).

## 1. Propósito y propuesta personalizada

SportX Chile propone una tienda deportiva orientada a productos de boxeo, fitness, calzado y accesorios. La actualización transforma el prototipo HTML/JavaScript en un frontend React con 21 vistas, diseño Bootstrap y pruebas Jasmine/Karma. Se mantienen datos locales para poder demostrar los procesos sin un servidor de base de datos.

El propósito es facilitar la búsqueda de productos, el seguimiento del inventario y la compra simulada, separando la experiencia de clientes de las funciones de vendedores y administradores.

## 2. Alcance

Incluye catálogo, carrito, descuentos, checkout simulado, registro de pedidos, historial, perfil, autenticación de demostración, CRUD de productos/usuarios, estados de pedido y consultas. Excluye cobros reales, correo, despacho externo, backend y conexión MySQL en esta versión. La conexión futura se realizará mediante una API.

## 3. Actores

| Actor | Acciones |
|---|---|
| Visitante | Navegar, buscar productos, usar carrito, consultar artículos, registrar consulta y crear cuenta |
| Cliente | Funciones públicas, iniciar/cerrar sesión, editar su perfil, comprar y consultar sus pedidos |
| Vendedor | Gestionar catálogo e inventario, consultar métricas y cambiar estados de pedidos |
| Administrador | Funciones de vendedor, administrar usuarios, revisar consultas y exportar respaldo |

## 4. Requisitos funcionales y criterios de aceptación

| ID | Requisito | Criterio de aceptación |
|---|---|---|
| RF01 | Mostrar catálogo compartido | Inicio, catálogo y detalle consultan los mismos productos |
| RF02 | Buscar y filtrar | El listado se actualiza por nombre y categoría y muestra un mensaje si está vacío |
| RF03 | Mostrar detalle | Presenta nombre, categoría, descripción, precio y stock del producto seleccionado |
| RF04 | Gestionar carrito | Agrega, acumula, disminuye y elimina cantidades, y permite vaciarlo |
| RF05 | Controlar stock | Rechaza cantidades negativas, no enteras o superiores al stock acumulado |
| RF06 | Aplicar descuentos | Valida cupones DUOC10 y SPORTX2026; el total nunca es negativo |
| RF07 | Crear pedido | Requiere sesión y dirección válida; conserva productos y precios del momento de compra |
| RF08 | Confirmar pedido | Guarda el pedido, descuenta stock y vacía carrito en una única escritura |
| RF09 | Mostrar historial | Cada cuenta solo consulta sus propios pedidos y sus detalles |
| RF10 | Registrar cuentas | Valida RUN, correo autorizado, campos requeridos y confirmación de contraseña; evita duplicados |
| RF11 | Iniciar/cerrar sesión | Credenciales válidas habilitan la cuenta; cerrar sesión restringe las rutas protegidas |
| RF12 | Editar perfil | Permite actualizar datos sin cambiar el rol; una contraseña vacía conserva la anterior |
| RF13 | Gestionar productos | Administrador y vendedor crean, editan y eliminan; código único, precio y stock enteros no negativos |
| RF14 | Gestionar usuarios | Solo administrador crea, edita y elimina; no elimina su propia cuenta ni deja el sistema sin administrador |
| RF15 | Gestionar pedidos | Permite transiciones Pendiente → Preparando → Enviado → Entregado y cancelar antes de Enviado |
| RF16 | Reponer stock al cancelar | Un pedido cancelado devuelve unidades una vez y no puede reactivarse |
| RF17 | Mostrar panel | Calcula métricas desde catálogo y pedidos guardados; indica stock crítico |
| RF18 | Registrar consultas | Valida nombre, correo y mensaje y permite al administrador leer las consultas guardadas |
| RF19 | Presentar contenido informativo | Nosotros, blog y artículos muestran contenido real de la propuesta |
| RF20 | Exportar respaldo | Administrador descarga JSON de la versión local actual |
| RF21 | Implementar 21 vistas | Las pantallas y rutas constan en VISTAS.md y ninguna usa un marcador de sección pendiente |

## 5. Requisitos no funcionales

| ID | Requisito | Comprobación |
|---|---|---|
| RNF01 | React organizado por responsabilidades | Páginas, componentes, contexto y servicios separados |
| RNF02 | Diseño responsivo Bootstrap | Verificación de navegación a 375 px y escritorio a 1280 px |
| RNF03 | Formularios comprensibles | Labels asociados, errores contextualizados y mensajes de estado |
| RNF04 | Persistencia local | Los datos se mantienen al recargar en el mismo origen |
| RNF05 | Protección de datos existentes | Las claves antiguas no se eliminan y JSON inválido no se sustituye silenciosamente |
| RNF06 | Pruebas reproducibles | npm test ejecuta Jasmine/Karma, mocks/spies y genera cobertura |
| RNF07 | Mantenibilidad | Reglas de datos centralizadas para sustituir localStorage por servicios del backend |

No se afirma certificación de accesibilidad ni seguridad de producción. Los permisos y las contraseñas locales son propios de un prototipo académico.

## 6. Reglas y modelo de datos

- Producto: id, código, nombre, descripción, categoría, precio CLP, stock, stock crítico e imagen.
- Usuario: id, RUN, nombre, apellidos, correo, contraseña local para cuentas registradas, teléfono, fecha de nacimiento, región, comuna, dirección y rol.
- Carrito: producto, cantidad y datos de presentación.
- Pedido: id, número, dueño, cliente/correo, fecha, estado, detalle histórico, dirección de despacho, cupón, subtotal, descuento y total.
- Consulta: id, nombre, correo, mensaje y fecha.

Los precios se guardan como enteros CLP. El RUN valida dígito verificador. Los correos de cuentas admiten @duoc.cl, @profesor.duoc.cl y @gmail.com. Las contraseñas conservan la regla heredada del prototipo (4 a 10 caracteres), que deberá sustituirse al incorporar autenticación real. Región/comuna utiliza cuatro regiones de demostración, no el catálogo completo de Chile.

## 7. Arquitectura

React/Bootstrap presenta la interfaz. HashRouter resuelve rutas. ShopContext mantiene el estado compartido y ejecuta operaciones. repository.js valida reglas y construye el siguiente documento. storage.js realiza la lectura/escritura local. Las compras se simulan; no existen pagos ni comunicación con MySQL.

## 8. Validación y evolución

COBERTURA_TESTING.md registra las pruebas y porcentajes reales. La siguiente versión requiere revisar el esquema MySQL ya creado por el estudiante, implementar API con autorización y hashing, y realizar una importación validada de los datos exportados. Debe revisarse este ERS contra cualquier requisito adicional indicado por el docente.
