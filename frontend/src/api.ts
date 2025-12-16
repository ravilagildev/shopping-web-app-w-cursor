import axios from 'axios';
import { Person, Gift, BudgetSummary } from './types';

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

// Person API
export const personApi = {
  getAll: () => api.get<Person[]>('/persons'),
  getById: (id: number) => api.get<Person>(`/persons/${id}`),
  create: (person: { name: string }) => api.post<Person>('/persons', person),
  update: (id: number, person: { name: string }) => api.put<Person>(`/persons/${id}`, person),
  delete: (id: number) => api.delete(`/persons/${id}`),
};

// Gift API
export const giftApi = {
  getAll: () => api.get<Gift[]>('/gifts'),
  getById: (id: number) => api.get<Gift>(`/gifts/${id}`),
  getByPersonId: (personId: number) => api.get<Gift[]>(`/gifts/person/${personId}`),
  create: (gift: { description: string; price: number; personId: number }) =>
    api.post<Gift>('/gifts', gift),
  update: (id: number, gift: { description: string; price: number; personId: number }) =>
    api.put<Gift>(`/gifts/${id}`, gift),
  delete: (id: number) => api.delete(`/gifts/${id}`),
};

// Budget API
export const budgetApi = {
  getSummary: (totalBudget: number = 1000) =>
    api.get<BudgetSummary>(`/budget/summary?totalBudget=${totalBudget}`),
};

