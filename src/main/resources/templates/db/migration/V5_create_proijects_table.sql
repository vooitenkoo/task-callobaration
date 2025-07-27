CREATE TABLE projects (
                          id UUID PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          description TEXT,
                          created_at TIMESTAMP NOT NULL,
                          created_by_id UUID NOT NULL REFERENCES users(id),
                          status VARCHAR(50) NOT NULL,
                          deadline TIMESTAMP
);