import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { Box, TextField, Button, Typography, Paper, Alert, CircularProgress, Chip, Autocomplete } from '@mui/material';
import { RootState, AppDispatch } from '../store/store';
import { createPost } from '../store/slices/postsSlice';
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

const CreatePostPage = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch<AppDispatch>();
  const { isLoading, error } = useSelector((state: RootState) => state.posts);
  const [serverError, setServerError] = useState<string | null>(null);
  const [tags, setTags] = useState<string[]>([]);

  const {
    register,
    handleSubmit,
    control,
    formState: { errors },
  } = useForm<PostFormData>({
    resolver: yupResolver(schema),
    defaultValues: {
      status: 'PUBLISHED',
      featured: false,
    },
  });

  const onSubmit = async (data: PostFormData) => {
    setServerError(null);
    try {
      const result = await dispatch(createPost({ ...data, tags }));
      if (createPost.fulfilled.match(result)) {
        navigate(`/post/${result.payload.id}`);
      } else {
        setServerError('Ошибка при создании поста');
      }
    } catch (error: any) {
      setServerError(error.message || 'Ошибка при создании поста');
    }
  };

  return (
    <Box sx={{ maxWidth: 800, mx: 'auto' }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Создать пост
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
              {isLoading ? <CircularProgress size={24} /> : 'Создать пост'}
            </Button>
            <Button variant="outlined" onClick={() => navigate('/')}>
              Отмена
            </Button>
          </Box>
        </Box>
      </Paper>
    </Box>
  );
};

export default CreatePostPage;

