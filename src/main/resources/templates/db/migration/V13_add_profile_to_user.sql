-- Добавляем колонку profile_id в таблицу users.
-- Изначально она может быть NULL, если у вас уже есть пользователи без связанных профилей.
ALTER TABLE users
    ADD COLUMN profile_id UUID;

-- Добавляем внешний ключ, связывающий users.profile_id с profiles.id.
-- ON DELETE SET NULL: если профиль удаляется, ссылка на него в таблице users становится NULL.
-- Вы можете изменить это на ON DELETE CASCADE, если хотите, чтобы пользователь удалялся вместе с профилем,
-- но это нетипичное поведение, чаще профиль удаляется при удалении пользователя, а не наоборот.
ALTER TABLE users
    ADD CONSTRAINT fk_users_profile_id
        FOREIGN KEY (profile_id) REFERENCES profiles (id) ON DELETE SET NULL;

-- Добавляем UNIQUE ограничение на profile_id.
-- Это гарантирует, что один профиль может быть связан только с одним пользователем.
ALTER TABLE users
    ADD CONSTRAINT uq_users_profile_id UNIQUE (profile_id);