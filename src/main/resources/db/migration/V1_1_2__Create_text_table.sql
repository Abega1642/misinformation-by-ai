CREATE TABLE texts
(
    id         VARCHAR(40) PRIMARY KEY,
    owner_id   VARCHAR(40)              NOT NULL,
    value      TEXT                     NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_texts_owner FOREIGN KEY (owner_id)
        REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_texts_owner_id ON texts (owner_id);
CREATE INDEX idx_texts_created_at ON texts (created_at);
CREATE INDEX idx_texts_owner_created ON texts (owner_id, created_at DESC);