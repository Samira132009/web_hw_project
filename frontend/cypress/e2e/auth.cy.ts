describe('Authentication', () => {
  beforeEach(() => {
    cy.visit('/');
  });

  it('should navigate to login page', () => {
    cy.contains('Вход').click();
    cy.url().should('include', '/login');
    cy.contains('Вход').should('be.visible');
  });

  it('should navigate to register page', () => {
    cy.contains('Регистрация').click();
    cy.url().should('include', '/register');
    cy.contains('Регистрация').should('be.visible');
  });

  it('should show validation errors on empty login form', () => {
    cy.visit('/login');
    cy.get('form').submit();
    cy.contains('Введите имя пользователя или email').should('be.visible');
    cy.contains('Введите пароль').should('be.visible');
  });

  it('should show validation errors on empty register form', () => {
    cy.visit('/register');
    cy.get('form').submit();
    cy.contains('Введите имя пользователя').should('be.visible');
    cy.contains('Введите email').should('be.visible');
    cy.contains('Введите пароль').should('be.visible');
  });
});

