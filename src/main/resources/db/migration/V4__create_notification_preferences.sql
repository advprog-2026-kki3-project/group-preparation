CREATE TABLE notification_preferences (
    username VARCHAR(255) PRIMARY KEY,
    email_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    push_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMP NOT NULL
);
