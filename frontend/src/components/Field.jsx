export default function Field({
  label,
  name,
  value,
  onChange,
  error,
  type = "text",
  children,
  ...props
}) {
  const id = "field-" + name;
  const common = {
    id,
    name,
    value: value ?? "",
    onChange: (e) => onChange(name, e.target.value),
    "aria-invalid": !!error,
    "aria-describedby": error ? id + "-error" : undefined,
    ...props,
  };
  return (
    <div className="mb-3">
      <label htmlFor={id} className="form-label">
        {label}
      </label>
      {children ? (
        <select
          {...common}
          className={"form-select " + (error ? "is-invalid" : "")}
        >
          {children}
        </select>
      ) : type === "textarea" ? (
        <textarea
          {...common}
          rows="3"
          className={"form-control " + (error ? "is-invalid" : "")}
        />
      ) : (
        <input
          {...common}
          type={type}
          className={"form-control " + (error ? "is-invalid" : "")}
        />
      )}{" "}
      {error && (
        <div id={id + "-error"} className="invalid-feedback">
          {error}
        </div>
      )}
    </div>
  );
}
