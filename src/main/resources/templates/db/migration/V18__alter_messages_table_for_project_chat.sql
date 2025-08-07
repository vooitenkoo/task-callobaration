-- Удаляем внешний ключ на tasks (если существует)
ALTER TABLE messages DROP CONSTRAINT IF EXISTS messages_task_id_fkey;

-- Удаляем старое ограничение PRIMARY KEY
ALTER TABLE messages DROP CONSTRAINT IF EXISTS messages_pkey;

-- Удаляем колонку task_id
ALTER TABLE messages DROP COLUMN IF EXISTS task_id;

-- Добавляем колонку project_id только если она не существует
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'messages' AND column_name = 'project_id'
    ) THEN
        ALTER TABLE messages ADD COLUMN project_id UUID;
    END IF;
END $$;

-- Меняем тип id на UUID с автоинкрементом
ALTER TABLE messages ALTER COLUMN id TYPE UUID USING gen_random_uuid();
ALTER TABLE messages ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE messages ALTER COLUMN id SET NOT NULL;

-- Восстанавливаем PRIMARY KEY
ALTER TABLE messages ADD PRIMARY KEY (id);

-- Добавляем внешний ключ на projects только если он не существует
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'messages_project_id_fkey'
    ) THEN
        ALTER TABLE messages ADD CONSTRAINT messages_project_id_fkey 
            FOREIGN KEY (project_id) REFERENCES projects(id);
    END IF;
END $$;

-- Делаем project_id NOT NULL
ALTER TABLE messages ALTER COLUMN project_id SET NOT NULL;

-- Добавляем колонку is_read только если она не существует
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'messages' AND column_name = 'is_read'
    ) THEN
        ALTER TABLE messages ADD COLUMN is_read BOOLEAN NOT NULL DEFAULT false;
    END IF;
END $$; 