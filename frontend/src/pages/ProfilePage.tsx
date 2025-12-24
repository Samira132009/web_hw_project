import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { Box, TextField, Button, Typography, Paper, Alert, CircularProgress, Avatar, Grid } from '@mui/material';
import { RootState, AppDispatch } from '../store/store';
import { usersApi } from '../api/usersApi';
import { postsApi } from '../api/postsApi';
import { setUser } from '../store/slices/authSlice';
import PostCard from '../components/PostCard';
import * as yup from 'yup';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';

const schema = yup.object({
  firstName: yup.string(),
  lastName: yup.string(),
  bio: yup.string().max(500, 'Биография не должна превышать 500 символов'),
});

type ProfileFormData = yup.InferType<typeof schema>;

const ProfilePage = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch<AppDispatch>();
  const currentUser = useSelector((state: RootState) => state.auth.user);
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingPosts, setIsLoadingPosts] = useState(false);
  const [posts, setPosts] = useState<any[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ProfileFormData>({
    resolver: yupResolver(schema),
  });

  useEffect(() => {
    if (currentUser) {
      reset({
        firstName: currentUser.firstName || '',
        lastName: currentUser.lastName || '',
        bio: currentUser.bio || '',
      });
      loadMyPosts();
    }
  }, [currentUser, reset]);

  const loadMyPosts = async () => {
    setIsLoadingPosts(true);
    try {
      const response = await postsApi.getMyPosts(0, 10);
      if (response.success && response.data) {
        setPosts(response.data.content);
      }
    } catch (error) {
      console.error('Failed to load posts:', error);
    } finally {
      setIsLoadingPosts(false);
    }
  };

  const onSubmit = async (data: ProfileFormData) => {
    setIsLoading(true);
    setError(null);
    setSuccess(null);
    try {
      const response = await usersApi.updateProfile(data);
      if (response.success && response.data) {
        dispatch(setUser(response.data));
        setSuccess('Профиль успешно обновлен');
      } else {
        setError(response.error || 'Ошибка при обновлении профиля');
      }
    } catch (error: any) {
      setError(error.response?.data?.message || 'Ошибка при обновлении профиля');
    } finally {
      setIsLoading(false);
    }
  };

  if (!currentUser) {
    return (
      <Alert severity="error">Пользователь не найден</Alert>
    );
  }

  return (
    <Grid container spacing={3}>
      <Grid item xs={12} md={4}>
        <Paper sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 3 }}>
            <Avatar sx={{ width: 100, height: 100, mb: 2 }} src={currentUser.avatarUrl}>
              {currentUser.username[0].toUpperCase()}
            </Avatar>
            <Typography variant="h5">{currentUser.username}</Typography>
            <Typography variant="body2" color="text.secondary">
              {currentUser.email}
            </Typography>
          </Box>
          <Typography variant="h6" gutterBottom>
            Редактировать профиль
          </Typography>
          {error && (
            <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
              {error}
            </Alert>
          )}
          {success && (
            <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess(null)}>
              {success}
            </Alert>
          )}
          <Box component="form" onSubmit={handleSubmit(onSubmit)}>
            <TextField
              fullWidth
              label="Имя"
              margin="normal"
              {...register('firstName')}
              error={!!errors.firstName}
              helperText={errors.firstName?.message}
              disabled={isLoading}
            />
            <TextField
              fullWidth
              label="Фамилия"
              margin="normal"
              {...register('lastName')}
              error={!!errors.lastName}
              helperText={errors.lastName?.message}
              disabled={isLoading}
            />
            <TextField
              fullWidth
              label="Биография"
              multiline
              rows={4}
              margin="normal"
              {...register('bio')}
              error={!!errors.bio}
              helperText={errors.bio?.message}
              disabled={isLoading}
            />
            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 2 }}
              disabled={isLoading}
            >
              {isLoading ? <CircularProgress size={24} /> : 'Сохранить изменения'}
            </Button>
          </Box>
        </Paper>
      </Grid>
      <Grid item xs={12} md={8}>
        <Typography variant="h6" gutterBottom>
          Мои посты
        </Typography>
        {isLoadingPosts ? (
          <CircularProgress />
        ) : posts.length === 0 ? (
          <Typography variant="body2" color="text.secondary">
            У вас пока нет постов
          </Typography>
        ) : (
          posts.map((post) => (
            <PostCard
              key={post.id}
              post={post}
              isAuthor={true}
              onDelete={async (id) => {
                if (window.confirm('Удалить пост?')) {
                  try {
                    await postsApi.deletePost(id);
                    loadMyPosts();
                  } catch (error) {
                    console.error('Failed to delete post:', error);
                  }
                }
              }}
            />
          ))
        )}
      </Grid>
    </Grid>
  );
};

export default ProfilePage;

