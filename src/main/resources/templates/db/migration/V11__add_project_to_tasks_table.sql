ALTER TABLE tasks
    ADD COLUMN project_id UUID REFERENCES projects(id);