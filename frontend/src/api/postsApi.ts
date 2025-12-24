import apiClient from './api';
import type { ApiResponse, Post, PaginationResponse, PostRequest } from '../types';

export const postsApi = {
  getAllPosts: async (page = 0, size = 20, sort = 'publishedAt,desc') => {
    const response = await apiClient.get<ApiResponse<PaginationResponse<Post>>>('/posts', {
      params: { page, size, sort }
    });
    return response.data;
  },

  getPostById: async (id: number) => {
    const response = await apiClient.get<ApiResponse<Post>>(`/posts/${id}`);
    return response.data;
  },

  createPost: async (postData: PostRequest) => {
    const response = await apiClient.post<ApiResponse<Post>>('/posts', postData);
    return response.data;
  },

  updatePost: async (id: number, postData: PostRequest) => {
    const response = await apiClient.put<ApiResponse<Post>>(`/posts/${id}`, postData);
    return response.data;
  },

  deletePost: async (id: number) => {
    const response = await apiClient.delete<ApiResponse<void>>(`/posts/${id}`);
    return response.data;
  },

  searchPosts: async (query: string, page = 0, size = 20) => {
    const response = await apiClient.get<ApiResponse<PaginationResponse<Post>>>('/posts/search', {
      params: { q: query, page, size }
    });
    return response.data;
  },

  getPopularPosts: async (page = 0, size = 10) => {
    const response = await apiClient.get<ApiResponse<PaginationResponse<Post>>>('/posts/popular', {
      params: { page, size }
    });
    return response.data;
  },

  getPostsByTag: async (tagName: string, page = 0, size = 20) => {
    const response = await apiClient.get<ApiResponse<PaginationResponse<Post>>>(`/posts/tag/${tagName}`, {
      params: { page, size }
    });
    return response.data;
  },

  getPostsByAuthor: async (authorId: number, page = 0, size = 20) => {
    const response = await apiClient.get<ApiResponse<PaginationResponse<Post>>>(`/posts/author/${authorId}`, {
      params: { page, size }
    });
    return response.data;
  },

  likePost: async (id: number) => {
    const response = await apiClient.post<ApiResponse<{ liked: boolean }>>(`/posts/${id}/like`);
    return response.data;
  },

  getMyPosts: async (page = 0, size = 20) => {
    const response = await apiClient.get<ApiResponse<PaginationResponse<Post>>>('/posts/me', {
      params: { page, size }
    });
    return response.data;
  }
};


