package dev.razafindratelo.misinformation.endpoint.rest.controller;

import dev.razafindratelo.misinformation.model.Image;
import dev.razafindratelo.misinformation.model.Video;
import dev.razafindratelo.misinformation.service.MediaService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/media")
@Slf4j
public class MediaUploadController {
  private final MediaService mediaService;

  @PostMapping("/videos/upload")
  public ResponseEntity<Video> uploadVideo(
      @RequestParam("file") @NotNull MultipartFile file,
      @RequestParam("userEmail") @Email String userEmail) {

    log.info("Video upload request received from owner with email={}", userEmail);

    Video uploadedVideo = mediaService.uploadVideo(file, userEmail);
    return ResponseEntity.ok(uploadedVideo);
  }

  @PostMapping("/images/upload")
  public Image uploadImage(
      @RequestParam("file") @NotNull MultipartFile file,
      @RequestParam("userEmail") @Email String userEmail) {

    log.info("Image upload request received from owner with email={}", userEmail);

    return mediaService.uploadImage(file, userEmail);
  }
}
