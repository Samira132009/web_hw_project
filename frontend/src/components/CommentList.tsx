import { Box, Typography, Paper, Avatar, Button, TextField } from '@mui/material';
import { useState } from 'react';
import { format } from 'date-fns';
import { ru } from 'date-fns/locale';
import { useSelector } from 'react-redux';
import { RootState } from '../store/store';
import type { Comment } from '../types';

interface CommentListProps {
  comments: Comment[] | any;
  postId: number;
  onAddComment: (content: string, parentId?: number) => void;
  onDeleteComment?: (id: number) => void;
}

interface CommentItemProps {
  comment: Comment;
  postId: number;
  onAddComment: (content: string, parentId?: number) => void;
  onDeleteComment?: (id: number) => void;
  level?: number;
}

const CommentItem = ({ comment, postId, onAddComment, onDeleteComment, level = 0 }: CommentItemProps) => {
  const [showReply, setShowReply] = useState(false);
  const [replyContent, setReplyContent] = useState('');
  const currentUser = useSelector((state: RootState) => state.auth.user);
  const isAuthor = currentUser?.id === comment.authorId;

  const handleReply = () => {
    if (replyContent.trim()) {
      onAddComment(replyContent, comment.id);
      setReplyContent('');
      setShowReply(false);
    }
  };

  return (
    <Paper sx={{ p: 2, mb: 2, ml: level * 4, bgcolor: level > 0 ? 'grey.50' : 'white' }}>
      <Box sx={{ display: 'flex', gap: 2 }}>
        <Avatar src={comment.authorAvatar}>{comment.authorUsername[0].toUpperCase()}</Avatar>
        <Box sx={{ flex: 1 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
            <Typography variant="subtitle2">{comment.authorUsername}</Typography>
            <Typography variant="caption" color="text.secondary">
              {format(new Date(comment.createdAt), 'dd MMMM yyyy HH:mm', { locale: ru })}
            </Typography>
          </Box>
          <Typography variant="body2" sx={{ mb: 1 }}>
            {comment.content}
          </Typography>
          <Box sx={{ display: 'flex', gap: 1 }}>
            {currentUser && (
              <Button size="small" onClick={() => setShowReply(!showReply)}>
                Ответить
              </Button>
            )}
            {isAuthor && onDeleteComment && (
              <Button size="small" color="error" onClick={() => onDeleteComment(comment.id)}>
                Удалить
              </Button>
            )}
          </Box>
          {showReply && (
            <Box sx={{ mt: 2 }}>
              <TextField
                fullWidth
                multiline
                rows={3}
                placeholder="Напишите ответ..."
                value={replyContent}
                onChange={(e) => setReplyContent(e.target.value)}
                sx={{ mb: 1 }}
              />
              <Box sx={{ display: 'flex', gap: 1 }}>
                <Button size="small" variant="contained" onClick={handleReply}>
                  Отправить
                </Button>
                <Button size="small" onClick={() => setShowReply(false)}>
                  Отмена
                </Button>
              </Box>
            </Box>
          )}
          {comment.replies && comment.replies.length > 0 && (
            <Box sx={{ mt: 2 }}>
              {comment.replies.map((reply) => (
                <CommentItem
                  key={reply.id}
                  comment={reply}
                  postId={postId}
                  onAddComment={onAddComment}
                  onDeleteComment={onDeleteComment}
                  level={level + 1}
                />
              ))}
            </Box>
          )}
        </Box>
      </Box>
    </Paper>
  );
};

const CommentList = ({ comments, postId, onAddComment, onDeleteComment }: CommentListProps) => {
  const [newComment, setNewComment] = useState('');
  const currentUser = useSelector((state: RootState) => state.auth.user);

  const handleAddComment = () => {
    if (newComment.trim()) {
      onAddComment(newComment);
      setNewComment('');
    }
  };

  const safeComments = comments || [];

  return (
    <Box>
      <Typography variant="h6" sx={{ mb: 2 }}>
        Комментарии ({safeComments.length})
      </Typography>
      {currentUser ? (
        <Box sx={{ mb: 3 }}>
          <TextField
            fullWidth
            multiline
            rows={4}
            placeholder="Напишите комментарий..."
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
            sx={{ mb: 1 }}
          />
          <Button variant="contained" onClick={handleAddComment}>
            Отправить комментарий
          </Button>
        </Box>
      ) : (
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          Войдите, чтобы оставить комментарий
        </Typography>
      )}
      {safeComments.length === 0 ? (
        <Typography variant="body2" color="text.secondary">
          Пока нет комментариев. Будьте первым!
        </Typography>
      ) : (
        safeComments.map((comment: Comment | any) => (
          <CommentItem
            key={comment.id}
            comment={comment}
            postId={postId}
            onAddComment={onAddComment}
            onDeleteComment={onDeleteComment}
          />
        ))
      )}
    </Box>
  );
};

export default CommentList;