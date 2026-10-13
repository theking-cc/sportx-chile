import ErrorBoundary from "./components/ErrorBoundary.jsx";
import React from "react";
import { createRoot } from "react-dom/client";
import { HashRouter } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";
import "./styles.css";
import App from "./App.jsx";
import { ShopProvider } from "./context/ShopContext.jsx";
createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <HashRouter>
      <ErrorBoundary>
        <ShopProvider>
          <App />
        </ShopProvider>
      </ErrorBoundary>
    </HashRouter>
  </React.StrictMode>,
);
