# SportX Chile - Evaluación Parcial 2

Frontend de tienda deportiva migrado a **React + Vite + Bootstrap**, con 21 vistas y persistencia en localStorage. El prototipo HTML anterior permanece en la raíz como referencia. La aplicación actual está en `frontend/`.

## Ejecutar en VS Code

Requiere Node.js 22.12 o superior (recomendado Node.js 24 LTS).

```sh
git fetch origin
git switch migracion-react
git pull --ff-only origin migracion-react
cd frontend
npm install
npm run dev
```

Abre la dirección que muestra Vite. No uses Live Server para este frontend.

```sh
npm run build
npm run preview
npm test
npm run test:logic
```

`npm test` usa Jasmine/Karma y Chrome instalado en tu computador. Genera `frontend/coverage/html/index.html`. Si Chrome no se detecta en Windows, antes de ejecutar las pruebas usa en PowerShell:

```powershell
$env:CHROME_BIN = 'C:\Program Files\Google\Chrome\Application\chrome.exe'
npm test
```

Si Chrome está en otra carpeta, ajusta esa ruta.

Para repetir el recorrido completo de navegador, desde `frontend/`:

```sh
npx playwright install chromium
npm run test:e2e
```

El recorrido crea datos de prueba en un navegador aislado, comprueba los flujos de las 21 vistas y guarda dos capturas en `tests/browser-output/`. No cambia los datos de tu navegador habitual. Arranca su propio servidor Vite en el puerto 4175.

## Acceso de demostración

En Iniciar sesión, abre **Acceso de demostración** y elige **Entrar como Administrador**, **Entrar como Vendedor** o **Entrar como Cliente**. Son cuentas ficticias para probar los roles y no publican contraseñas. El formulario de login también permite entrar con cuentas que registres tú mismo.

Las cuentas demo se incluyen al inicializar los datos. Si se editan o eliminan después, se respeta ese cambio.

## Funcionalidades

- 21 vistas: tienda, cuenta del cliente y administración.
- Catálogo compartido con filtros, detalle, cantidades y alertas de stock.
- Carrito, cupones, checkout simulado, confirmación e historial.
- Al confirmar, pedido, stock y carrito se guardan mediante una sola escritura.
- Cancelación administrativa con devolución de stock una sola vez.
- Login, registro con RUN y correo, perfil editable y permisos por rol.
- CRUD de productos y usuarios, panel con datos reales del prototipo y gestión de pedidos.
- Contacto guardado localmente y visible para el administrador.
- Respaldo JSON desde el panel administrativo para preparar la migración posterior.
- Bootstrap responsivo, navegación con HashRouter y estados/propiedades React.

## Datos y migración

La versión actual usa `sportx_state_v2` para productos, usuarios, carrito, pedidos y consultas. La sesión guarda únicamente el ID en `sportx_session_v2`. Al iniciar por primera vez se leen `sportx_products`, `sportx_users` y `sportx_cart_items` si existen en el mismo origen. Las claves anteriores no se borran. Una vez creado el documento v2, este es la fuente de datos de la aplicación.

**Cambiar de Live Server a Vite cambia el origen y no transfiere automáticamente los datos.** Conserva el almacenamiento anterior y exporta tus datos antes de la migración. El panel exporta un respaldo de la versión actual; todavía no hay un importador de respaldos externos.

El prototipo conserva las contraseñas de las cuentas registradas en datos locales y comprueba roles en el frontend. Este acceso sirve para la evaluación, no constituye autenticación segura para producción. En la etapa MySQL se implementarán hashing de contraseñas, autenticación y autorización en el backend. React se comunicará con una API, no directamente con MySQL.

No hay cobros reales, servicio de despacho ni envío de correos. Las fotos se cargan desde URLs externas; si fallan se muestra una imagen alternativa local incluida en el código.

## Documentación para la evaluación

- [Inventario de 21 vistas](docs/VISTAS.md)
- [ERS: propuesta de requisitos](docs/ERS.md)
- [Cobertura y resultados de testing](docs/COBERTURA_TESTING.md)
- [Evidencia de ejecución](docs/EVIDENCIA_TESTING.txt)
- [Guion de presentación](docs/PRESENTACION.md)
- [Plan de conexión a MySQL](docs/MIGRACION_MYSQL.md)

El ERS es la propuesta basada en este proyecto; revisa que coincida con los requisitos particulares que haya indicado tu profesor. MySQL se conectará después de revisar el esquema existente del usuario.

## Estructura del frontend

- `src/components/`: controles compartidos, navegación, validación visual y protección de rutas.
- `src/pages/`: 21 pantallas.
- `src/context/ShopContext.jsx`: estado compartido y operaciones de la aplicación.
- `src/services/repository.js`: operaciones de usuarios, catálogo, pedidos y contactos.
- `src/services/storage.js`: lectura/escritura de localStorage.
- `src/services/cart.js`: cantidades y cálculos de descuentos.
- `src/services/validation.js`: reglas de formularios.
- `tests/`: componentes, regresiones, vistas y recorrido de navegador.

Documentación oficial: https://react.dev/, https://vite.dev/guide/, https://jasmine.github.io/ y https://karma-runner.github.io/6.4/index.html.
