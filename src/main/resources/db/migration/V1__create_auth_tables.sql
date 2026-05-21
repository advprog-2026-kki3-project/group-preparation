CREATE TABLE auth_users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    primary_role VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    disabled_at TIMESTAMP WITH TIME ZONE NULL,
    CONSTRAINT chk_auth_users_role CHECK (primary_role IN ('ADMINISTRATOR', 'SELLER', 'BUYER')),
    CONSTRAINT chk_auth_users_status CHECK (status IN ('ACTIVE', 'DISABLED')),
    CONSTRAINT chk_auth_users_email_lower CHECK (email = lower(email))
);

CREATE TABLE auth_user_two_factor_settings (
    user_id UUID PRIMARY KEY REFERENCES auth_users(id) ON DELETE CASCADE,
    enabled BOOLEAN NOT NULL,
    method VARCHAR(32) NULL,
    pending_method VARCHAR(32) NULL,
    totp_secret VARCHAR(64) NULL,
    pending_totp_secret VARCHAR(64) NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_auth_user_2fa_method CHECK (method IS NULL OR method IN ('EMAIL_OTP', 'TOTP')),
    CONSTRAINT chk_auth_user_2fa_pending_method CHECK (pending_method IS NULL OR pending_method IN ('EMAIL_OTP', 'TOTP'))
);

CREATE TABLE auth_two_factor_challenges (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth_users(id) ON DELETE CASCADE,
    purpose VARCHAR(32) NOT NULL,
    method VARCHAR(32) NOT NULL,
    code_hash VARCHAR(128) NOT NULL,
    attempts INTEGER NOT NULL,
    max_attempts INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    consumed_at TIMESTAMP WITH TIME ZONE NULL,
    CONSTRAINT chk_auth_2fa_challenge_purpose CHECK (purpose IN ('LOGIN', 'ENABLE', 'DISABLE', 'CHANGE')),
    CONSTRAINT chk_auth_2fa_challenge_method CHECK (method IN ('EMAIL_OTP', 'TOTP'))
);

CREATE INDEX idx_auth_two_factor_challenges_user_active
    ON auth_two_factor_challenges (user_id, created_at)
    WHERE consumed_at IS NULL;

CREATE TABLE auth_login_attempts (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    ip_address VARCHAR(64) NULL,
    successful BOOLEAN NOT NULL,
    attempted_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_auth_login_attempts_email_time
    ON auth_login_attempts (email, attempted_at);

CREATE TABLE auth_policy_settings (
    id VARCHAR(32) PRIMARY KEY,
    max_concurrent_sessions INTEGER NOT NULL,
    concurrent_session_policy VARCHAR(32) NOT NULL,
    login_attempt_limit INTEGER NOT NULL,
    login_attempt_window_seconds BIGINT NOT NULL,
    otp_attempt_limit INTEGER NOT NULL,
    otp_ttl_seconds BIGINT NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_auth_policy_concurrent_session_policy
        CHECK (concurrent_session_policy IN ('REJECT_NEW', 'REVOKE_OLDEST')),
    CONSTRAINT chk_auth_policy_positive_limits
        CHECK (
            max_concurrent_sessions > 0
            AND login_attempt_limit > 0
            AND login_attempt_window_seconds > 0
            AND otp_attempt_limit > 0
            AND otp_ttl_seconds > 0
        )
);

CREATE TABLE auth_roles (
    id UUID PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    system_role BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE auth_permissions (
    id UUID PRIMARY KEY,
    name VARCHAR(128) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE auth_user_roles (
    user_id UUID NOT NULL REFERENCES auth_users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES auth_roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE auth_role_permissions (
    role_id UUID NOT NULL REFERENCES auth_roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES auth_permissions(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE auth_user_permissions (
    user_id UUID NOT NULL REFERENCES auth_users(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES auth_permissions(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (user_id, permission_id)
);

CREATE TABLE auth_sessions (
   id UUID PRIMARY KEY,
   user_id UUID NOT NULL REFERENCES auth_users(id) ON DELETE CASCADE,
   ip_address VARCHAR(64) NULL,
   user_agent VARCHAR(512) NULL,
   two_factor_verified BOOLEAN NOT NULL DEFAULT FALSE,
   created_at TIMESTAMP WITH TIME ZONE NOT NULL,
   last_seen_at TIMESTAMP WITH TIME ZONE NOT NULL,
   expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
   revoked_at TIMESTAMP WITH TIME ZONE NULL,
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
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE NULL,
    replaced_by_token_id UUID NULL
);

CREATE INDEX idx_auth_refresh_tokens_session_active
    ON auth_refresh_tokens (session_id, issued_at)
    WHERE revoked_at IS NULL;

INSERT INTO auth_roles (id, name, description, system_role, created_at, updated_at)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'ADMINISTRATOR', 'Built-in administrator role', TRUE, now(), now()),
    ('00000000-0000-0000-0000-000000000002', 'SELLER', 'Built-in seller role', TRUE, now(), now()),
    ('00000000-0000-0000-0000-000000000003', 'BUYER', 'Built-in buyer role', TRUE, now(), now());

INSERT INTO auth_permissions (id, name, description, created_at)
VALUES ('00000000-0000-0000-0000-000000000101', 'auth:admin', 'Manage authentication users, roles, and permissions', now());

INSERT INTO auth_role_permissions (role_id, permission_id, assigned_at)
VALUES ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000101', now());
