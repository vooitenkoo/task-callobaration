-- Добавляем колонку project_id только если она не существует
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'tasks' AND column_name = 'project_id'
    ) THEN
        ALTER TABLE tasks ADD COLUMN project_id UUID REFERENCES projects(id);
    END IF;
END $$;