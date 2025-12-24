export interface User {
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  fullName?: string;
  avatarUrl?: string;
  bio?: string;
  createdAt?: string;
  updatedAt?: string;
  lastLoginAt?: string;
  emailVerified?: boolean;
  roles?: string[];
}

export interface Post {
  id: number;
  title: string;
  slug: string;
  content: string;
  excerpt?: string;
  status: string;
  viewCount: number;
  likeCount: number;
  commentCount: number;
  featured: boolean;
  createdAt: string;
  updatedAt: string;
  publishedAt?: string;
  authorId: number;
  authorUsername: string;
  authorAvatar?: string;
  tags: string[];
}

export interface Comment {
  id: number;
  content: string;
  postId: number;
  authorId: number;
  authorUsername: string;
  authorAvatar?: string;
  parentId?: number;
  likeCount: number;
  createdAt: string;
  updatedAt: string;
  replies?: Comment[];
}

export interface Tag {
  id: number;
  name: string;
  slug: string;
  postCount: number;
}

export interface PaginationResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  error?: string;
  timestamp?: string;
}

export interface AuthResponse {
  token: string;
  expiresAt: string;
  user: User;
}

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
}

export interface PostRequest {
  title: string;
  content: string;
  status?: string;
  tags?: string[];
  featured?: boolean;
}

export interface CommentRequest {
  content: string;
  parentId?: number;
}


