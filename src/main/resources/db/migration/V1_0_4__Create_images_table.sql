CREATE TABLE image
(
    id          VARCHAR PRIMARY KEY REFERENCES media (id) ON DELETE CASCADE,
    width       INT,
    height      INT,
    format      image_format,
    bit_depth   INT,
    color_model TEXT,
    dpi         DOUBLE PRECISION,
    iso         INT
);