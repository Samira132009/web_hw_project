import { Box, TextField, InputAdornment, Button } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';

const SearchBar = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [query, setQuery] = useState(searchParams.get('q') || '');

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (query.trim()) {
      navigate(`/search?q=${encodeURIComponent(query.trim())}`);
    }
  };

  return (
    <Box component="form" onSubmit={handleSearch} sx={{ mb: 3 }}>
      <TextField
        fullWidth
        placeholder="Поиск постов и пользователей..."
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <SearchIcon />
            </InputAdornment>
          ),
          endAdornment: (
            <InputAdornment position="end">
              <Button type="submit" variant="contained">
                Найти
              </Button>
            </InputAdornment>
          ),
        }}
      />
    </Box>
  );
};

export default SearchBar;

