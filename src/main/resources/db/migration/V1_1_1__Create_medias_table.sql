CREATE TYPE size_type_enum AS ENUM ('BYTES', 'KB', 'MB', 'GB', 'TB');

CREATE TYPE file_extension_enum AS ENUM (
    'JPG', 'JPEG', 'PNG', 'GIF', 'WEBP', 'SVG', 'BMP', 'TIFF', 'ICO',
    'MP4', 'AVI', 'MOV', 'WMV', 'FLV', 'MKV', 'WEBM', 'MPEG', 'MPG',
    'MP3', 'WAV', 'FLAC', 'AAC', 'OGG', 'WMA', 'M4A', 'OPUS', 'AIFF'
    );

CREATE TABLE medias
(
    id             VARCHAR(40) PRIMARY KEY,
    file_name      VARCHAR(255)             NOT NULL,
    size           DOUBLE PRECISION         NOT NULL,
    size_type      size_type_enum           NOT NULL,
    file_extension file_extension_enum      NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    bucket_key     VARCHAR(500)             NOT NULL,
    owner_id       VARCHAR(40)              NOT NULL,

    CONSTRAINT fk_medias_owner FOREIGN KEY (owner_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT medias_size_positive CHECK (size >= 0)
);

CREATE INDEX idx_medias_owner_id ON medias (owner_id);
CREATE INDEX idx_medias_created_at ON medias (created_at);
CREATE INDEX idx_medias_file_extension ON medias (file_extension);
CREATE INDEX idx_medias_bucket_key ON medias (bucket_key);