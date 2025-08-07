-- Добавляем колонку profile_id в таблицу users только если она не существует
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'profile_id'
    ) THEN
        ALTER TABLE users ADD COLUMN profile_id UUID;
    END IF;
END $$;

-- Добавляем внешний ключ, связывающий users.profile_id с profiles.id.
-- ON DELETE SET NULL: если профиль удаляется, ссылка на него в таблице users становится NULL.
-- Вы можете изменить это на ON DELETE CASCADE, если хотите, чтобы пользователь удалялся вместе с профилем,
-- но это нетипичное поведение, чаще профиль удаляется при удалении пользователя, а не наоборот.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_users_profile_id'
    ) THEN
        ALTER TABLE users
            ADD CONSTRAINT fk_users_profile_id
                FOREIGN KEY (profile_id) REFERENCES profiles (id) ON DELETE SET NULL;
    END IF;
END $$;

-- Добавляем UNIQUE ограничение на profile_id.
-- Это гарантирует, что один профиль может быть связан только с одним пользователем.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'uq_users_profile_id'
    ) THEN
        ALTER TABLE users
            ADD CONSTRAINT uq_users_profile_id UNIQUE (profile_id);
    END IF;
END $$;