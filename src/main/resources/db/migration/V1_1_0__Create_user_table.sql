CREATE TYPE user_role_enum AS ENUM ('USER', 'ADMIN');
CREATE TYPE user_status_enum AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED');

CREATE TABLE users
(
    id                VARCHAR(40) PRIMARY KEY,
    email             VARCHAR(255)             NOT NULL,
    full_name         VARCHAR(255)             NOT NULL,
    clerk_id          VARCHAR(40)              NOT NULL,
    password          VARCHAR(255),
    user_role         user_role_enum           NOT NULL,
    user_status       user_status_enum         NOT NULL,
    is_email_verified BOOLEAN                  NOT NULL DEFAULT false,
    updated_at        TIMESTAMP WITH TIME ZONE,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT users_email_unique UNIQUE (email),
    CONSTRAINT users_clerk_id_unique UNIQUE (clerk_id)
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_clerk_id ON users (clerk_id);
CREATE INDEX idx_users_created_at ON users (created_at);