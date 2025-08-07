CREATE TABLE IF NOT EXISTS teams (
                       id UUID PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       description TEXT,
                       created_at TIMESTAMP NOT NULL,
                       created_by_id UUID NOT NULL REFERENCES users(id)
);