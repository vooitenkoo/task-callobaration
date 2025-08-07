CREATE TABLE IF NOT EXISTS tasks (
                       id UUID PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       description VARCHAR(1000),
                       deadline TIMESTAMP WITH TIME ZONE,
                       status VARCHAR(50) NOT NULL,
                       file_url VARCHAR(255),
                       created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                       created_by_id UUID NOT NULL,
                       assignee_id UUID,
                       FOREIGN KEY (created_by_id) REFERENCES users(id),
                       FOREIGN KEY (assignee_id) REFERENCES users(id)
);