import axios from 'axios';
import { Roaster, Coffee, InventorySummary, RoastLevel } from './types';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach token if present
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/';
    }
    return Promise.reject(error);
  }
);

// Auth API
export const authApi = {
  login: (username: string, password: string) =>
    api.post<{ token: string; type: string }>('/auth/login', { username, password }),
  logout: () => {
    localStorage.removeItem('token');
  },
  isAuthenticated: () => !!localStorage.getItem('token'),
};

// Roaster API
export const roasterApi = {
  getAll: () => api.get<Roaster[]>('/roasters'),
  getById: (id: number) => api.get<Roaster>(`/roasters/${id}`),
  create: (roaster: { name: string; location?: string; website?: string; notes?: string }) =>
    api.post<Roaster>('/roasters', roaster),
  update: (id: number, roaster: { name: string; location?: string; website?: string; notes?: string }) =>
    api.put<Roaster>(`/roasters/${id}`, roaster),
  delete: (id: number) => api.delete(`/roasters/${id}`),
};

// Coffee API
export const coffeeApi = {
  getAll: () => api.get<Coffee[]>('/coffees'),
  getById: (id: number) => api.get<Coffee>(`/coffees/${id}`),
  getByRoasterId: (roasterId: number) => api.get<Coffee[]>(`/coffees/roaster/${roasterId}`),
  create: (coffee: {
    coffeeName: string;
    roastDate: string;
    purchaseDate: string;
    initialWeight: number;
    currentWeight?: number;
    origin?: string;
    roastLevel?: RoastLevel;
    processingMethod?: string;
    price?: number;
    notes?: string;
    roasterId: number;
  }) => api.post<Coffee>('/coffees', coffee),
  update: (id: number, coffee: {
    coffeeName: string;
    roastDate: string;
    purchaseDate: string;
    initialWeight: number;
    currentWeight: number;
    origin?: string;
    roastLevel?: RoastLevel;
    processingMethod?: string;
    price?: number;
    notes?: string;
    roasterId: number;
  }) => api.put<Coffee>(`/coffees/${id}`, coffee),
  consume: (id: number, amount: number) =>
    api.post<Coffee>(`/coffees/${id}/consume?amount=${amount}`),
  delete: (id: number) => api.delete(`/coffees/${id}`),
};

// Inventory API
export const inventoryApi = {
  getSummary: () => api.get<InventorySummary>('/inventory/summary'),
};
