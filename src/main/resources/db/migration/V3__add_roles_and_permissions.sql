-- Создаем таблицу для аудита
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Создаем индекс для быстрого поиска по аудиту
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);

-- Добавляем поле для отслеживания последнего входа
ALTER TABLE users ADD COLUMN last_login_at TIMESTAMP;

-- Добавляем поле для верификации email
ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN verification_token VARCHAR(255);
ALTER TABLE users ADD COLUMN verification_token_expiry TIMESTAMP;

-- Добавляем поле для восстановления пароля
ALTER TABLE users ADD COLUMN reset_password_token VARCHAR(255);
ALTER TABLE users ADD COLUMN reset_password_token_expiry TIMESTAMP;