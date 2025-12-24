import { useEffect, useState } from 'react';
import { Box, Typography, CircularProgress, Alert } from '@mui/material';
import { postsApi } from '../api/postsApi';
import { favoritesStorage } from '../utils/favoritesStorage';
import PostCard from '../components/PostCard';
import type { Post } from '../types';

const FavoritesPage = () => {
  const [posts, setPosts] = useState<Post[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadFavorites();
  }, []);

  const loadFavorites = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const favoriteIds = favoritesStorage.getFavorites();
      if (favoriteIds.length === 0) {
        setPosts([]);
        setIsLoading(false);
        return;
      }

      const postsPromises = favoriteIds.map((id) => postsApi.getPostById(id));
      const results = await Promise.allSettled(postsPromises);
      const loadedPosts = results
        .filter((result) => result.status === 'fulfilled' && result.value.success && result.value.data)
        .map((result) => (result as PromiseFulfilledResult<any>).value.data);

      setPosts(loadedPosts);
    } catch (error: any) {
      setError(error.message || 'Ошибка загрузки избранных постов');
    } finally {
      setIsLoading(false);
    }
  };

  const handleRemoveFavorite = (postId: number) => {
    favoritesStorage.removeFavorite(postId);
    setPosts(posts.filter((p) => p.id !== postId));
  };

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Alert severity="error">{error}</Alert>
    );
  }

  return (
    <Box>
      <Typography variant="h4" component="h1" gutterBottom>
        Избранные посты
      </Typography>
      {posts.length === 0 ? (
        <Typography variant="body1" color="text.secondary">
          У вас пока нет избранных постов
        </Typography>
      ) : (
        posts.map((post) => (
          <PostCard
            key={post.id}
            post={post}
            onDelete={handleRemoveFavorite}
            showActions={false}
          />
        ))
      )}
    </Box>
  );
};

export default FavoritesPage;

