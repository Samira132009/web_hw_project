import apiClient from './api';
import { tokenStorage } from '../utils/tokenStorage';
import type { ApiResponse, AuthResponse, LoginRequest, RegisterRequest } from '../types';

export const authApi = {
  login: async (credentials: LoginRequest): Promise<ApiResponse<AuthResponse>> => {
    const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/login', credentials);
    
    if (response.data.success && response.data.data?.token) {
      tokenStorage.setToken(response.data.data.token);
      if (response.data.data.user) {
        tokenStorage.setUser(response.data.data.user);
      }
    }
    
    return response.data;
  },

  register: async (userData: RegisterRequest): Promise<ApiResponse<AuthResponse>> => {
    const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/register', userData);
    
    if (response.data.success && response.data.data?.token) {
      tokenStorage.setToken(response.data.data.token);
      if (response.data.data.user) {
        tokenStorage.setUser(response.data.data.user);
      }
    }
    
    return response.data;
  },

  getCurrentUser: async (): Promise<ApiResponse<any>> => {
    const response = await apiClient.get<ApiResponse<any>>('/auth/me');
    return response.data;
  },

  logout: (): void => {
    tokenStorage.clear();
    window.location.href = '/login';
  },

  isAuthenticated: (): boolean => {
    return tokenStorage.hasToken();
  }
};


