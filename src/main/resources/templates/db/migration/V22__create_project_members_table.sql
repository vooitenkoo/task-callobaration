CREATE TABLE IF NOT EXISTS project_members (
                                               id UUID PRIMARY KEY,
                                               user_id UUID NOT NULL REFERENCES users(id),
                                               project_id UUID NOT NULL REFERENCES projects(id),
                                               role VARCHAR(20) NOT NULL,
                                               CONSTRAINT uq_project_member UNIQUE (user_id, project_id)
);