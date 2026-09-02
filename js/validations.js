// js/validations.js

// 1. Validación de dominios de correo institucional/gmail
function validarEmailDominio(correo) {
  const dominiosPermitidos = ["@duoc.cl", "@profesor.duoc.cl", "@gmail.com"];
  const regexFormato = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!regexFormato.test(correo)) return false;
  return dominiosPermitidos.some(dominio => correo.toLowerCase().endsWith(dominio));
}

// 2. Validación de RUN chileno (7 a 9 caracteres alfanuméricos, sin puntos ni guion)
function validarRunChileno(runCompleto) {
  const regexFormato = /^[0-9]{7,8}[0-9kK]{1}$/;
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

// 3. Renderizado contextual de errores (Bootstrap feedback)
function setCampoInvalido(inputElement, errorElement, mensaje) {
  inputElement.classList.add("is-invalid");
  inputElement.classList.remove("is-valid");
  errorElement.textContent = mensaje;
  errorElement.classList.add("d-block");
}

function setCampoValido(inputElement, errorElement) {
  inputElement.classList.remove("is-invalid");
  inputElement.classList.add("is-valid");
  errorElement.textContent = "";
  errorElement.classList.remove("d-block");
}

// 4. Poblado dinámico de Selects Región -> Comuna
function inicializarSelectsUbicacion(selectRegionId, selectComunaId) {
  const selectRegion = document.getElementById(selectRegionId);
  const selectComuna = document.getElementById(selectComunaId);
  if (!selectRegion || !selectComuna) return;

  selectRegion.innerHTML = '<option value="">-- Seleccione una región --</option>';
  REGIONES_COMUNAS.forEach(reg => {
    selectRegion.innerHTML += `<option value="${reg.region}">${reg.region}</option>`;
  });

  selectRegion.addEventListener("change", () => {
    const regionSeleccionada = selectRegion.value;
    selectComuna.innerHTML = '<option value="">-- Seleccione una comuna --</option>';

    if (!regionSeleccionada) {
      selectComuna.disabled = true;
      return;
    }

    const regObj = REGIONES_COMUNAS.find(r => r.region === regionSeleccionada);
    if (regObj) {
      regObj.comunas.forEach(comuna => {
        selectComuna.innerHTML += `<option value="${comuna}">${comuna}</option>`;
      });
      selectComuna.disabled = false;
    }
  });
}