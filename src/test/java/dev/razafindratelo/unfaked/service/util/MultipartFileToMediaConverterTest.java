package dev.razafindratelo.unfaked.service.util;

import static org.junit.jupiter.api.Assertions.*;

import dev.razafindratelo.unfaked.model.Media;
import dev.razafindratelo.unfaked.model.User;
import dev.razafindratelo.unfaked.model.classifier.FileExtension;
import dev.razafindratelo.unfaked.model.classifier.FileType;
import dev.razafindratelo.unfaked.model.classifier.SizeType;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class MultipartFileToMediaConverterTest {
  private static final String TEST_USER_ID = "user-123";
  private static final String TEST_USER_EMAIL = "test@example.com";
  private static final String VIDEO_TEST_FILE = "/videos/test-video-one.mp4";
  private static final String AUDIO_TEST_FILE = "/audios/audio-test.mp3";
  private static final String IMAGE_TEST_FILE = "/images/test-image-1.png";

  private MultipartFileToMediaConverter subject;
  private User testUser;

  @BeforeEach
  void setUp() {
    subject = new MultipartFileToMediaConverter();
    testUser = User.builder().id(TEST_USER_ID).email(TEST_USER_EMAIL).build();
  }

  @Test
  void apply_should_convert_video_file_successfully() throws URISyntaxException, IOException {
    MultipartFile multipartFile = loadTestFile(VIDEO_TEST_FILE, "video/mp4");

    Media media = subject.apply(multipartFile, testUser);

    assertNotNull(media);
    assertNotNull(media.getId());
    assertEquals("test-video-one.mp4", media.getFileName());
    assertEquals(FileExtension.MP4, media.getFileExtension());
    assertEquals(FileType.VIDEO, media.getFileType());
    assertTrue(media.getSize() > 0);
    assertNotNull(media.getSizeType());
    assertNotNull(media.getCreatedAt());
    assertNotNull(media.getBucketKey());
    assertTrue(media.getBucketKey().startsWith("media/" + TEST_USER_ID + "/"));
    assertTrue(media.getBucketKey().endsWith(".mp4"));
    assertEquals(testUser, media.getOwner());
  }

  @Test
  void apply_should_convert_audio_file_successfully() throws URISyntaxException, IOException {
    MultipartFile multipartFile = loadTestFile(AUDIO_TEST_FILE, "audio/mpeg");

    Media media = subject.apply(multipartFile, testUser);

    assertNotNull(media);
    assertNotNull(media.getId());
    assertEquals("audio-test.mp3", media.getFileName());
    assertEquals(FileExtension.MP3, media.getFileExtension());
    assertEquals(FileType.AUDIO, media.getFileType());
    assertTrue(media.getSize() > 0);
    assertNotNull(media.getSizeType());
    assertNotNull(media.getCreatedAt());
    assertNotNull(media.getBucketKey());
    assertTrue(media.getBucketKey().startsWith("media/" + TEST_USER_ID + "/"));
    assertTrue(media.getBucketKey().endsWith(".mp3"));
    assertEquals(testUser, media.getOwner());
  }

  @Test
  void apply_should_convert_image_file_successfully() throws URISyntaxException, IOException {
    MultipartFile multipartFile = loadTestFile(IMAGE_TEST_FILE, "image/png");

    Media media = subject.apply(multipartFile, testUser);

    assertNotNull(media);
    assertNotNull(media.getId());
    assertEquals("test-image-1.png", media.getFileName());
    assertEquals(FileExtension.PNG, media.getFileExtension());
    assertEquals(FileType.IMAGE, media.getFileType());
    assertTrue(media.getSize() > 0);
    assertNotNull(media.getSizeType());
    assertNotNull(media.getCreatedAt());
    assertNotNull(media.getBucketKey());
    assertTrue(media.getBucketKey().startsWith("media/" + TEST_USER_ID + "/"));
    assertTrue(media.getBucketKey().endsWith(".png"));
    assertEquals(testUser, media.getOwner());
  }

  @Test
  void apply_should_use_best_fit_size_type_for_small_files() {
    byte[] smallContent = new byte[512]; // 512 bytes
    MultipartFile multipartFile =
        new MockMultipartFile("file", "small-image.png", "image/png", smallContent);

    Media media = subject.apply(multipartFile, testUser);

    assertEquals(SizeType.BYTES, media.getSizeType());
    assertEquals(512.0, media.getSize(), 0.01);
  }

  @Test
  void apply_should_use_best_fit_size_type_for_large_files() {
    byte[] largeContent = new byte[5 * 1024 * 1024]; // 5MB
    MultipartFile multipartFile =
        new MockMultipartFile("file", "large-video.mp4", "video/mp4", largeContent);

    Media media = subject.apply(multipartFile, testUser);

    assertEquals(SizeType.MB, media.getSizeType());
    assertEquals(5.0, media.getSize(), 0.01);
  }

  @Test
  void apply_should_generate_unique_ids_for_different_files()
      throws URISyntaxException, IOException {
    MultipartFile file1 = loadTestFile(VIDEO_TEST_FILE, "video/mp4");
    MultipartFile file2 = loadTestFile(VIDEO_TEST_FILE, "video/mp4");

    Media media1 = subject.apply(file1, testUser);
    Media media2 = subject.apply(file2, testUser);

    assertNotEquals(media1.getId(), media2.getId());
    assertNotEquals(media1.getBucketKey(), media2.getBucketKey());
  }

  @Test
  void apply_should_throw_exception_when_filename_is_null() {
    MultipartFile multipartFile = new MockMultipartFile("file", null, "video/mp4", new byte[100]);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> subject.apply(multipartFile, testUser));

    assertEquals("File must have a valid filename", exception.getMessage());
  }

  @Test
  void apply_should_throw_exception_when_filename_is_blank() {
    MultipartFile multipartFile = new MockMultipartFile("file", "   ", "video/mp4", new byte[100]);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> subject.apply(multipartFile, testUser));

    assertEquals("File must have a valid filename", exception.getMessage());
  }

  @Test
  void apply_should_throw_exception_when_file_has_no_extension() {
    MultipartFile multipartFile =
        new MockMultipartFile("file", "videofile", "video/mp4", new byte[100]);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> subject.apply(multipartFile, testUser));

    assertEquals("File must have a valid extension", exception.getMessage());
  }

  @Test
  void apply_should_throw_exception_when_file_extension_is_unsupported() {
    MultipartFile multipartFile =
        new MockMultipartFile(
            "file",
            "document.docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            new byte[100]);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> subject.apply(multipartFile, testUser));

    assertTrue(exception.getMessage().contains("Unsupported file extension"));
  }

  @Test
  void apply_should_handle_uppercase_extensions() {
    MultipartFile multipartFile =
        new MockMultipartFile("file", "VIDEO.MP4", "video/mp4", new byte[1024]);

    Media media = subject.apply(multipartFile, testUser);

    assertNotNull(media);
    assertEquals(FileExtension.MP4, media.getFileExtension());
  }

  @Test
  void apply_should_handle_lowercase_extensions() {
    MultipartFile multipartFile =
        new MockMultipartFile("file", "video.mp4", "video/mp4", new byte[1024]);

    Media media = subject.apply(multipartFile, testUser);

    assertNotNull(media);
    assertEquals(FileExtension.MP4, media.getFileExtension());
  }

  @Test
  void apply_should_sanitize_filename_with_special_characters() {
    MultipartFile multipartFile =
        new MockMultipartFile("file", "my video (2024) @test!.mp4", "video/mp4", new byte[1024]);

    Media media = subject.apply(multipartFile, testUser);

    assertNotNull(media);
    String sanitizedName = media.getFileName();
    assertFalse(sanitizedName.contains("@"));
    assertFalse(sanitizedName.contains("!"));
    assertFalse(sanitizedName.contains("("));
    assertFalse(sanitizedName.contains(")"));
  }

  @Test
  void apply_should_include_user_id_in_bucket_key() throws URISyntaxException, IOException {
    User anotherUser = User.builder().id("user-999").email("another@test.com").build();
    MultipartFile multipartFile = loadTestFile(VIDEO_TEST_FILE, "video/mp4");

    Media media = subject.apply(multipartFile, anotherUser);

    assertTrue(media.getBucketKey().contains("user-999"));
    assertFalse(media.getBucketKey().contains(TEST_USER_ID));
  }

  @Test
  void apply_should_include_media_id_in_bucket_key() throws URISyntaxException, IOException {
    MultipartFile multipartFile = loadTestFile(VIDEO_TEST_FILE, "video/mp4");

    Media media = subject.apply(multipartFile, testUser);

    assertTrue(media.getBucketKey().contains(media.getId()));
  }

  @Test
  void apply_should_preserve_file_extension_in_bucket_key() throws URISyntaxException, IOException {
    MultipartFile mp4File = loadTestFile(VIDEO_TEST_FILE, "video/mp4");
    MultipartFile mp3File = loadTestFile(AUDIO_TEST_FILE, "audio/mpeg");
    MultipartFile pngFile = loadTestFile(IMAGE_TEST_FILE, "image/png");

    Media mp4Media = subject.apply(mp4File, testUser);
    Media mp3Media = subject.apply(mp3File, testUser);
    Media pngMedia = subject.apply(pngFile, testUser);

    assertTrue(mp4Media.getBucketKey().endsWith(".mp4"));
    assertTrue(mp3Media.getBucketKey().endsWith(".mp3"));
    assertTrue(pngMedia.getBucketKey().endsWith(".png"));
  }

  @Test
  void apply_should_set_created_at_timestamp() throws URISyntaxException, IOException {
    MultipartFile multipartFile = loadTestFile(VIDEO_TEST_FILE, "video/mp4");

    Media media = subject.apply(multipartFile, testUser);

    assertNotNull(media.getCreatedAt());
  }

  private MultipartFile loadTestFile(String resourcePath, String contentType)
      throws URISyntaxException, IOException {
    var resource = getClass().getResource(resourcePath);
    assertNotNull(resource, "Test resource not found: " + resourcePath);

    Path path = Paths.get(resource.toURI());
    byte[] content = Files.readAllBytes(path);
    String filename = path.getFileName().toString();

    return new MockMultipartFile("file", filename, contentType, content);
  }
}
