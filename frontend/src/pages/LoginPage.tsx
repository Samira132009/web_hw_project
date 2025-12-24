import { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { Box, TextField, Button, Typography, Paper, Alert, CircularProgress } from '@mui/material';
import { RootState, AppDispatch } from '../store/store';
import { login, clearError } from '../store/slices/authSlice';
import * as yup from 'yup';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';

const schema = yup.object({
  usernameOrEmail: yup.string().required('Введите имя пользователя или email'),
  password: yup.string().required('Введите пароль').min(6, 'Пароль должен содержать минимум 6 символов'),
});

type LoginFormData = yup.InferType<typeof schema>;

const LoginPage = () => {
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();
  const { isLoading, error } = useSelector((state: RootState) => state.auth);
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: yupResolver(schema),
  });

  const onSubmit = async (data: LoginFormData) => {
    setServerError(null);
    dispatch(clearError());
    try {
      const result = await dispatch(login(data));
      if (login.fulfilled.match(result)) {
        navigate('/');
      } else {
        setServerError(result.payload as string || 'Ошибка входа');
      }
    } catch (error: any) {
      setServerError(error.message || 'Ошибка входа');
    }
  };

  return (
    <Box sx={{ maxWidth: 400, mx: 'auto', mt: 8 }}>
      <Paper sx={{ p: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom align="center">
          Вход
        </Typography>
        {(error || serverError) && (
          <Alert severity="error" sx={{ mb: 2 }} onClose={() => { dispatch(clearError()); setServerError(null); }}>
            {error || serverError}
          </Alert>
        )}
        <Box component="form" onSubmit={handleSubmit(onSubmit)}>
          <TextField
            fullWidth
            label="Имя пользователя или Email"
            margin="normal"
            {...register('usernameOrEmail')}
            error={!!errors.usernameOrEmail}
            helperText={errors.usernameOrEmail?.message}
            disabled={isLoading}
          />
          <TextField
            fullWidth
            label="Пароль"
            type="password"
            margin="normal"
            {...register('password')}
            error={!!errors.password}
            helperText={errors.password?.message}
            disabled={isLoading}
          />
          <Button
            type="submit"
            fullWidth
            variant="contained"
            sx={{ mt: 3, mb: 2 }}
            disabled={isLoading}
          >
            {isLoading ? <CircularProgress size={24} /> : 'Войти'}
          </Button>
        </Box>
      </Paper>
    </Box>
  );
};

export default LoginPage;

