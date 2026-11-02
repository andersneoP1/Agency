-- V2__seed.sql: Seed initial roles and users

-- 1. Insert default system roles
INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');

-- 2. Insert initial users (Password: 'Admin123!' for admin, 'Password123!' for users)
INSERT INTO users (email, password_hash, first_name, last_name, phone, referral_code, enabled, created_at, updated_at) VALUES 
('admin@nativetravel.local', '$2a$10$zB7QcpgYxo.GM9SDEpqFduuxAKzf1QW2RpOeS9ZgflBzgWrX.vMw.', 'Администратор', 'Системы', '+79990000001', 'NT-ADM0001', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (email, password_hash, first_name, last_name, phone, referral_code, enabled, created_at, updated_at) VALUES 
('ivan@nativetravel.local', '$2a$10$qr0fMXHAc8g1c0hjJ3QpFOVbbxeg5jo70Bq1N7EdbjMbMazHbQpky', 'Иван', 'Петров', '+79991112233', 'NT-IVAN123', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (email, password_hash, first_name, last_name, phone, referral_code, enabled, created_at, updated_at) VALUES 
('anna@nativetravel.local', '$2a$10$qr0fMXHAc8g1c0hjJ3QpFOVbbxeg5jo70Bq1N7EdbjMbMazHbQpky', 'Анна', 'Соколова', '+79992223344', 'NT-ANNA456', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (email, password_hash, first_name, last_name, phone, referral_code, enabled, created_at, updated_at) VALUES 
('sergey@nativetravel.local', '$2a$10$qr0fMXHAc8g1c0hjJ3QpFOVbbxeg5jo70Bq1N7EdbjMbMazHbQpky', 'Сергей', 'Морозов', '+79993334455', 'NT-SERG789', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 3. Map user roles dynamically
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'admin@nativetravel.local' AND r.name IN ('ROLE_USER', 'ROLE_ADMIN');

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email != 'admin@nativetravel.local' AND r.name = 'ROLE_USER';
