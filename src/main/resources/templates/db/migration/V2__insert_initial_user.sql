INSERT INTO users (id, email, password, name, role, is_blocked, created_at)
VALUES (
           '550e8400-e29b-41d4-a716-446655440000', -- Уникальный UUID
           'admin@example.com',
           '$2a$10$randomhashedpassword',
           'Admin',
           'ADMIN',
           false,
           '2025-05-26 22:41:00'
       );