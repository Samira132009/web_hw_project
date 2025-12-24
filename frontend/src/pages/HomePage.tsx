import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Box, Typography, Pagination, CircularProgress, Alert } from '@mui/material';
import { RootState, AppDispatch } from '../store/store';
import { fetchPosts, likePost, deletePost } from '../store/slices/postsSlice';
import { postsApi } from '../api/postsApi';
import PostCard from '../components/PostCard';
import SearchBar from '../components/SearchBar';

const HomePage = () => {
  const dispatch = useDispatch<AppDispatch>();
  const { posts, pagination, isLoading, error } = useSelector((state: RootState) => state.posts);
  const currentUser = useSelector((state: RootState) => state.auth.user);
  const [page, setPage] = useState(1);

  useEffect(() => {
    dispatch(fetchPosts({ page: page - 1, size: 20 }));
  }, [dispatch, page]);

  const handleLike = async (postId: number) => {
    try {
      await postsApi.likePost(postId);
      dispatch(fetchPosts({ page: page - 1, size: 20 }));
    } catch (error) {
      console.error('Failed to like post:', error);
    }
  };

  const handleDelete = async (postId: number) => {
    if (window.confirm('Вы уверены, что хотите удалить этот пост?')) {
      try {
        await dispatch(deletePost(postId));
        dispatch(fetchPosts({ page: page - 1, size: 20 }));
      } catch (error) {
        console.error('Failed to delete post:', error);
      }
    }
  };

  if (isLoading && posts.length === 0) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h4" component="h1" gutterBottom>
        Лента постов
      </Typography>
      <SearchBar />
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      {posts.length === 0 ? (
        <Typography variant="body1" color="text.secondary">
          Пока нет постов. Создайте первый пост!
        </Typography>
      ) : (
        <>
          {posts.map((post) => (
            <PostCard
              key={post.id}
              post={post}
              onLike={handleLike}
              onDelete={currentUser?.id === post.authorId ? handleDelete : undefined}
              isAuthor={currentUser?.id === post.authorId}
            />
          ))}
          {pagination.totalPages > 1 && (
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
              <Pagination
                count={pagination.totalPages}
                page={page}
                onChange={(_, value) => setPage(value)}
                color="primary"
              />
            </Box>
          )}
        </>
      )}
    </Box>
  );
};

export default HomePage;

