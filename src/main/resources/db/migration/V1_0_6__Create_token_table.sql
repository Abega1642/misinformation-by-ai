CREATE TABLE tokens
(
    id              VARCHAR(40) PRIMARY KEY,
    owner_id        VARCHAR(40) NOT NULL,
    token           TEXT        NOT NULL,
    expiration_date DATE        NOT NULL,

    FOREIGN KEY ("owner_id") REFERENCES users ("id")
);