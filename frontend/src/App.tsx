import { Routes, Route, Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { RootState } from './store/store';
import Layout from './components/Layout';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import PostPage from './pages/PostPage';
import CreatePostPage from './pages/CreatePostPage';
import EditPostPage from './pages/EditPostPage';
import ProfilePage from './pages/ProfilePage';
import SearchPage from './pages/SearchPage';
import UserProfilePage from './pages/UserProfilePage';
import FavoritesPage from './pages/FavoritesPage';

function App() {
  const isAuthenticated = useSelector((state: RootState) => state.auth.isAuthenticated);

  return (
    <Layout>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={!isAuthenticated ? <LoginPage /> : <Navigate to="/" />} />
        <Route path="/register" element={!isAuthenticated ? <RegisterPage /> : <Navigate to="/" />} />
        <Route path="/post/:id" element={<PostPage />} />
        <Route path="/create-post" element={isAuthenticated ? <CreatePostPage /> : <Navigate to="/login" />} />
        <Route path="/edit-post/:id" element={isAuthenticated ? <EditPostPage /> : <Navigate to="/login" />} />
        <Route path="/profile" element={isAuthenticated ? <ProfilePage /> : <Navigate to="/login" />} />
        <Route path="/favorites" element={isAuthenticated ? <FavoritesPage /> : <Navigate to="/login" />} />
        <Route path="/user/:id" element={<UserProfilePage />} />
        <Route path="/search" element={<SearchPage />} />
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </Layout>
  );
}

export default App;


