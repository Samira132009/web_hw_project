import apiClient from './api';
import type { ApiResponse, User, PaginationResponse } from '../types';

export const usersApi = {
  getUserById: async (id: number) => {
    const response = await apiClient.get<ApiResponse<User>>(`/users/${id}`);
    return response.data;
  },

  getCurrentUser: async () => {
    const response = await apiClient.get<ApiResponse<User>>('/users/me');
    return response.data;
  },

  updateProfile: async (userData: Partial<User>) => {
    const response = await apiClient.put<ApiResponse<User>>('/users/me', userData);
    return response.data;
  },

  searchUsers: async (query: string, page = 0, size = 20) => {
    const response = await apiClient.get<ApiResponse<PaginationResponse<User>>>('/users/search', {
      params: { q: query, page, size }
    });
    return response.data;
  },

  getUserByUsername: async (username: string) => {
    const response = await apiClient.get<ApiResponse<User>>(`/users/username/${username}`);
    return response.data;
  }
};


