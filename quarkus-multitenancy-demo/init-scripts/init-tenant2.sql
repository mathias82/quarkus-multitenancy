CREATE SEQUENCE IF NOT EXISTS users_seq START 1;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT DEFAULT nextval('users_seq') PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255)
);

INSERT INTO users(name, email)
SELECT 'Tenant Two User', 'tenant2@example.com'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'tenant2@example.com');
