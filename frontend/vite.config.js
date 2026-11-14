import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
// En desarrollo, /api se redirige al backend Spring Boot.
// SPORTX_API_TARGET permite otro puerto (por defecto http://localhost:8080).
const apiTarget = process.env.SPORTX_API_TARGET || "http://localhost:8080";
export default defineConfig({
  plugins: [react()],
  base: "./",
  server: {
    proxy: {
      "/api": { target: apiTarget, changeOrigin: false },
    },
  },
});
