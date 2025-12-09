package dev.razafindratelo.misinformation.model.classifier;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileExtension {
  // Images
  JPG(FileType.IMAGE),
  JPEG(FileType.IMAGE),
  PNG(FileType.IMAGE),
  GIF(FileType.IMAGE),
  WEBP(FileType.IMAGE),
  SVG(FileType.IMAGE),
  BMP(FileType.IMAGE),
  TIFF(FileType.IMAGE),
  ICO(FileType.IMAGE),

  // Videos
  MP4(FileType.VIDEO),
  AVI(FileType.VIDEO),
  MOV(FileType.VIDEO),
  WMV(FileType.VIDEO),
  FLV(FileType.VIDEO),
  MKV(FileType.VIDEO),
  WEBM(FileType.VIDEO),
  MPEG(FileType.VIDEO),
  MPG(FileType.VIDEO),

  // Audio
  MP3(FileType.AUDIO),
  WAV(FileType.AUDIO),
  FLAC(FileType.AUDIO),
  AAC(FileType.AUDIO),
  OGG(FileType.AUDIO),
  WMA(FileType.AUDIO),
  M4A(FileType.AUDIO),
  OPUS(FileType.AUDIO),
  AIFF(FileType.AUDIO);

  private final FileType fileType;

  public static FileExtension fromString(String extension) {
    if (extension == null || extension.isBlank()) {
      throw new IllegalArgumentException("File extension cannot be null or empty");
    }

    String normalized = extension.toUpperCase().replace(".", "");

    try {
      return FileExtension.valueOf(normalized);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Unsupported file extension: " + extension);
    }
  }

  public boolean isCompatibleWith(FileType type) {
    return this.fileType == type;
  }
}
