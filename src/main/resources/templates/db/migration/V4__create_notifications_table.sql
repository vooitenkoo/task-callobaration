CREATE TABLE notifications (
                               id UUID PRIMARY KEY,
                               recipient_id UUID NOT NULL,
                               task_id UUID,
                               type VARCHAR(50) NOT NULL,
                               message VARCHAR(255) NOT NULL,
                               is_read BOOLEAN NOT NULL DEFAULT FALSE,
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                               FOREIGN KEY (recipient_id) REFERENCES users(id),
                               FOREIGN KEY (task_id) REFERENCES tasks(id)
);