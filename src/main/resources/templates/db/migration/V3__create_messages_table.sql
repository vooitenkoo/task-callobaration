CREATE TABLE IF NOT EXISTS messages (
                          id UUID PRIMARY KEY,
                          task_id UUID NOT NULL,
                          sender_id UUID NOT NULL,
                          content VARCHAR(1000) NOT NULL,
                          sent_at TIMESTAMP WITH TIME ZONE NOT NULL,
                          FOREIGN KEY (task_id) REFERENCES tasks(id),
                          FOREIGN KEY (sender_id) REFERENCES users(id)
);