import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Box, Typography, Tabs, Tab, CircularProgress, Alert } from '@mui/material';
import { postsApi } from '../api/postsApi';
import { usersApi } from '../api/usersApi';
import PostCard from '../components/PostCard';
import SearchBar from '../components/SearchBar';
import type { Post, User } from '../types';

const SearchPage = () => {
  const [searchParams] = useSearchParams();
  const query = searchParams.get('q') || '';
  const type = searchParams.get('type') || 'all';
  const [activeTab, setActiveTab] = useState(type === 'users' ? 1 : 0);
  const [posts, setPosts] = useState<Post[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (query) {
      performSearch();
    }
  }, [query, activeTab]);

  const performSearch = async () => {
    if (!query.trim()) return;
    setIsLoading(true);
    setError(null);
    try {
      if (activeTab === 0) {
        const response = await postsApi.searchPosts(query);
        if (response.success && response.data) {
          setPosts(response.data.content);
        }
      } else {
        const response = await usersApi.searchUsers(query);
        if (response.success && response.data) {
          setUsers(response.data.content);
        }
      }
    } catch (error: any) {
      setError(error.response?.data?.message || 'Ошибка поиска');
    } finally {
      setIsLoading(false);
    }
  };

  const handleTabChange = (_: React.SyntheticEvent, newValue: number) => {
    setActiveTab(newValue);
  };

  return (
    <Box>
      <Typography variant="h4" component="h1" gutterBottom>
        Поиск
      </Typography>
      <SearchBar />
      <Tabs value={activeTab} onChange={handleTabChange} sx={{ mb: 3 }}>
        <Tab label="Посты" />
        <Tab label="Пользователи" />
      </Tabs>
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      {isLoading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
          <CircularProgress />
        </Box>
      ) : activeTab === 0 ? (
        <>
          {posts.length === 0 ? (
            <Typography variant="body1" color="text.secondary">
              {query ? 'Посты не найдены' : 'Введите запрос для поиска'}
            </Typography>
          ) : (
            posts.map((post) => <PostCard key={post.id} post={post} />)
          )}
        </>
      ) : (
        <>
          {users.length === 0 ? (
            <Typography variant="body1" color="text.secondary">
              {query ? 'Пользователи не найдены' : 'Введите запрос для поиска'}
            </Typography>
          ) : (
            users.map((user) => (
              <Box key={user.id} sx={{ mb: 2, p: 2, border: '1px solid #e0e0e0', borderRadius: 1 }}>
                <Typography variant="h6">{user.username}</Typography>
                <Typography variant="body2" color="text.secondary">
                  {user.email}
                </Typography>
                {user.bio && (
                  <Typography variant="body2" sx={{ mt: 1 }}>
                    {user.bio}
                  </Typography>
                )}
              </Box>
            ))
          )}
        </>
      )}
    </Box>
  );
};

export default SearchPage;

