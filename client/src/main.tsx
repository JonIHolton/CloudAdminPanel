import "inter-ui/inter.css";

import * as React from "react";
import * as ReactDOM from "react-dom/client";
import { HelmetProvider } from "react-helmet-async";
import { BrowserRouter } from "react-router-dom";
import { ThemeProvider } from "@opengovsg/design-system-react";
import { GoogleOAuthProvider } from "@react-oauth/google";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";

import customTheme from "~shared/theme";

import { AuthProvider } from "~features/auth";

import App from "./App";

const helmetContext = {};

const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID;

const tableClient = new QueryClient();

const rootElement = document.getElementById("root");
if (rootElement) {
  ReactDOM.createRoot(rootElement).render(
    <React.StrictMode>
      <GoogleOAuthProvider clientId={clientId}>
        <QueryClientProvider client={tableClient}>
          <ThemeProvider theme={customTheme}>
            <BrowserRouter>
              <AuthProvider>
                <HelmetProvider context={helmetContext}>
                  <App />
                </HelmetProvider>
              </AuthProvider>
            </BrowserRouter>
          </ThemeProvider>
        </QueryClientProvider>
      </GoogleOAuthProvider>
    </React.StrictMode>,
  );
}
