import { Card, CardContent, CardActions, Typography, Button, Box, Chip, Avatar, IconButton } from '@mui/material';
import { Link } from 'react-router-dom';
import { format } from 'date-fns';
import { ru } from 'date-fns/locale';
import FavoriteIcon from '@mui/icons-material/Favorite';
import FavoriteBorderIcon from '@mui/icons-material/FavoriteBorder';
import CommentIcon from '@mui/icons-material/Comment';
import VisibilityIcon from '@mui/icons-material/Visibility';
import BookmarkIcon from '@mui/icons-material/Bookmark';
import BookmarkBorderIcon from '@mui/icons-material/BookmarkBorder';
import { favoritesStorage } from '../utils/favoritesStorage';
import { useState, useEffect } from 'react';
import type { Post } from '../types';

interface PostCardProps {
  post: Post;
  onLike?: (postId: number) => void;
  onDelete?: (postId: number) => void;
  showActions?: boolean;
  isAuthor?: boolean;
}

const PostCard = ({ post, onLike, onDelete, showActions = true, isAuthor = false }: PostCardProps) => {
  const [isFavorite, setIsFavorite] = useState(favoritesStorage.isFavorite(post.id));

  useEffect(() => {
    setIsFavorite(favoritesStorage.isFavorite(post.id));
  }, [post.id]);

  const handleToggleFavorite = () => {
    if (isFavorite) {
      favoritesStorage.removeFavorite(post.id);
    } else {
      favoritesStorage.addFavorite(post.id);
    }
    setIsFavorite(!isFavorite);
  };

  return (
    <Card sx={{ mb: 3 }}>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
          <Avatar sx={{ mr: 1 }} src={post.authorAvatar}>
            {post.authorUsername[0].toUpperCase()}
          </Avatar>
          <Box>
            <Typography variant="subtitle2" component={Link} to={`/user/${post.authorId}`} sx={{ textDecoration: 'none', color: 'inherit' }}>
              {post.authorUsername}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {post.publishedAt ? format(new Date(post.publishedAt), 'dd MMMM yyyy', { locale: ru }) : 'Черновик'}
            </Typography>
          </Box>
        </Box>
        <Typography variant="h5" component={Link} to={`/post/${post.id}`} sx={{ textDecoration: 'none', color: 'inherit', display: 'block', mb: 1 }}>
          {post.title}
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          {post.excerpt || post.content.substring(0, 200) + '...'}
        </Typography>
        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mb: 2 }}>
          {post.tags.map((tag) => (
            <Chip key={tag} label={tag} size="small" component={Link} to={`/search?q=${tag}&type=tag`} />
          ))}
        </Box>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', justifyContent: 'space-between' }}>
          <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              <VisibilityIcon fontSize="small" color="action" />
              <Typography variant="caption">{post.viewCount}</Typography>
            </Box>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              <FavoriteIcon fontSize="small" color="action" />
              <Typography variant="caption">{post.likeCount}</Typography>
            </Box>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              <CommentIcon fontSize="small" color="action" />
              <Typography variant="caption">{post.commentCount}</Typography>
            </Box>
          </Box>
          <IconButton size="small" onClick={handleToggleFavorite} color={isFavorite ? 'primary' : 'default'}>
            {isFavorite ? <BookmarkIcon /> : <BookmarkBorderIcon />}
          </IconButton>
        </Box>
      </CardContent>
      {showActions && (
        <CardActions>
          <Button size="small" component={Link} to={`/post/${post.id}`}>
            Читать далее
          </Button>
          {onLike && (
            <Button size="small" startIcon={<FavoriteIcon />} onClick={() => onLike(post.id)}>
              Лайк
            </Button>
          )}
          {isAuthor && (
            <>
              <Button size="small" component={Link} to={`/edit-post/${post.id}`}>
                Редактировать
              </Button>
              {onDelete && (
                <Button size="small" color="error" onClick={() => onDelete(post.id)}>
                  Удалить
                </Button>
              )}
            </>
          )}
        </CardActions>
      )}
    </Card>
  );
};

export default PostCard;

