package dev.razafindratelo.misinformation.service;

import static dev.razafindratelo.misinformation.model.classifier.FileType.IMAGE;
import static dev.razafindratelo.misinformation.model.classifier.FileType.VIDEO;
import static java.util.UUID.randomUUID;

import dev.razafindratelo.misinformation.exception.MediaUploadException;
import dev.razafindratelo.misinformation.file.BucketComponent;
import dev.razafindratelo.misinformation.mapper.ImageMapper;
import dev.razafindratelo.misinformation.mapper.VideoMapper;
import dev.razafindratelo.misinformation.model.Image;
import dev.razafindratelo.misinformation.model.Video;
import dev.razafindratelo.misinformation.model.classifier.FileType.*;
import dev.razafindratelo.misinformation.repository.ImageRepository;
import dev.razafindratelo.misinformation.repository.VideoRepository;
import dev.razafindratelo.misinformation.service.media.ImageMetaDataExtractor;
import dev.razafindratelo.misinformation.service.media.MultipartFileConverter;
import dev.razafindratelo.misinformation.service.media.ValidMediaType;
import dev.razafindratelo.misinformation.service.media.VideoMetaDataExtractor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@Validated
@RequiredArgsConstructor
public class MediaService {
  private final BucketComponent bucket;
  private final VideoMetaDataExtractor videoExtractor;
  private final ImageMetaDataExtractor imageExtractor;
  private final VideoRepository videoRepository;
  private final ImageRepository imageRepository;
  private final ImageMapper imageMapper;
  private final UserService userService;
  private final MultipartFileConverter fileConverter;
  private final VideoMapper videoMapper;

  public Video uploadVideo(
      @ValidMediaType(VIDEO) @NotNull MultipartFile file, @NotNull @Email String userEmail) {
    var owner = userService.findByEmail(userEmail);
    try {
      var videoFile = fileConverter.convert(file);
      var video = videoExtractor.apply(videoFile);
      video.setOwner(owner);

      String bucketKey = generateBucketKey("videos");

      log.info(
          "Uploading video: id={}, name={}, duration={}, bucketKey={}, owner={}",
          video.getId(),
          video.getFileName(),
          video.getDuration(),
          bucketKey,
          userEmail);

      bucket.upload(videoFile, bucketKey);
      video.setBucketKey(bucketKey);

      log.info("Video uploaded successfully. Pre-signed URL generated for id={}", video.getId());

      videoRepository.save(videoMapper.toPersistenceModel(video));

      return video;
    } catch (IOException e) {
      log.error("Failed to upload video for owner: {}", userEmail, e);
      throw new MediaUploadException("Failed to upload video : " + e);
    }
  }

  public Image uploadImage(
      @ValidMediaType(IMAGE) @NotNull MultipartFile file, @NotNull @Email String userEmail) {
    var owner = userService.findByEmail(userEmail);
    try {
      var imageFile = fileConverter.convert(file);
      var image = imageExtractor.apply(imageFile);
      image.setOwner(owner);

      String bucketKey = generateBucketKey("images");

      log.info(
          "Uploading image: id={}, name={}, format={}, bucketKey={}, owner={}",
          image.getId(),
          image.getFileName(),
          image.getFormat(),
          bucketKey,
          userEmail);

      bucket.upload(imageFile, bucketKey);
      image.setBucketKey(bucketKey);

      log.info("Image uploaded successfully. Pre-signed URL generated for id={}", image.getId());

      imageRepository.save(imageMapper.toPersistenceModel(image));

      return image;
    } catch (IOException e) {
      log.error("Failed to upload image for owner: {}", userEmail, e);
      throw new MediaUploadException("Failed to upload image : " + e);
    }
  }

  private String generateBucketKey(String prefix) {
    return prefix + "/" + randomUUID();
  }
}
