DROP TABLE IF EXISTS project_members;

CREATE TABLE project_members (
                                 id BIGSERIAL PRIMARY KEY,
                                 user_id UUID NOT NULL REFERENCES users(id),
                                 project_id UUID NOT NULL REFERENCES projects(id),
                                 role VARCHAR(50) NOT NULL
);