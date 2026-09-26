import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig(({ mode }) => {
  const { VITE_API_BASE_URL } = loadEnv(mode, process.cwd(), "VITE_");

  if (mode === "production") {
    if (!VITE_API_BASE_URL) {
      throw new Error("Set VITE_API_BASE_URL to the public HTTPS backend URL before building.");
    }

    const apiUrl = new URL(VITE_API_BASE_URL);
    if (apiUrl.protocol !== "https:") {
      throw new Error("VITE_API_BASE_URL must use HTTPS for production.");
    }
  }

  return { plugins: [react()] };
});
