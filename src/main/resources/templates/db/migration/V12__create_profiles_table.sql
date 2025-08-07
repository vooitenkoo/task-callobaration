-- src/main/resources/db/migration/V12__create_profiles_table.sql
CREATE TABLE IF NOT EXISTS profiles (
                          id UUID PRIMARY KEY REFERENCES users(id),
                          avatar_url VARCHAR(1000),
                          bio VARCHAR(1000),
                          location VARCHAR(255),
                          job_title VARCHAR(100)
);