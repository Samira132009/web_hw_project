import apiClient from './api';
import type { ApiResponse, Comment, CommentRequest, PaginationResponse } from '../types';

export const commentsApi = {
  getPostComments: async (postId: number) => {
    const response = await apiClient.get<ApiResponse<Comment[]>>(`/comments/post/${postId}`);
    return response.data;
  },

  getCommentById: async (id: number) => {
    const response = await apiClient.get<ApiResponse<Comment>>(`/comments/${id}`);
    return response.data;
  },


  createComment: async (postId: number, commentData: CommentRequest) => {
    const response = await apiClient.post<ApiResponse<Comment>>('/comments', {
      ...commentData,
      postId
    });
    return response.data;
  },

  replyToComment: async (parentId: number, commentData: CommentRequest) => {
    const response = await apiClient.post<ApiResponse<Comment>>(`/comments/${parentId}/reply`, commentData);
    return response.data;
  },

  updateComment: async (id: number, commentData: CommentRequest) => {
    const response = await apiClient.put<ApiResponse<Comment>>(`/comments/${id}`, commentData);
    return response.data;
  },

  deleteComment: async (id: number) => {
    const response = await apiClient.delete<ApiResponse<void>>(`/comments/${id}`);
    return response.data;
  },

  likeComment: async (id: number) => {
    const response = await apiClient.post<ApiResponse<{ liked: boolean }>>(`/comments/${id}/like`);
    return response.data;
  }
};


