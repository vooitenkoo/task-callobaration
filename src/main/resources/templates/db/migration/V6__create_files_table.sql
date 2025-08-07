CREATE TABLE IF NOT EXISTS files (
                       id UUID PRIMARY KEY,
                       task_id UUID NOT NULL REFERENCES tasks(id),
                       name VARCHAR(255) NOT NULL,
                       url VARCHAR(1000) NOT NULL,
                       uploaded_at TIMESTAMP NOT NULL,
                       uploaded_by_id UUID NOT NULL REFERENCES users(id)
);