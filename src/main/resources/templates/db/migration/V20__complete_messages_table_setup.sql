-- Меняем тип id на UUID с автоинкрементом
ALTER TABLE messages ALTER COLUMN id TYPE UUID USING gen_random_uuid();
ALTER TABLE messages ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE messages ALTER COLUMN id SET NOT NULL;

-- Удаляем существующий первичный ключ, если он есть
ALTER TABLE messages DROP CONSTRAINT IF EXISTS messages_pkey;

-- Восстанавливаем PRIMARY KEY
ALTER TABLE messages ADD PRIMARY KEY (id);

-- Удаляем существующее ограничение внешнего ключа, если оно есть
ALTER TABLE messages DROP CONSTRAINT IF EXISTS messages_project_id_fkey;

-- Добавляем внешний ключ на projects
ALTER TABLE messages ADD CONSTRAINT messages_project_id_fkey
    FOREIGN KEY (project_id) REFERENCES projects(id);

-- Делаем project_id NOT NULL
ALTER TABLE messages ALTER COLUMN project_id SET NOT NULL;

-- Добавляем колонку is_read (если еще не существует)
ALTER TABLE messages ADD COLUMN IF NOT EXISTS is_read BOOLEAN NOT NULL DEFAULT false;