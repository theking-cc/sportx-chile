// Esta capa se reemplazará por llamadas al backend cuando conectemos MySQL.
export function read(key, fallback) {
  const raw = localStorage.getItem(key);
  if (raw === null) return fallback;
  try {
    return JSON.parse(raw);
  } catch {
    throw new Error(
      "Los datos guardados no se pueden leer. Revisa el almacenamiento antes de continuar.",
    );
  }
}
export function write(key, value) {
  localStorage.setItem(key, JSON.stringify(value));
}
