-- Шаг 1: Сохраняем резервную копию таблицы tasks (опционально, для безопасности данных)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'tasks_backup') THEN
        CREATE TABLE tasks_backup AS SELECT * FROM tasks;
    END IF;
END $$;

-- Шаг 2: Удаляем все внешние ключи, ссылающиеся на tasks
ALTER TABLE activities DROP CONSTRAINT IF EXISTS fkl2c4tfjpf89kfw8pd3b4msbv3;
ALTER TABLE files DROP CONSTRAINT IF EXISTS fkbf6pb5mdsgg5if1g0d2yikpjb;

-- Проверяем существование колонки task_id в таблице messages перед удалением внешнего ключа
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'messages' AND column_name = 'task_id'
    ) THEN
        ALTER TABLE messages DROP CONSTRAINT IF EXISTS fk1fe2brsvkicrj653duknqxf7h;
    END IF;
END $$;

-- Проверяем существование колонки task_id в таблице notifications перед удалением внешнего ключа
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'notifications' AND column_name = 'task_id'
    ) THEN
        ALTER TABLE notifications DROP CONSTRAINT IF EXISTS fk2ktjq1slw0ldkuy5rx8fbte2p;
    END IF;
END $$;

-- Шаг 3: Удаляем старое ограничение PRIMARY KEY с каскадным удалением зависимостей
ALTER TABLE tasks DROP CONSTRAINT IF EXISTS tasks_pkey CASCADE;

-- Шаг 4: Меняем тип id на UUID с автоинкрементом
ALTER TABLE tasks
    ALTER COLUMN id TYPE UUID USING gen_random_uuid(),
    ALTER COLUMN id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN id SET NOT NULL;

-- Шаг 5: Восстанавливаем PRIMARY KEY
ALTER TABLE tasks ADD PRIMARY KEY (id);

-- Шаг 6: Восстанавливаем внешние ключи только если соответствующие колонки существуют
ALTER TABLE activities ADD CONSTRAINT fkl2c4tfjpf89kfw8pd3b4msbv3
    FOREIGN KEY (task_id) REFERENCES tasks(id);

ALTER TABLE files ADD CONSTRAINT fkbf6pb5mdsgg5if1g0d2yikpjb
    FOREIGN KEY (task_id) REFERENCES tasks(id);

-- Проверяем существование колонки task_id в таблице messages перед созданием внешнего ключа
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'messages' AND column_name = 'task_id'
    ) THEN
        ALTER TABLE messages ADD CONSTRAINT fk1fe2brsvkicrj653duknqxf7h
            FOREIGN KEY (task_id) REFERENCES tasks(id);
    END IF;
END $$;

-- Проверяем существование колонки task_id в таблице notifications перед созданием внешнего ключа
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'notifications' AND column_name = 'task_id'
    ) THEN
        ALTER TABLE notifications ADD CONSTRAINT fk2ktjq1slw0ldkuy5rx8fbte2p
            FOREIGN KEY (task_id) REFERENCES tasks(id);
    END IF;
END $$;