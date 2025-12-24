-- Добавляем поле для полнотекстового поиска
ALTER TABLE posts ADD COLUMN search_vector tsvector;

-- Создаем функцию для обновления search_vector
CREATE OR REPLACE FUNCTION posts_search_vector_update() RETURNS trigger AS $$
BEGIN
    NEW.search_vector :=
        setweight(to_tsvector('english', coalesce(NEW.title, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(NEW.content, '')), 'B') ||
        setweight(to_tsvector('english', coalesce(NEW.excerpt, '')), 'C');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Создаем триггер
CREATE TRIGGER tsvector_update BEFORE INSERT OR UPDATE
ON posts FOR EACH ROW EXECUTE FUNCTION posts_search_vector_update();

-- Создаем индекс GIN для быстрого поиска
CREATE INDEX idx_posts_search_vector ON posts USING gin(search_vector);

-- Обновляем существующие записи
UPDATE posts SET search_vector =
    setweight(to_tsvector('english', coalesce(title, '')), 'A') ||
    setweight(to_tsvector('english', coalesce(content, '')), 'B') ||
    setweight(to_tsvector('english', coalesce(excerpt, '')), 'C');