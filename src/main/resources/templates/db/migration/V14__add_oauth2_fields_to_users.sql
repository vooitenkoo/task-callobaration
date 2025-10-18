-- Add OAuth2 fields to users table
ALTER TABLE users 
ADD COLUMN provider VARCHAR(50),
ADD COLUMN provider_id VARCHAR(255),
ADD COLUMN email_verified BOOLEAN DEFAULT FALSE,
ADD COLUMN image_url VARCHAR(1000);

-- Create unique constraint for provider and provider_id combination
ALTER TABLE users 
ADD CONSTRAINT uk_users_provider_provider_id UNIQUE (provider, provider_id);

-- Update existing users to have LOCAL provider
UPDATE users SET provider = 'LOCAL' WHERE provider IS NULL;

-- Make provider column NOT NULL after setting default values
ALTER TABLE users ALTER COLUMN provider SET NOT NULL;
