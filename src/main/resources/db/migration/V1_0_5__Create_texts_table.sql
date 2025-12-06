CREATE TABLE texts
(
    id         VARCHAR(40) PRIMARY KEY,
    owner_id   VARCHAR(40) NOT NULL,
    value      TEXT        NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,

    FOREIGN KEY (owner_id) REFERENCES users ("id")
);