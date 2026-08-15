CREATE TABLE IF NOT EXISTS users (id BIGSERIAL PRIMARY KEY, name VARCHAR(255), email VARCHAR(255));
INSERT INTO users(name, email) VALUES ('Tenant One User', 'tenant1@example.com');
