-- Удаляем старое ограничение PRIMARY KEY
ALTER TABLE files DROP CONSTRAINT IF EXISTS files_pkey;

-- Меняем тип id на UUID с автоинкрементом
ALTER TABLE files
    ALTER COLUMN id TYPE UUID USING gen_random_uuid(),
    ALTER COLUMN id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN id SET NOT NULL;

-- Восстанавливаем PRIMARY KEY
ALTER TABLE files ADD PRIMARY KEY (id); 