import axios, { AxiosInstance, AxiosResponse } from "axios";
// Additional imports may be required depending on your logout implementation

const BASE_URL = import.meta.env.VITE_API_BASE_URL;

export const api: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 300000,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true,
});

// Response interceptor to check for specific 401 error
api.interceptors.response.use(
  (response: AxiosResponse) => {
    // Successful response, pass it through
    return response;
  },
  (error) => {
    if (
      error.response?.status === 401 &&
      error.response?.data?.error === "Invalid Token"
    ) {
      /*
       * Here you would trigger the logout
       * For example, emit a custom event that your application listens for
       */
      window.dispatchEvent(new CustomEvent("invalid-token-detected"));
    }
    // Always reject the error for downstream catch blocks to handle
    return Promise.reject(error);
  },
);

export async function handleResponse<T>(
  response: AxiosResponse<T>,
): Promise<T> {
  if (response.status >= 200 && response.status < 300) {
    return response.data;
  }
  throw new Error(`HTTP error! Status: ${response.status}`);
}
