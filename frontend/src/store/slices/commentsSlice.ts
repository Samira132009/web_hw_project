import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { commentsApi } from '../../api/commentsApi';
import type { Comment, CommentRequest } from '../../types';

interface CommentsState {
  comments: Comment[];
  isLoading: boolean;
  error: string | null;
}

const initialState: CommentsState = {
  comments: [],
  isLoading: false,
  error: null,
};

export const fetchPostComments = createAsyncThunk(
  'comments/fetchPostComments',
  async (postId: number) => {
    const response = await commentsApi.getPostComments(postId);
    if (response.success && response.data) {
      return response.data;
    }
    throw new Error(response.error || 'Failed to fetch comments');
  }
);

export const createComment = createAsyncThunk(
  'comments/createComment',
  async ({ postId, data }: { postId: number; data: CommentRequest }) => {
    const response = await commentsApi.createComment(postId, data);
    if (response.success && response.data) {
      return response.data;
    }
    throw new Error(response.error || 'Failed to create comment');
  }
);

export const deleteComment = createAsyncThunk(
  'comments/deleteComment',
  async (id: number) => {
    const response = await commentsApi.deleteComment(id);
    if (response.success) {
      return id;
    }
    throw new Error(response.error || 'Failed to delete comment');
  }
);

const commentsSlice = createSlice({
  name: 'comments',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    clearComments: (state) => {
      state.comments = [];
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchPostComments.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchPostComments.fulfilled, (state, action) => {
        state.isLoading = false;
        state.comments = action.payload;
      })
      .addCase(fetchPostComments.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Failed to fetch comments';
      })
      .addCase(createComment.fulfilled, (state, action) => {
        state.comments.push(action.payload);
      })
      .addCase(deleteComment.fulfilled, (state, action) => {
        state.comments = state.comments.filter((c) => c.id !== action.payload);
      });
  },
});

export const { clearError, clearComments } = commentsSlice.actions;
export default commentsSlice.reducer;


