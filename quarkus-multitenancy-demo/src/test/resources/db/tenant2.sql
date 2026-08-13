CREATE TABLE IF NOT EXISTS users (id BIGSERIAL PRIMARY KEY, name VARCHAR(255), email VARCHAR(255));
INSERT INTO users(name, email) VALUES ('Tenant Two User', 'tenant2@example.com');
