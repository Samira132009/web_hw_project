import { ReactNode } from 'react';
import { AppBar, Toolbar, Typography, Button, Box, Container } from '@mui/material';
import { Link, useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { RootState } from '../store/store';
import { logout } from '../store/slices/authSlice';
import { tokenStorage } from '../utils/tokenStorage';

interface LayoutProps {
  children: ReactNode;
}

const Layout = ({ children }: LayoutProps) => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const isAuthenticated = useSelector((state: RootState) => state.auth.isAuthenticated);
  const user = useSelector((state: RootState) => state.auth.user);

  const handleLogout = () => {
    dispatch(logout());
    tokenStorage.clear();
    navigate('/login');
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" component={Link} to="/" sx={{ flexGrow: 1, textDecoration: 'none', color: 'inherit' }}>
            Blog Platform
          </Typography>
          <Button color="inherit" component={Link} to="/">
            Главная
          </Button>
          <Button color="inherit" component={Link} to="/search">
            Поиск
          </Button>
          {isAuthenticated ? (
            <>
              <Button color="inherit" component={Link} to="/create-post">
                Создать пост
              </Button>
              <Button color="inherit" component={Link} to="/favorites">
                Избранное
              </Button>
              <Button color="inherit" component={Link} to="/profile">
                {user?.username || 'Профиль'}
              </Button>
              <Button color="inherit" onClick={handleLogout}>
                Выход
              </Button>
            </>
          ) : (
            <>
              <Button color="inherit" component={Link} to="/login">
                Вход
              </Button>
              <Button color="inherit" component={Link} to="/register">
                Регистрация
              </Button>
            </>
          )}
        </Toolbar>
      </AppBar>
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4, flex: 1 }}>
        {children}
      </Container>
    </Box>
  );
};

export default Layout;

