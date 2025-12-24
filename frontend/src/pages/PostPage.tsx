import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { Box, Typography, Paper, Avatar, Button, Chip, CircularProgress, Alert } from '@mui/material';
import { format } from 'date-fns';
import { ru } from 'date-fns/locale';
import FavoriteIcon from '@mui/icons-material/Favorite';
import { RootState, AppDispatch } from '../store/store';
import { fetchPostById, deletePost, likePost } from '../store/slices/postsSlice';
import { fetchPostComments, createComment, deleteComment } from '../store/slices/commentsSlice';
import { postsApi } from '../api/postsApi';
import CommentList from '../components/CommentList';

const PostPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const dispatch = useDispatch<AppDispatch>();
  const { currentPost, isLoading, error } = useSelector((state: RootState) => state.posts);
  const { comments, isLoading: commentsLoading } = useSelector((state: RootState) => state.comments);
  const currentUser = useSelector((state: RootState) => state.auth.user);
  const [isLiking, setIsLiking] = useState(false);

  useEffect(() => {
    if (id) {
      dispatch(fetchPostById(Number(id)));
      dispatch(fetchPostComments(Number(id)));
    }
  }, [dispatch, id]);

  const handleLike = async () => {
    if (!id) return;
    setIsLiking(true);
    try {
      await postsApi.likePost(Number(id));
      dispatch(fetchPostById(Number(id)));
    } catch (error) {
      console.error('Failed to like post:', error);
    } finally {
      setIsLiking(false);
    }
  };

  const handleDelete = async () => {
    if (!id || !window.confirm('Вы уверены, что хотите удалить этот пост?')) return;
    try {
      await dispatch(deletePost(Number(id)));
      navigate('/');
    } catch (error) {
      console.error('Failed to delete post:', error);
    }
  };

  const handleAddComment = async (content: string, parentId?: number) => {
    if (!id) return;
    try {
      await dispatch(createComment({ postId: Number(id), data: { content, parentId } }));
      dispatch(fetchPostComments(Number(id)));
    } catch (error) {
      console.error('Failed to add comment:', error);
    }
  };

  const handleDeleteComment = async (commentId: number) => {
    if (!id) return;
    try {
      await dispatch(deleteComment(commentId));
      dispatch(fetchPostComments(Number(id)));
    } catch (error) {
      console.error('Failed to delete comment:', error);
    }
  };

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error || !currentPost) {
    return (
      <Alert severity="error">
        {error || 'Пост не найден'}
      </Alert>
    );
  }

  const isAuthor = currentUser?.id === currentPost.authorId;

  return (
    <Box>
      <Paper sx={{ p: 4, mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
          <Avatar src={currentPost.authorAvatar}>{currentPost.authorUsername[0].toUpperCase()}</Avatar>
          <Box sx={{ ml: 2 }}>
            <Typography variant="subtitle1">{currentPost.authorUsername}</Typography>
            <Typography variant="caption" color="text.secondary">
              {currentPost.publishedAt ? format(new Date(currentPost.publishedAt), 'dd MMMM yyyy', { locale: ru }) : 'Черновик'}
            </Typography>
          </Box>
        </Box>
        <Typography variant="h3" component="h1" gutterBottom>
          {currentPost.title}
        </Typography>
        <Box sx={{ display: 'flex', gap: 1, mb: 3, flexWrap: 'wrap' }}>
          {currentPost.tags.map((tag) => (
            <Chip key={tag} label={tag} size="small" />
          ))}
        </Box>
        <Typography variant="body1" sx={{ whiteSpace: 'pre-wrap', mb: 3 }}>
          {currentPost.content}
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', mb: 2 }}>
          <Button
            variant="outlined"
            startIcon={<FavoriteIcon />}
            onClick={handleLike}
            disabled={isLiking || !currentUser}
          >
            {currentPost.likeCount}
          </Button>
          {isAuthor && (
            <>
              <Button variant="outlined" onClick={() => navigate(`/edit-post/${currentPost.id}`)}>
                Редактировать
              </Button>
              <Button variant="outlined" color="error" onClick={handleDelete}>
                Удалить
              </Button>
            </>
          )}
        </Box>
      </Paper>
      <CommentList
        comments={comments}
        postId={currentPost.id}
        onAddComment={handleAddComment}
        onDeleteComment={currentUser ? handleDeleteComment : undefined}
      />
    </Box>
  );
};

export default PostPage;

