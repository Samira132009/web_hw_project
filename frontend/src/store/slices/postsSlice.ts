import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { postsApi } from '../../api/postsApi';
import type { Post, PostRequest, PaginationResponse } from '../../types';

interface PostsState {
  posts: Post[];
  currentPost: Post | null;
  pagination: {
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
  };
  isLoading: boolean;
  error: string | null;
}

const initialState: PostsState = {
  posts: [],
  currentPost: null,
  pagination: {
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
  },
  isLoading: false,
  error: null,
};

export const fetchPosts = createAsyncThunk(
  'posts/fetchPosts',
  async ({ page = 0, size = 20 }: { page?: number; size?: number }) => {
    const response = await postsApi.getAllPosts(page, size);
    if (response.success && response.data) {
      return response.data;
    }
    throw new Error(response.error || 'Failed to fetch posts');
  }
);

export const fetchPostById = createAsyncThunk(
  'posts/fetchPostById',
  async (id: number) => {
    const response = await postsApi.getPostById(id);
    if (response.success && response.data) {
      return response.data;
    }
    throw new Error(response.error || 'Failed to fetch post');
  }
);

export const createPost = createAsyncThunk(
  'posts/createPost',
  async (postData: PostRequest) => {
    const response = await postsApi.createPost(postData);
    if (response.success && response.data) {
      return response.data;
    }
    throw new Error(response.error || 'Failed to create post');
  }
);

export const updatePost = createAsyncThunk(
  'posts/updatePost',
  async ({ id, data }: { id: number; data: PostRequest }) => {
    const response = await postsApi.updatePost(id, data);
    if (response.success && response.data) {
      return response.data;
    }
    throw new Error(response.error || 'Failed to update post');
  }
);

export const deletePost = createAsyncThunk(
  'posts/deletePost',
  async (id: number) => {
    const response = await postsApi.deletePost(id);
    if (response.success) {
      return id;
    }
    throw new Error(response.error || 'Failed to delete post');
  }
);

export const searchPosts = createAsyncThunk(
  'posts/searchPosts',
  async ({ query, page = 0, size = 20 }: { query: string; page?: number; size?: number }) => {
    const response = await postsApi.searchPosts(query, page, size);
    if (response.success && response.data) {
      return response.data;
    }
    throw new Error(response.error || 'Failed to search posts');
  }
);

const postsSlice = createSlice({
  name: 'posts',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    clearCurrentPost: (state) => {
      state.currentPost = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchPosts.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchPosts.fulfilled, (state, action) => {
        state.isLoading = false;
        state.posts = action.payload.content;
        state.pagination = {
          page: action.payload.page,
          size: action.payload.size,
          totalElements: action.payload.totalElements,
          totalPages: action.payload.totalPages,
        };
      })
      .addCase(fetchPosts.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Failed to fetch posts';
      })
      .addCase(fetchPostById.fulfilled, (state, action) => {
        state.currentPost = action.payload;
      })
      .addCase(createPost.fulfilled, (state, action) => {
        state.posts.unshift(action.payload);
      })
      .addCase(updatePost.fulfilled, (state, action) => {
        const index = state.posts.findIndex((p) => p.id === action.payload.id);
        if (index !== -1) {
          state.posts[index] = action.payload;
        }
        if (state.currentPost?.id === action.payload.id) {
          state.currentPost = action.payload;
        }
      })
      .addCase(deletePost.fulfilled, (state, action) => {
        state.posts = state.posts.filter((p) => p.id !== action.payload);
        if (state.currentPost?.id === action.payload) {
          state.currentPost = null;
        }
      })
      .addCase(searchPosts.fulfilled, (state, action) => {
        state.posts = action.payload.content;
        state.pagination = {
          page: action.payload.page,
          size: action.payload.size,
          totalElements: action.payload.totalElements,
          totalPages: action.payload.totalPages,
        };
      });
  },
});

export const { clearError, clearCurrentPost } = postsSlice.actions;
export default postsSlice.reducer;


