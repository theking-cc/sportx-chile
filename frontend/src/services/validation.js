import { REGIONS } from "../data/seed.js";
// js/validations.js

// 1. Validación de dominios de correo institucional/gmail
export function validarEmailDominio(correo) {
  const dominiosPermitidos = ["@duoc.cl", "@profesor.duoc.cl", "@gmail.com"];
  const regexFormato = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!regexFormato.test(correo)) return false;
  return dominiosPermitidos.some((dominio) =>
    correo.toLowerCase().endsWith(dominio),
  );
}

// 2. Validación de RUN chileno (7 a 9 caracteres alfanuméricos, sin puntos ni guion)
export function validarRunChileno(runCompleto) {
  const regexFormato = /^[0-9]{6,8}[0-9kK]{1}$/;
  if (!regexFormato.test(runCompleto)) return false;

  const cuerpo = runCompleto.slice(0, -1);
  const dv = runCompleto.slice(-1).toUpperCase();

  let suma = 0;
  let multiplo = 2;
  for (let i = cuerpo.length - 1; i >= 0; i--) {
    suma += parseInt(cuerpo.charAt(i), 10) * multiplo;
    multiplo = multiplo === 7 ? 2 : multiplo + 1;
  }

  const resto = 11 - (suma % 11);
  let dvEsperado = "0";
  if (resto === 11) dvEsperado = "0";
  else if (resto === 10) dvEsperado = "K";
  else dvEsperado = resto.toString();

  return dv === dvEsperado;
}

export function validateUser(user, { passwordRequired = true } = {}) {
  const errors = {};
  if (!validarRunChileno(user.run || ""))
    errors.run = "Ingresa un RUN válido sin puntos ni guion.";
  if (!user.nombre?.trim() || user.nombre.length > 50)
    errors.nombre = "Nombre obligatorio, máximo 50 caracteres.";
  if (!user.apellidos?.trim() || user.apellidos.length > 100)
    errors.apellidos = "Apellidos obligatorios, máximo 100 caracteres.";
  if (!validarEmailDominio(user.correo || "") || user.correo.length > 100)
    errors.correo = "Usa un correo @duoc.cl, @profesor.duoc.cl o @gmail.com.";
  if (
    (passwordRequired || user.password) &&
    (!user.password || user.password.length < 4 || user.password.length > 10)
  )
    errors.password = "La contraseña debe tener entre 4 y 10 caracteres.";
  if (
    !REGIONS.find((r) => r.region === user.region)?.comunas.includes(
      user.comuna,
    )
  )
    errors.comuna = "Selecciona región y comuna.";
  if (!user.direccion?.trim() || user.direccion.length > 300)
    errors.direccion = "Dirección obligatoria, máximo 300 caracteres.";
  if (user.telefono && !/^\+?[0-9\s-]{8,15}$/.test(user.telefono))
    errors.telefono = "Ingresa un teléfono válido.";
  if (
    user.fechaNacimiento &&
    (Number.isNaN(Date.parse(user.fechaNacimiento)) ||
      new Date(user.fechaNacimiento) > new Date())
  )
    errors.fechaNacimiento = "La fecha no puede estar en el futuro.";
  return errors;
}
export function validateProduct(product) {
  const errors = {};
  if (!product.codigo?.trim() || product.codigo.length < 3)
    errors.codigo = "Código obligatorio, mínimo 3 caracteres.";
  if (!product.nombre?.trim() || product.nombre.length > 100)
    errors.nombre = "Nombre obligatorio, máximo 100 caracteres.";
  if ((product.descripcion || "").length > 500)
    errors.descripcion = "Máximo 500 caracteres.";
  for (const key of ["precio", "stock", "stockCritico"])
    if (
      product[key] === "" ||
      !Number.isInteger(Number(product[key])) ||
      Number(product[key]) < 0
    )
      errors[key] = "Ingresa un entero mayor o igual a cero.";
  if (
    !["Boxeo", "Fitness", "Calzado", "Accesorios"].includes(product.categoria)
  )
    errors.categoria = "Selecciona una categoría.";
  if (product.imagen && !/^https?:\/\//i.test(product.imagen))
    errors.imagen = "Usa una dirección http o https.";
  return errors;
}
