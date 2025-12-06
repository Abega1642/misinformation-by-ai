package dev.razafindratelo.misinformation.service;

import static java.util.UUID.randomUUID;

import dev.razafindratelo.misinformation.exception.MediaUploadException;
import dev.razafindratelo.misinformation.file.BucketComponent;
import dev.razafindratelo.misinformation.mapper.VideoMapper;
import dev.razafindratelo.misinformation.model.Video;
import dev.razafindratelo.misinformation.repository.VideoRepository;
import dev.razafindratelo.misinformation.service.media.MultipartFileConverter;
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
  private final VideoRepository videoRepository;
  private final UserService userService;
  private final MultipartFileConverter fileConverter;
  private final VideoMapper videoMapper;

  public Video uploadVideo(@NotNull MultipartFile file, @NotNull @Email String userEmail) {
    var owner = userService.findByEmail(userEmail);
    try {
      var videoFile = fileConverter.convert(file);
      var video = videoExtractor.apply(videoFile);
      video.setOwner(owner);

      String bucketKey = "videos/" + randomUUID();

      log.info(
          "Uploading video: id={}, name={}, duration={}, bucketKey={}, user={}",
          video.getId(),
          video.getFileName(),
          video.getDuration(),
          bucketKey,
          userEmail);

      bucket.upload(videoFile, bucketKey);
      video.setFilePath(bucketKey);

      log.info("Video uploaded successfully. Pre-signed URL generated for id={}", video.getId());

      videoRepository.save(videoMapper.toPersistenceModel(video));

      return video;
    } catch (IOException e) {
      log.error("Failed to upload video for user: {}", userEmail, e);
      throw new MediaUploadException("Failed to upload video : " + e);
    }
  }
}
