import Field from "./Field.jsx";
import { REGIONS } from "../data/seed.js";
export const emptyUser = {
  run: "",
  nombre: "",
  apellidos: "",
  correo: "",
  password: "",
  telefono: "",
  fechaNacimiento: "",
  region: "",
  comuna: "",
  direccion: "",
  rol: "Cliente",
};
export default function UserFields({
  value,
  onChange,
  errors = {},
  editing = false,
  admin = false,
}) {
  const comunas = REGIONS.find((r) => r.region === value.region)?.comunas || [];
  function change(name, val) {
    onChange(name, val);
    if (name === "region") onChange("comuna", "");
  }
  return (
    <>
      <div className="row">
        <div className="col-md-6">
          <Field
            label="RUN (sin puntos ni guion)"
            name="run"
            value={value.run}
            onChange={change}
            error={errors.run}
            readOnly={editing}
            maxLength="9"
          />
        </div>
        <div className="col-md-6">
          <Field
            label="Correo electrónico"
            name="correo"
            value={value.correo}
            onChange={change}
            error={errors.correo}
            type="email"
            maxLength="100"
          />
        </div>
      </div>
      <div className="row">
        <div className="col-md-6">
          <Field
            label="Nombre"
            name="nombre"
            value={value.nombre}
            onChange={change}
            error={errors.nombre}
            maxLength="50"
          />
        </div>
        <div className="col-md-6">
          <Field
            label="Apellidos"
            name="apellidos"
            value={value.apellidos}
            onChange={change}
            error={errors.apellidos}
            maxLength="100"
          />
        </div>
      </div>
      <Field
        label={
          editing
            ? "Nueva contraseña (opcional)"
            : "Contraseña (4 a 10 caracteres)"
        }
        name="password"
        value={value.password}
        onChange={change}
        error={errors.password}
        type="password"
        maxLength="10"
        autoComplete={editing ? "new-password" : "new-password"}
      />
      <div className="row">
        <div className="col-md-6">
          <Field
            label="Teléfono (opcional)"
            name="telefono"
            value={value.telefono}
            onChange={change}
            error={errors.telefono}
            type="tel"
            maxLength="15"
          />
        </div>
        <div className="col-md-6">
          <Field
            label="Fecha de nacimiento (opcional)"
            name="fechaNacimiento"
            value={value.fechaNacimiento}
            onChange={change}
            error={errors.fechaNacimiento}
            type="date"
          />
        </div>
      </div>
      <div className="row">
        <div className="col-md-6">
          <Field
            label="Región"
            name="region"
            value={value.region}
            onChange={change}
          >
            <option value="">Selecciona una región</option>
            {REGIONS.map((r) => (
              <option key={r.region}>{r.region}</option>
            ))}
          </Field>
        </div>
        <div className="col-md-6">
          <Field
            label="Comuna"
            name="comuna"
            value={value.comuna}
            onChange={change}
            error={errors.comuna}
            disabled={!value.region}
          >
            <option value="">Selecciona una comuna</option>
            {comunas.map((c) => (
              <option key={c}>{c}</option>
            ))}
          </Field>
        </div>
      </div>
      <Field
        label="Dirección"
        name="direccion"
        value={value.direccion}
        onChange={change}
        error={errors.direccion}
        type="textarea"
        maxLength="300"
      />
      {admin && (
        <Field label="Rol" name="rol" value={value.rol} onChange={change}>
          {["Cliente", "Vendedor", "Administrador"].map((role) => (
            <option key={role}>{role}</option>
          ))}
        </Field>
      )}
    </>
  );
}
