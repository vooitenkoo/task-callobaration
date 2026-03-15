-- Add lastReadAt field to project_members for unread message counting
ALTER TABLE project_members 
ADD COLUMN last_read_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
