import { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { Box, TextField, Button, Typography, Paper, Alert, CircularProgress } from '@mui/material';
import { RootState, AppDispatch } from '../store/store';
import { register as registerUser, clearError } from '../store/slices/authSlice'; // ← переименовали
import * as yup from 'yup';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';

const schema = yup.object({
  username: yup.string()
    .required('Введите имя пользователя')
    .min(3, 'Имя пользователя должно содержать минимум 3 символа'),
  email: yup.string()
    .required('Введите email')
    .email('Введите корректный email')
    .transform((value) => value ? value.trim() : ''), // ← добавляем trim
  password: yup.string()
    .required('Введите пароль')
    .min(6, 'Пароль должен содержать минимум 6 символов'),
  confirmPassword: yup.string()
    .required('Подтвердите пароль')
    .oneOf([yup.ref('password')], 'Пароли не совпадают'),
  firstName: yup.string().transform((value) => value ? value.trim() : ''),
  lastName: yup.string().transform((value) => value ? value.trim() : ''),
});

type RegisterFormData = yup.InferType<typeof schema>;

const RegisterPage = () => {
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();
  const { isLoading, error } = useSelector((state: RootState) => state.auth);
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,  // ← это react-hook-form register
    handleSubmit,
    formState: { errors },
    setValue,  // ← добавьте setValue если нужно
  } = useForm<RegisterFormData>({
    resolver: yupResolver(schema) as any,
    defaultValues: {
      username: '',
      email: '',
      firstName: '',
      lastName: '',
      password: '',
      confirmPassword: '',
    }
  });

  const onSubmit = async (data: RegisterFormData) => {
    setServerError(null);
    dispatch(clearError());
    
    try {
      const { confirmPassword, ...registerData } = data;
      
      // Очищаем пробелы в строковых полях
      const cleanData = {
        ...registerData,
        email: registerData.email?.trim() || '',
        username: registerData.username?.trim() || '',
        firstName: registerData.firstName?.trim() || '',
        lastName: registerData.lastName?.trim() || '',
      };
      
      const result = await dispatch(registerUser(cleanData));
      
      if (registerUser.fulfilled.match(result)) {
        navigate('/');
      } else {
        setServerError(result.payload as string || 'Ошибка регистрации');
      }
    } catch (error: any) {
      console.error('Registration error:', error);
      setServerError(error.message || 'Ошибка регистрации');
    }
  };

  return (
    <Box sx={{ maxWidth: 400, mx: 'auto', mt: 8 }}>
      <Paper sx={{ p: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom align="center">
          Регистрация
        </Typography>
        
        {(error || serverError) && (
          <Alert 
            severity="error" 
            sx={{ mb: 2 }} 
            onClose={() => { 
              dispatch(clearError()); 
              setServerError(null); 
            }}
          >
            {error || serverError}
          </Alert>
        )}
        
        <Box component="form" onSubmit={handleSubmit(onSubmit)}>
          <TextField
            fullWidth
            label="Имя пользователя"
            margin="normal"
            {...register('username')}
            error={!!errors.username}
            helperText={errors.username?.message}
            disabled={isLoading}
            inputProps={{ maxLength: 50 }}
          />
          
          <TextField
            fullWidth
            label="Email"
            type="email"
            margin="normal"
            {...register('email')}
            error={!!errors.email}
            helperText={errors.email?.message}
            disabled={isLoading}
            inputProps={{ maxLength: 100 }}
          />
          
          <TextField
            fullWidth
            label="Имя"
            margin="normal"
            {...register('firstName')}
            error={!!errors.firstName}
            helperText={errors.firstName?.message}
            disabled={isLoading}
            inputProps={{ maxLength: 50 }}
          />
          
          <TextField
            fullWidth
            label="Фамилия"
            margin="normal"
            {...register('lastName')}
            error={!!errors.lastName}
            helperText={errors.lastName?.message}
            disabled={isLoading}
            inputProps={{ maxLength: 50 }}
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
            inputProps={{ maxLength: 100 }}
          />
          
          <TextField
            fullWidth
            label="Подтвердите пароль"
            type="password"
            margin="normal"
            {...register('confirmPassword')}
            error={!!errors.confirmPassword}
            helperText={errors.confirmPassword?.message}
            disabled={isLoading}
            inputProps={{ maxLength: 100 }}
          />
          
          <Button
            type="submit"
            fullWidth
            variant="contained"
            sx={{ mt: 3, mb: 2 }}
            disabled={isLoading}
          >
            {isLoading ? <CircularProgress size={24} /> : 'Зарегистрироваться'}
          </Button>
        </Box>
      </Paper>
    </Box>
  );
};

export default RegisterPage;