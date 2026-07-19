import axios from 'axios';
const ApiClient = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_BACKEND_URL || "http://localhost:8080/api/v1",
    headers: {
        "Content-Type": "application/json"
    },
    withCredentials: true,
    timeout: 10000,
});

export default ApiClient;