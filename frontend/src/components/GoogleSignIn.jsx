import { useEffect, useRef, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useShop } from "../context/ShopContext.jsx";

// ID de cliente OAuth "Aplicación web" de Google Cloud (frontend/.env.local).
export const GOOGLE_CLIENT_ID = import.meta.env?.VITE_GOOGLE_CLIENT_ID || "";
const GSI_SRC = "https://accounts.google.com/gsi/client";
let gsiPromise = null;

function loadGsi() {
  if (window.google?.accounts?.id) return Promise.resolve();
  if (!gsiPromise)
    gsiPromise = new Promise((resolve, reject) => {
      const s = document.createElement("script");
      s.src = GSI_SRC;
      s.async = true;
      s.onload = resolve;
      s.onerror = () => {
        gsiPromise = null;
        reject(new Error("No se pudo cargar el inicio de sesión de Google."));
      };
      document.head.appendChild(s);
    });
  return gsiPromise;
}

// Botón oficial de Google Identity Services. Solo aparece con backend activo
// y un client ID configurado.
export default function GoogleSignIn({ text = "signin_with" }) {
  const { online, signInGoogle } = useShop();
  const navigate = useNavigate();
  const location = useLocation();
  const box = useRef(null);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const enabled = online === true && !!GOOGLE_CLIENT_ID;

  useEffect(() => {
    if (!enabled) return;
    let cancelled = false;
    loadGsi()
      .then(() => {
        if (cancelled || !box.current) return;
        window.google.accounts.id.initialize({
          client_id: GOOGLE_CLIENT_ID,
          callback: async ({ credential }) => {
            setError("");
            setBusy(true);
            try {
              const result = await signInGoogle(credential);
              if (result.pendiente) {
                navigate("/registro-google", {
                  state: { credential, ...result, from: location.state?.from },
                });
                return;
              }
              navigate(
                location.state?.from ||
                  (result.rol === "Cliente" ? "/perfil" : "/admin"),
                { replace: true, state: location.state?.nextState },
              );
            } catch (err) {
              setError(err.message);
              setBusy(false);
            }
          },
        });
        window.google.accounts.id.renderButton(box.current, {
          theme: "outline",
          size: "large",
          text,
          shape: "rectangular",
          logo_alignment: "left",
          width: box.current.offsetWidth || 320,
          locale: "es",
        });
      })
      .catch((err) => !cancelled && setError(err.message));
    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [enabled]);

  if (!enabled) return null;
  return (
    <div className="google-signin">
      <div className="google-signin__divider" aria-hidden="true">
        <span>o</span>
      </div>
      <div ref={box} className="google-signin__button" aria-busy={busy} />
      {busy && <p className="small text-muted mt-2">Conectando con Google…</p>}
      {error && (
        <p role="alert" className="alert alert-danger mt-2">
          {error}
        </p>
      )}
    </div>
  );
}
