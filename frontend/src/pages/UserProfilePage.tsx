import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Box, Typography, Paper, Avatar, CircularProgress, Alert } from '@mui/material';
import { usersApi } from '../api/usersApi';
import { postsApi } from '../api/postsApi';
import PostCard from '../components/PostCard';
import type { User, Post } from '../types';

const UserProfilePage = () => {
  const { id } = useParams<{ id: string }>();
  const [user, setUser] = useState<User | null>(null);
  const [posts, setPosts] = useState<Post[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      loadUser();
      loadUserPosts();
    }
  }, [id]);

  const loadUser = async () => {
    setIsLoading(true);
    try {
      const response = await usersApi.getUserById(Number(id));
      if (response.success && response.data) {
        setUser(response.data);
      } else {
        setError('Пользователь не найден');
      }
    } catch (error: any) {
      setError(error.response?.data?.message || 'Ошибка загрузки пользователя');
    } finally {
      setIsLoading(false);
    }
  };

  const loadUserPosts = async () => {
    try {
      const response = await postsApi.getPostsByAuthor(Number(id));
      if (response.success && response.data) {
        setPosts(response.data.content);
      }
    } catch (error) {
      console.error('Failed to load user posts:', error);
    }
  };

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error || !user) {
    return (
      <Alert severity="error">{error || 'Пользователь не найден'}</Alert>
    );
  }

  return (
    <Box>
      <Paper sx={{ p: 4, mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 3 }}>
          <Avatar sx={{ width: 100, height: 100 }} src={user.avatarUrl}>
            {user.username[0].toUpperCase()}
          </Avatar>
          <Box>
            <Typography variant="h4">{user.username}</Typography>
            <Typography variant="body1" color="text.secondary">
              {user.email}
            </Typography>
            {user.bio && (
              <Typography variant="body2" sx={{ mt: 1 }}>
                {user.bio}
              </Typography>
            )}
          </Box>
        </Box>
      </Paper>
      <Typography variant="h6" gutterBottom>
        Посты пользователя ({posts.length})
      </Typography>
      {posts.length === 0 ? (
        <Typography variant="body2" color="text.secondary">
          У пользователя пока нет постов
        </Typography>
      ) : (
        posts.map((post) => <PostCard key={post.id} post={post} />)
      )}
    </Box>
  );
};

export default UserProfilePage;

