// API configuration - update API_BASE_URL after deploy
export const API_BASE_URL = "http://REPLACE_WITH_API_URL_OR_IP:8000"; // e.g. http://192.168.1.10:8000 or https://api.example.com
export const AUTH_HEADER = (token) => ({ Authorization: `Bearer ${token}` });
