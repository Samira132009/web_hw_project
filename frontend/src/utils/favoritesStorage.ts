const FAVORITES_KEY = 'favorite_posts';

export const favoritesStorage = {
  getFavorites: (): number[] => {
    const favorites = localStorage.getItem(FAVORITES_KEY);
    return favorites ? JSON.parse(favorites) : [];
  },

  addFavorite: (postId: number): void => {
    const favorites = favoritesStorage.getFavorites();
    if (!favorites.includes(postId)) {
      favorites.push(postId);
      localStorage.setItem(FAVORITES_KEY, JSON.stringify(favorites));
    }
  },

  removeFavorite: (postId: number): void => {
    const favorites = favoritesStorage.getFavorites();
    const filtered = favorites.filter((id) => id !== postId);
    localStorage.setItem(FAVORITES_KEY, JSON.stringify(filtered));
  },

  isFavorite: (postId: number): boolean => {
    const favorites = favoritesStorage.getFavorites();
    return favorites.includes(postId);
  },

  clear: (): void => {
    localStorage.removeItem(FAVORITES_KEY);
  },
};

