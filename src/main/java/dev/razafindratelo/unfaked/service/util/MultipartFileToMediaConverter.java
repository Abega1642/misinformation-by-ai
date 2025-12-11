package dev.razafindratelo.unfaked.service.util;

import static java.util.UUID.randomUUID;
import static org.owasp.encoder.Encode.forJava;

import dev.razafindratelo.unfaked.model.Media;
import dev.razafindratelo.unfaked.model.User;
import dev.razafindratelo.unfaked.model.classifier.FileExtension;
import dev.razafindratelo.unfaked.model.classifier.SizeType;
import java.time.LocalDateTime;
import java.util.function.BiFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
public class MultipartFileToMediaConverter implements BiFunction<MultipartFile, User, Media> {

  @Override
  public Media apply(MultipartFile file, User owner) {
    log.debug(
        "Converting multipart file to Media object: filename={}",
        forJava(file.getOriginalFilename()));

    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null || originalFilename.isBlank()) {
      log.error("Original filename is null or blank");
      throw new IllegalArgumentException("File must have a valid filename");
    }

    FileExtension fileExtension = extractFileExtension(originalFilename);
    long sizeInBytes = file.getSize();
    SizeType bestSizeType = SizeType.bestFit(sizeInBytes);
    double convertedSize = SizeType.convert(sizeInBytes, bestSizeType);

    String mediaId = randomUUID().toString();
    String bucketKey = generateBucketKey(owner.getId(), mediaId, fileExtension);

    log.debug(
        "Media conversion complete: id={}, bucketKey={}, size={} {}",
        forJava(mediaId),
        forJava(bucketKey),
        convertedSize,
        bestSizeType);

    return Media.builder()
        .id(mediaId)
        .fileName(sanitizeFilename(originalFilename))
        .size(convertedSize)
        .sizeType(bestSizeType)
        .fileExtension(fileExtension)
        .createdAt(LocalDateTime.now())
        .bucketKey(bucketKey)
        .owner(owner)
        .build();
  }

  private FileExtension extractFileExtension(String filename) {
    int lastDotIndex = filename.lastIndexOf('.');
    if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
      log.error("File has no extension: filename={}", forJava(filename));
      throw new IllegalArgumentException("File must have a valid extension");
    }

    String extension = filename.substring(lastDotIndex + 1).toUpperCase();
    try {
      return FileExtension.valueOf(extension);
    } catch (IllegalArgumentException e) {
      log.error("Unsupported file extension: extension={}", forJava(extension));
      throw new IllegalArgumentException("Unsupported file extension: " + extension);
    }
  }

  private String generateBucketKey(String userId, String mediaId, FileExtension extension) {
    return String.format("media/%s/%s.%s", userId, mediaId, extension.name().toLowerCase());
  }

  private String sanitizeFilename(String filename) {
    // Remove path traversal attempts and dangerous characters
    return filename
        .replaceAll("[^a-zA-Z0-9._-]", "_")
        .replaceAll("\\.\\.", "")
        .substring(0, Math.min(filename.length(), 255));
  }
}
