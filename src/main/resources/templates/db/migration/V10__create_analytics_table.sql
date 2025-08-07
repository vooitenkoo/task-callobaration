CREATE TABLE IF NOT EXISTS analytics (
                           id UUID PRIMARY KEY,
                           project_id UUID NOT NULL REFERENCES projects(id),
                           metric_type VARCHAR(50) NOT NULL,
                           value DOUBLE PRECISION NOT NULL,
                           timestamp TIMESTAMP NOT NULL
);