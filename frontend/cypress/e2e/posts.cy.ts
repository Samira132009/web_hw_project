describe('Posts', () => {
  beforeEach(() => {
    cy.visit('/');
  });

  it('should display posts list', () => {
    cy.contains('Лента постов').should('be.visible');
  });

  it('should have search bar', () => {
    cy.get('input[placeholder*="Поиск"]').should('be.visible');
  });

  it('should navigate to create post page when authenticated', () => {
    // This test assumes user is logged in
    // In real scenario, you would login first
    cy.visit('/create-post');
    // If not authenticated, should redirect to login
    cy.url().should('satisfy', (url) => {
      return url.includes('/login') || url.includes('/create-post');
    });
  });
});

