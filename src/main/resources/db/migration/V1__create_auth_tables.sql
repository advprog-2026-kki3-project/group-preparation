CREATE TABLE auth_users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    primary_role VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    disabled_at TIMESTAMPTZ NULL,
    CONSTRAINT chk_auth_users_role CHECK (primary_role IN ('ADMINISTRATOR', 'SELLER', 'BUYER')),
    CONSTRAINT chk_auth_users_status CHECK (status IN ('ACTIVE', 'DISABLED')),
    CONSTRAINT chk_auth_users_email_lower CHECK (email = lower(email))
);

CREATE TABLE auth_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth_users(id) ON DELETE CASCADE,
    ip_address VARCHAR(64) NULL,
    user_agent VARCHAR(512) NULL,
    created_at TIMESTAMPTZ NOT NULL,
    last_seen_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ NULL,
    revoke_reason VARCHAR(255) NULL
);

CREATE INDEX idx_auth_sessions_user_active
    ON auth_sessions (user_id, created_at)
    WHERE revoked_at IS NULL;

CREATE TABLE auth_refresh_tokens (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES auth_sessions(id) ON DELETE CASCADE,
    token_hash VARCHAR(128) NOT NULL UNIQUE,
    token_family_id UUID NOT NULL,
    issued_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ NULL,
    replaced_by_token_id UUID NULL
);

CREATE INDEX idx_auth_refresh_tokens_session_active
    ON auth_refresh_tokens (session_id, issued_at)
    WHERE revoked_at IS NULL;
