CREATE TABLE users
(
    id       VARCHAR(40) PRIMARY KEY,
    email    VARCHAR(255) NOT NULL,
    clerk_id VARCHAR(40)  NOT NULL
);