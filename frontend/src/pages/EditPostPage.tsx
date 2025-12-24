import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { Box, TextField, Button, Typography, Paper, Alert, CircularProgress, Chip, Autocomplete } from '@mui/material';
import { RootState, AppDispatch } from '../store/store';
import { fetchPostById, updatePost } from '../store/slices/postsSlice';
import * as yup from 'yup';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';

const schema = yup.object({
  title: yup.string().required('Введите заголовок').min(3, 'Заголовок должен содержать минимум 3 символа'),
  content: yup.string().required('Введите содержание поста').min(10, 'Содержание должно содержать минимум 10 символов'),
  status: yup.string().oneOf(['DRAFT', 'PUBLISHED'], 'Неверный статус'),
  tags: yup.array().of(yup.string()),
  featured: yup.boolean(),
});

type PostFormData = yup.InferType<typeof schema>;

const EditPostPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const dispatch = useDispatch<AppDispatch>();
  const { currentPost, isLoading, error } = useSelector((state: RootState) => state.posts);
  const [serverError, setServerError] = useState<string | null>(null);
  const [tags, setTags] = useState<string[]>([]);

  useEffect(() => {
    if (id) {
      dispatch(fetchPostById(Number(id)));
    }
  }, [dispatch, id]);

  useEffect(() => {
    if (currentPost) {
      setTags(currentPost.tags || []);
    }
  }, [currentPost]);

  const {
    register,
    handleSubmit,
    control,
    reset,
    formState: { errors },
  } = useForm<PostFormData>({
    resolver: yupResolver(schema),
  });

  useEffect(() => {
    if (currentPost) {
      reset({
        title: currentPost.title,
        content: currentPost.content,
        status: currentPost.status,
        featured: currentPost.featured,
      });
    }
  }, [currentPost, reset]);

  const onSubmit = async (data: PostFormData) => {
    if (!id) return;
    setServerError(null);
    try {
      const result = await dispatch(updatePost({ id: Number(id), data: { ...data, tags } }));
      if (updatePost.fulfilled.match(result)) {
        navigate(`/post/${id}`);
      } else {
        setServerError('Ошибка при обновлении поста');
      }
    } catch (error: any) {
      setServerError(error.message || 'Ошибка при обновлении поста');
    }
  };

  if (isLoading && !currentPost) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!currentPost) {
    return (
      <Alert severity="error">Пост не найден</Alert>
    );
  }

  return (
    <Box sx={{ maxWidth: 800, mx: 'auto' }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Редактировать пост
      </Typography>
      {(error || serverError) && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setServerError(null)}>
          {error || serverError}
        </Alert>
      )}
      <Paper sx={{ p: 4 }}>
        <Box component="form" onSubmit={handleSubmit(onSubmit)}>
          <TextField
            fullWidth
            label="Заголовок"
            margin="normal"
            {...register('title')}
            error={!!errors.title}
            helperText={errors.title?.message}
            disabled={isLoading}
          />
          <TextField
            fullWidth
            label="Содержание"
            multiline
            rows={15}
            margin="normal"
            {...register('content')}
            error={!!errors.content}
            helperText={errors.content?.message}
            disabled={isLoading}
          />
          <Controller
            name="status"
            control={control}
            render={({ field }) => (
              <TextField
                fullWidth
                select
                label="Статус"
                margin="normal"
                {...field}
                SelectProps={{ native: true }}
                disabled={isLoading}
              >
                <option value="PUBLISHED">Опубликован</option>
                <option value="DRAFT">Черновик</option>
              </TextField>
            )}
          />
          <Autocomplete
            multiple
            freeSolo
            options={[]}
            value={tags}
            onChange={(_, newValue) => setTags(newValue)}
            renderTags={(value, getTagProps) =>
              value.map((option, index) => (
                <Chip variant="outlined" label={option} {...getTagProps({ index })} key={index} />
              ))
            }
            renderInput={(params) => (
              <TextField
                {...params}
                label="Теги"
                margin="normal"
                placeholder="Добавьте теги"
              />
            )}
            sx={{ mt: 2 }}
          />
          <Box sx={{ display: 'flex', gap: 2, mt: 3 }}>
            <Button
              type="submit"
              variant="contained"
              disabled={isLoading}
            >
              {isLoading ? <CircularProgress size={24} /> : 'Сохранить изменения'}
            </Button>
            <Button variant="outlined" onClick={() => navigate(`/post/${id}`)}>
              Отмена
            </Button>
          </Box>
        </Box>
      </Paper>
    </Box>
  );
};

export default EditPostPage;

