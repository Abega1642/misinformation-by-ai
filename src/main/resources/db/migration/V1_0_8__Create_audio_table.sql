CREATE TABLE audio
(
    id          VARCHAR PRIMARY KEY REFERENCES media (id) ON DELETE CASCADE,
    duration    DOUBLE PRECISION,
    bit_rate    INT,
    sample_rate INT,
    channels    INT,
    codec       audio_codec,
    format      container_format
);