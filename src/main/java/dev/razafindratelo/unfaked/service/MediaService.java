package dev.razafindratelo.unfaked.service;

import static org.owasp.encoder.Encode.forJava;

import dev.razafindratelo.unfaked.file.BucketComponent;
import dev.razafindratelo.unfaked.file.FilenameSanitizer;
import dev.razafindratelo.unfaked.mapper.MediaMapper;
import dev.razafindratelo.unfaked.model.Media;
import dev.razafindratelo.unfaked.model.User;
import dev.razafindratelo.unfaked.repository.MediaRepository;
import dev.razafindratelo.unfaked.repository.model.JMedia;
import dev.razafindratelo.unfaked.service.util.MultipartFileToMediaConverter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaService {

  private static final int KILO = 1024;
  private static final int MAX_SIZE_ALLOWED = 100 * KILO * KILO;
  private final MediaRepository mediaRepository;
  private final MediaMapper mediaMapper;
  private final BucketComponent bucketComponent;
  private final MultipartFileToMediaConverter mediaConverter;
  private final UserService userService;
  private final FilenameSanitizer filenameSanitizer;

  @Transactional
  public Media uploadMedia(MultipartFile multipartFile, String userEmail) {
    log.info(
        "Starting media upload: userEmail={}, filename={}, size={} bytes",
        forJava(userEmail),
        forJava(multipartFile.getOriginalFilename()),
        multipartFile.getSize());

    validateMultipartFile(multipartFile);

    User owner = userService.findByEmail(userEmail);
    log.debug("User found: userId={}", forJava(owner.getId()));

    Media media = mediaConverter.apply(multipartFile, owner);
    log.debug(
        "Media object created: mediaId={}, bucketKey={}",
        forJava(media.getId()),
        forJava(media.getBucketKey()));

    File tempFile = null;
    try {
      tempFile = convertMultipartFileToFile(multipartFile);
      bucketComponent.upload(tempFile, media.getBucketKey());
      log.info("File uploaded to bucket successfully: bucketKey={}", forJava(media.getBucketKey()));
    } catch (IOException e) {
      log.error(
          "Failed to convert multipart file to temporary file: filename={}",
          forJava(multipartFile.getOriginalFilename()),
          e);
      throw new RuntimeException("Failed to process file for upload", e);
    } finally {
      cleanupTempFile(tempFile);
    }

    JMedia jMedia = mediaMapper.toPersistence(media);
    JMedia savedJMedia = mediaRepository.save(jMedia);
    log.info("Media saved to database: mediaId={}", forJava(savedJMedia.getId()));

    Media result = mediaMapper.toCoreModel(savedJMedia);
    log.info("Media upload completed successfully: mediaId={}", forJava(result.getId()));
    return result;
  }

  private void validateMultipartFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      log.error("Multipart file is null or empty");
      throw new IllegalArgumentException("File cannot be null or empty");
    }

    if (file.getSize() == 0) {
      log.error("File size is zero: filename={}", forJava(file.getOriginalFilename()));
      throw new IllegalArgumentException("File cannot have zero size");
    }

    if (file.getSize() > MAX_SIZE_ALLOWED) {
      log.error(
          "File size exceeds maximum allowed: size={} bytes, max={} bytes",
          file.getSize(),
          MAX_SIZE_ALLOWED);
      throw new IllegalArgumentException("File size exceeds maximum allowed size of 100MB");
    }
  }

  private File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
    String sanitizedFilename = filenameSanitizer.apply(multipartFile.getOriginalFilename());
    var tempPath = Files.createTempFile("upload-", "-" + sanitizedFilename);
    var tempFile = tempPath.toFile();

    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
      fos.write(multipartFile.getBytes());
    }

    return tempFile;
  }

  private void cleanupTempFile(File tempFile) {
    if (tempFile != null && tempFile.exists()) {
      boolean deleted = tempFile.delete();
      if (!deleted)
        log.warn("Failed to delete temporary file: path={}", forJava(tempFile.getAbsolutePath()));
    }
  }
}
