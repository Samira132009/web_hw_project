import { describe, it, expect, beforeEach } from '@jest/globals';
import { tokenStorage } from '../utils/tokenStorage';

describe('tokenStorage', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('should save and retrieve token', () => {
    const token = 'test-token';
    tokenStorage.setToken(token);
    expect(tokenStorage.getToken()).toBe(token);
  });

  it('should return null when token does not exist', () => {
    expect(tokenStorage.getToken()).toBeNull();
  });

  it('should remove token', () => {
    tokenStorage.setToken('test-token');
    tokenStorage.removeToken();
    expect(tokenStorage.getToken()).toBeNull();
  });

  it('should check if token exists', () => {
    expect(tokenStorage.hasToken()).toBe(false);
    tokenStorage.setToken('test-token');
    expect(tokenStorage.hasToken()).toBe(true);
  });

  it('should save and retrieve user', () => {
    const user = { id: 1, username: 'testuser' };
    tokenStorage.setUser(user);
    expect(tokenStorage.getUser()).toEqual(user);
  });

  it('should clear all data', () => {
    tokenStorage.setToken('test-token');
    tokenStorage.setUser({ id: 1, username: 'testuser' });
    tokenStorage.clear();
    expect(tokenStorage.getToken()).toBeNull();
    expect(tokenStorage.getUser()).toBeNull();
  });
});

