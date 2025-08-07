CREATE TABLE IF NOT EXISTS activities (
                            id UUID PRIMARY KEY,
                            user_id UUID NOT NULL REFERENCES users(id),
                            task_id UUID REFERENCES tasks(id),
                            type VARCHAR(50) NOT NULL,
                            description TEXT,
                            created_at TIMESTAMP NOT NULL
);