import { describe, it, expect } from '@jest/globals';
import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { BrowserRouter } from 'react-router-dom';
import { configureStore } from '@reduxjs/toolkit';
import App from '../App';
import authReducer from '../store/slices/authSlice';
import postsReducer from '../store/slices/postsSlice';
import commentsReducer from '../store/slices/commentsSlice';

const createMockStore = (initialState = {}) => {
  return configureStore({
    reducer: {
      auth: authReducer,
      posts: postsReducer,
      comments: commentsReducer,
    },
    preloadedState: initialState,
  });
};

describe('App', () => {
  it('renders without crashing', () => {
    const store = createMockStore();
    render(
      <Provider store={store}>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </Provider>
    );
    expect(screen.getByText(/Blog Platform/i)).toBeInTheDocument();
  });

  it('shows login button when not authenticated', () => {
    const store = createMockStore({
      auth: {
        user: null,
        token: null,
        isAuthenticated: false,
        isLoading: false,
        error: null,
      },
    });
    render(
      <Provider store={store}>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </Provider>
    );
    expect(screen.getByText(/Вход/i)).toBeInTheDocument();
  });
});

