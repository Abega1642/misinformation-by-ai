package dev.razafindratelo.misinformation.endpoint.rest.controller;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.razafindratelo.misinformation.conf.FacadeIT;
import dev.razafindratelo.misinformation.endpoint.rest.controller.model.UserRequest;
import dev.razafindratelo.misinformation.model.Image;
import dev.razafindratelo.misinformation.model.Video;
import dev.razafindratelo.misinformation.model.classifier.ContainerFormat;
import dev.razafindratelo.misinformation.model.classifier.FileType;
import dev.razafindratelo.misinformation.model.classifier.ImageFormat;
import dev.razafindratelo.misinformation.model.classifier.SizeType;
import dev.razafindratelo.misinformation.model.classifier.VideoCodec;
import dev.razafindratelo.misinformation.repository.ImageRepository;
import dev.razafindratelo.misinformation.repository.VideoRepository;
import dev.razafindratelo.misinformation.service.UserService;
import dev.razafindratelo.misinformation.service.media.ImageMetaDataExtractor;
import dev.razafindratelo.misinformation.service.media.VideoMetaDataExtractor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import net.bramp.ffmpeg.FFprobe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
class MediaUploadControllerIT extends FacadeIT {

  private static final String TEST_EMAIL = "rakoto@gmail.com";
  private static final String VIDEO_ENDPOINT = "/api/media/videos/upload";
  private static final String TEST_VIDEO_FILENAME = "test-video.webm";
  private static final String VIDEO_CONTENT_TYPE = "video/webm";
  private static final String FILE_PARAM = "file";
  private static final String EMAIL_PARAM = "userEmail";
  private static final String IMAGE_ENDPOINT = "/api/media/images/upload";
  private static final String TEST_IMAGE_FILENAME = "test-image.png";
  private static final String IMAGE_CONTENT_TYPE = "image/png";

  @Autowired private MockMvc mvc;
  @Autowired private UserService userService;
  @Autowired private VideoRepository videoRepository;
  @Autowired private ImageRepository imageRepository;
  @Autowired private EntityManager entityManager;

  @BeforeEach
  void setUp() {
    videoRepository.deleteAll();
    imageRepository.deleteAll();
    ensureTestUserExists();
  }

  @AfterEach
  void tearDown() {
    videoRepository.deleteAll();
    imageRepository.deleteAll();
  }

  @Test
  void should_upload_video_successfully() throws Exception {
    MockMultipartFile file = createTestVideoFile();

    mvc.perform(multipart(VIDEO_ENDPOINT).file(file).param(EMAIL_PARAM, TEST_EMAIL))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.file_name").value(startsWith("upload-")))
        .andExpect(jsonPath("$.owner.email").value(TEST_EMAIL));
    entityManager.clear();

    var videos = videoRepository.findAllWithOwner();
    assertThat(videos).hasSize(1);
    assertThat(videos.getFirst().getOwner().getEmail()).isEqualTo(TEST_EMAIL);
  }

  @Test
  void should_return_bad_request_for_invalid_email() throws Exception {
    MockMultipartFile file = createTestVideoFile();

    mvc.perform(multipart(VIDEO_ENDPOINT).file(file).param(EMAIL_PARAM, "invalid-email"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_return_bad_request_when_file_is_missing() throws Exception {
    mvc.perform(multipart(VIDEO_ENDPOINT).param(EMAIL_PARAM, TEST_EMAIL))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_return_bad_request_when_email_is_missing() throws Exception {
    MockMultipartFile file = createTestVideoFile();

    mvc.perform(multipart(VIDEO_ENDPOINT).file(file)).andExpect(status().isBadRequest());
  }

  @Test
  void should_return_not_found_when_user_does_not_exist() throws Exception {
    MockMultipartFile file = createTestVideoFile();
    String nonExistentEmail = "nonexistent@example.com";

    mvc.perform(multipart(VIDEO_ENDPOINT).file(file).param(EMAIL_PARAM, nonExistentEmail))
        .andExpect(status().isNotFound());
  }

  @Test
  void should_handle_multiple_video_uploads() throws Exception {
    MockMultipartFile file1 = createTestVideoFile();
    MockMultipartFile file2 =
        new MockMultipartFile(
            FILE_PARAM, "test-video-2.webm", VIDEO_CONTENT_TYPE, "video content 2".getBytes());

    mvc.perform(multipart(VIDEO_ENDPOINT).file(file1).param(EMAIL_PARAM, TEST_EMAIL))
        .andExpect(status().isOk());
    entityManager.clear();

    mvc.perform(multipart(VIDEO_ENDPOINT).file(file2).param(EMAIL_PARAM, TEST_EMAIL))
        .andExpect(status().isOk());

    entityManager.clear();

    var videos = videoRepository.findAllWithOwner();
    assertThat(videos).hasSize(2);
    assertThat(videos).allMatch(video -> video.getOwner().getEmail().equals(TEST_EMAIL));
  }

  @Test
  void should_upload_image_successfully() throws Exception {
    MockMultipartFile file = createTestImageFile();

    mvc.perform(multipart(IMAGE_ENDPOINT).file(file).param(EMAIL_PARAM, TEST_EMAIL))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.file_name").value(startsWith("upload-")))
        .andExpect(jsonPath("$.owner.email").value(TEST_EMAIL));
    entityManager.clear();

    var images = imageRepository.findAllWithOwner();
    assertThat(images).hasSize(1);
    assertThat(images.getFirst().getOwner().getEmail()).isEqualTo(TEST_EMAIL);
  }

  @Test
  void should_return_bad_request_for_invalid_email_on_image_upload() throws Exception {
    MockMultipartFile file = createTestImageFile();

    mvc.perform(multipart(IMAGE_ENDPOINT).file(file).param(EMAIL_PARAM, "invalid-email"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_return_bad_request_when_image_file_is_missing() throws Exception {
    mvc.perform(multipart(IMAGE_ENDPOINT).param(EMAIL_PARAM, TEST_EMAIL))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_return_not_found_when_user_does_not_exist_for_image() throws Exception {
    MockMultipartFile file = createTestImageFile();
    String nonExistentEmail = "nonexistent@example.com";

    mvc.perform(multipart(IMAGE_ENDPOINT).file(file).param(EMAIL_PARAM, nonExistentEmail))
        .andExpect(status().isNotFound());
  }

  @Test
  void should_handle_multiple_image_uploads() throws Exception {
    MockMultipartFile file1 = createTestImageFile();
    MockMultipartFile file2 =
        new MockMultipartFile(
            FILE_PARAM, "test-image-2.png", IMAGE_CONTENT_TYPE, "image content 2".getBytes());

    mvc.perform(multipart(IMAGE_ENDPOINT).file(file1).param(EMAIL_PARAM, TEST_EMAIL))
        .andExpect(status().isOk());
    entityManager.clear();

    mvc.perform(multipart(IMAGE_ENDPOINT).file(file2).param(EMAIL_PARAM, TEST_EMAIL))
        .andExpect(status().isOk());

    entityManager.clear();

    var images = imageRepository.findAllWithOwner();
    assertThat(images).hasSize(2);
    assertThat(images).allMatch(image -> image.getOwner().getEmail().equals(TEST_EMAIL));
  }

  private MockMultipartFile createTestVideoFile() {
    return new MockMultipartFile(
        FILE_PARAM, TEST_VIDEO_FILENAME, VIDEO_CONTENT_TYPE, "video content".getBytes());
  }

  private MockMultipartFile createTestImageFile() {
    return new MockMultipartFile(
        FILE_PARAM, TEST_IMAGE_FILENAME, IMAGE_CONTENT_TYPE, "image content".getBytes());
  }

  private void ensureTestUserExists() {
    try {
      userService.findByEmail(TEST_EMAIL);
    } catch (EntityNotFoundException e) {
      UserRequest request = new UserRequest(TEST_EMAIL, "John Doe", randomUUID().toString());
      userService.registerUser(request);
    }
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    @Primary
    public ImageMetaDataExtractor imageMetaDataExtractor() {
      return new ImageMetaDataExtractor() {
        @Override
        public Image apply(File file) {
          return Image.builder()
              .id(randomUUID().toString())
              .fileName(file.getName())
              .fileType(FileType.IMAGE)
              .size(file.length())
              .sizeType(SizeType.BYTES)
              .createdAt(LocalDateTime.now())
              .width(1920)
              .height(1080)
              .format(ImageFormat.PNG)
              .bitDepth(24)
              .colorModel("DirectColorModel")
              .dpi(72.0)
              .iso(100)
              .build();
        }
      };
    }

    @Bean
    @Primary
    public VideoMetaDataExtractor videoMetaDataExtractor() throws IOException {
      return new VideoMetaDataExtractor(new FFprobe("/usr/bin/ffprobe")) {
        @Override
        public Video apply(File file) {
          return Video.builder()
              .id(randomUUID().toString())
              .fileName(file.getName())
              .fileType(FileType.VIDEO)
              .size(file.length())
              .sizeType(SizeType.BYTES)
              .createdAt(LocalDateTime.now())
              .duration(120.0)
              .width(1920)
              .height(1080)
              .frameRate(30.0)
              .codec(VideoCodec.H264)
              .containerFormat(ContainerFormat.WEBM)
              .build();
        }
      };
    }
  }
}
