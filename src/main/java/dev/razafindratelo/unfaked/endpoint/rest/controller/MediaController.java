package dev.razafindratelo.unfaked.endpoint.rest.controller;

import static org.owasp.encoder.Encode.forJava;

import dev.razafindratelo.unfaked.model.Media;
import dev.razafindratelo.unfaked.service.MediaService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/medias")
@RequiredArgsConstructor
@Validated
@Slf4j
public class MediaController {
  private final MediaService mediaService;

  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Media> uploadMedia(
      @RequestParam("from-userEmail") @Email @NotBlank String email,
      @RequestParam("file") @NotNull MultipartFile file) {

    log.info(
        "Received media upload request: userEmail={}, filename={}, size={} bytes",
        forJava(email),
        forJava(file.getOriginalFilename()),
        file.getSize());

    var uploadedMedia = mediaService.uploadMedia(file, email);

    log.info(
        "Media uploaded successfully: mediaId={}, userEmail={}",
        forJava(uploadedMedia.getId()),
        forJava(email));

    return ResponseEntity.status(HttpStatus.CREATED).body(uploadedMedia);
  }
}
