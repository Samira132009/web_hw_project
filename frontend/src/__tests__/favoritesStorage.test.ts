import { describe, it, expect, beforeEach } from '@jest/globals';
import { favoritesStorage } from '../utils/favoritesStorage';

describe('favoritesStorage', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('should return empty array when no favorites', () => {
    expect(favoritesStorage.getFavorites()).toEqual([]);
  });

  it('should add favorite post', () => {
    favoritesStorage.addFavorite(1);
    expect(favoritesStorage.getFavorites()).toContain(1);
  });

  it('should not add duplicate favorites', () => {
    favoritesStorage.addFavorite(1);
    favoritesStorage.addFavorite(1);
    expect(favoritesStorage.getFavorites()).toEqual([1]);
  });

  it('should remove favorite post', () => {
    favoritesStorage.addFavorite(1);
    favoritesStorage.addFavorite(2);
    favoritesStorage.removeFavorite(1);
    expect(favoritesStorage.getFavorites()).not.toContain(1);
    expect(favoritesStorage.getFavorites()).toContain(2);
  });

  it('should check if post is favorite', () => {
    expect(favoritesStorage.isFavorite(1)).toBe(false);
    favoritesStorage.addFavorite(1);
    expect(favoritesStorage.isFavorite(1)).toBe(true);
  });

  it('should clear all favorites', () => {
    favoritesStorage.addFavorite(1);
    favoritesStorage.addFavorite(2);
    favoritesStorage.clear();
    expect(favoritesStorage.getFavorites()).toEqual([]);
  });
});

