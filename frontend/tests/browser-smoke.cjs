const { chromium } = require("playwright");
const assert = require("node:assert/strict");
(async () => {
  require("node:fs").mkdirSync("tests/browser-output", { recursive: true });
  const { createServer } = await import("vite");
  const server = await createServer({
    root: process.cwd(),
    server: {
      host: "127.0.0.1",
      port: 4175,
      strictPort: true,
      watch: { ignored: ["**/coverage/**"] },
    },
  });
  await server.listen();
  const browser = await chromium.launch({
    ...(process.env.CHROME_BIN
      ? { executablePath: process.env.CHROME_BIN }
      : {}),
    headless: true,
    args: ["--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu"],
  });
  const page = await browser.newPage({
    viewport: { width: 1280, height: 900 },
  });
  const errors = [];
  page.on("pageerror", (e) => errors.push(e.message));
  const base = "http://127.0.0.1:4175/";
  const go = async (path) => {
    await page.goto(base + "#" + path);
    await page.waitForTimeout(150);
  };
  const title = async (text) =>
    page.getByRole("heading", { name: text, exact: true }).waitFor();
  const signIn = async (email, password) => {
    await go("/login");
    await page.getByLabel("Correo electrónico").fill(email);
    await page.getByLabel("Contraseña", { exact: true }).fill(password);
    await page.getByRole("button", { name: "Ingresar", exact: true }).click();
  };
  const signInDemo = async (role) => {
    await go("/login");
    await page.getByText("Acceso de demostración", { exact: true }).click();
    await page
      .getByRole("button", { name: "Entrar como " + role, exact: true })
      .click();
  };
  const stored = () =>
    page.evaluate(() => JSON.parse(localStorage.getItem("sportx_state_v2")));
  await go("/");
  await title("Productos destacados");
  await go("/nosotros");
  await title("Nosotros");
  await go("/blogs");
  await title("Blog SportX");
  await page.getByRole("link", { name: "Leer artículo" }).first().click();
  await title("Cómo elegir tus primeros guantes de boxeo");
  await go("/contacto");
  await page.getByLabel("Nombre", { exact: true }).fill("Consulta Smoke");
  await page.getByLabel("Correo electrónico").fill("consulta@gmail.com");
  await page.getByLabel("Mensaje").fill("Prueba de consulta");
  await page.getByRole("button", { name: "Guardar consulta" }).click();
  assert.equal((await stored()).messages.length, 1);
  await go("/productos");
  await page.getByLabel("Buscar producto").fill("Cuerda");
  await page
    .getByRole("link", { name: "Cuerda de Batalla Functional 12m" })
    .click();
  await page.getByLabel("Cantidad").fill("2");
  await page.getByRole("button", { name: "Agregar al carrito" }).click();
  await go("/carrito");
  await page.getByRole("button", { name: /Aumentar/ }).click();
  assert.equal((await stored()).cart[0].cantidad, 2);
  await page.getByLabel("Cupón de descuento").fill("DUOC10");
  await page.getByRole("button", { name: "Aplicar", exact: true }).click();
  await page.getByRole("link", { name: "Finalizar compra" }).click();
  await title("Iniciar sesión");
  await page.getByText("Acceso de demostración", { exact: true }).click();
  await page
    .getByRole("button", { name: "Entrar como Cliente", exact: true })
    .click();
  await title("Finalizar compra");
  assert.equal(
    await page.getByLabel("Cupón (opcional)").inputValue(),
    "DUOC10",
  );
  await page.getByRole("button", { name: "Confirmar pedido" }).click();
  await title("¡Pedido confirmado!");
  let db = await stored();
  assert.equal(db.orders.length, 1);
  assert.equal(db.cart.length, 0);
  assert.equal(db.products[1].stock, 0);
  assert.equal(db.orders[0].total, 82782);
  const orderId = db.orders[0].id;
  await go("/pedidos");
  await title("Mis pedidos");
  await page.getByRole("link", { name: "Ver pedido" }).click();
  await title("Detalle de mi pedido");
  await page.reload();
  await title("Detalle de mi pedido");
  await go("/perfil");
  await page.getByLabel("Nombre", { exact: true }).fill("Cliente Actualizado");
  await page.getByRole("button", { name: "Guardar cambios" }).click();
  assert.equal(
    (await stored()).users.find((u) => u.id === "demo-client").nombre,
    "Cliente Actualizado",
  );
  await go("/admin/usuarios");
  await title("Acceso restringido");
  await page.getByRole("button", { name: "Salir", exact: true }).click();
  await go("/registro");
  await page.getByLabel("RUN").fill("123456785");
  await page.getByLabel("Correo electrónico").fill("registrado@gmail.com");
  await page.getByLabel("Nombre", { exact: true }).fill("Registrado");
  await page.getByLabel("Apellidos").fill("Prueba");
  await page
    .getByLabel("Contraseña (4 a 10 caracteres)", { exact: true })
    .fill("1234");
  await page.getByLabel("Confirmar contraseña").fill("1234");
  await page
    .getByLabel("Región", { exact: true })
    .selectOption({ label: "Región Metropolitana de Santiago" });
  await page.getByLabel("Comuna", { exact: true }).selectOption("Santiago");
  await page.getByLabel("Dirección", { exact: true }).fill("Calle Prueba 123");
  await page.getByRole("button", { name: "Crear cuenta" }).click();
  await title("Iniciar sesión");
  await signIn("registrado@gmail.com", "1234");
  await go("/pedidos/" + orderId);
  await title("Pedido no encontrado");
  await page.getByRole("button", { name: "Salir", exact: true }).click();
  await signInDemo("Administrador");
  await title("Panel administrativo");
  await page.getByText("Consulta Smoke · consulta@gmail.com").waitFor();
  await go("/admin/productos");
  await title("Administración de productos");
  await page.getByRole("link", { name: "Nuevo producto" }).click();
  await title("Nuevo producto");
  for (const [label, value] of [
    ["Código", "TEST-001"],
    ["Nombre", "Producto Smoke"],
    ["Precio (CLP)", "12000"],
    ["Stock", "5"],
    ["Stock crítico", "1"],
  ])
    await page.getByLabel(label, { exact: true }).fill(value);
  await page.getByLabel("Categoría").selectOption("Fitness");
  await page.getByRole("button", { name: "Guardar producto" }).click();
  await title("Administración de productos");
  await page
    .getByRole("row")
    .filter({ hasText: "Producto Smoke" })
    .getByRole("link", { name: "Editar" })
    .click();
  await title("Editar producto");
  await page.getByLabel("Nombre", { exact: true }).fill("Producto Editado");
  await page.getByRole("button", { name: "Guardar producto" }).click();
  await title("Administración de productos");
  page.once("dialog", (d) => d.accept());
  await page
    .getByRole("row")
    .filter({ hasText: "Producto Editado" })
    .getByRole("button", { name: "Eliminar" })
    .click();
  assert.ok(!(await stored()).products.some((p) => p.codigo === "TEST-001"));
  await go("/admin/usuarios");
  await title("Administración de usuarios");
  await page.getByRole("link", { name: "Nuevo usuario" }).click();
  await title("Nuevo usuario");
  for (const [label, value] of [
    ["RUN (sin puntos ni guion)", "876543214"],
    ["Correo electrónico", "admincreado@gmail.com"],
    ["Nombre", "Creado Admin"],
    ["Apellidos", "Prueba"],
    ["Contraseña (4 a 10 caracteres)", "1234"],
    ["Dirección", "Prueba 456"],
  ])
    await page.getByLabel(label, { exact: true }).fill(value);
  await page
    .getByLabel("Región", { exact: true })
    .selectOption("Región Metropolitana de Santiago");
  await page.getByLabel("Comuna", { exact: true }).selectOption("Santiago");
  await page.getByRole("button", { name: "Guardar usuario" }).click();
  await title("Administración de usuarios");
  await page
    .getByRole("row")
    .filter({ hasText: "admincreado@gmail.com" })
    .getByRole("link", { name: "Editar" })
    .click();
  await title("Editar usuario");
  await page.getByLabel("Rol", { exact: true }).selectOption("Vendedor");
  await page.getByRole("button", { name: "Guardar usuario" }).click();
  await title("Administración de usuarios");
  page.once("dialog", (d) => d.accept());
  await page
    .getByRole("row")
    .filter({ hasText: "admincreado@gmail.com" })
    .getByRole("button", { name: "Eliminar" })
    .click();
  await go("/admin/pedidos");
  await title("Administración de pedidos");
  await page.getByLabel("Estado del pedido").selectOption("Cancelado");
  db = await stored();
  assert.equal(db.products[1].stock, 2);
  assert.equal(db.orders[0].status, "Cancelado");
  await page.getByRole("button", { name: "Salir", exact: true }).click();
  await signInDemo("Vendedor");
  await go("/admin/usuarios");
  await title("Acceso restringido");
  await go("/admin/productos");
  await title("Administración de productos");
  await page.getByRole("button", { name: "Salir", exact: true }).click();
  await go("/");
  await page.screenshot({
    path: "tests/browser-output/desktop.png",
    fullPage: true,
  });
  await page.setViewportSize({ width: 375, height: 812 });
  await page.getByRole("button", { name: "Abrir menú" }).click();
  await page.getByRole("link", { name: "Productos", exact: true }).click();
  await title("Nuestros productos");
  assert.ok(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= innerWidth,
    ),
  );
  await page.screenshot({
    path: "tests/browser-output/mobile.png",
    fullPage: true,
  });
  assert.deepEqual(errors, []);
  console.log(
    "OK: 21 vistas, compra con cupón tras login, inventario, historial persistente, perfil, registro, CRUD de productos/usuarios, cancelación, permisos de cliente/vendedor y navegación móvil.",
  );
  await browser.close();
  await server.close();
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
