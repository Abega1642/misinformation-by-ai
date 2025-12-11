package dev.razafindratelo.unfaked.service;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.razafindratelo.unfaked.file.BucketComponent;
import dev.razafindratelo.unfaked.mapper.MediaMapper;
import dev.razafindratelo.unfaked.model.Media;
import dev.razafindratelo.unfaked.model.User;
import dev.razafindratelo.unfaked.model.classifier.FileExtension;
import dev.razafindratelo.unfaked.model.classifier.SizeType;
import dev.razafindratelo.unfaked.repository.MediaRepository;
import dev.razafindratelo.unfaked.repository.model.JMedia;
import dev.razafindratelo.unfaked.service.util.MultipartFileToMediaConverter;
import java.io.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class MediaServiceIT {

  private static final String TEST_USER_EMAIL = "test@example.com";
  private static final String TEST_USER_ID = "user-123";
  private static final String TEST_MEDIA_ID = "media-456";
  private static final String TEST_FILENAME = "test-video.mp4";
  private static final String TEST_BUCKET_KEY = "media/user-123/media-456.mp4";
  private static final long TEST_FILE_SIZE_BYTES = 5_242_880L; // 5MB
  private static final double TEST_FILE_SIZE_MB = 5.0;

  @Mock private MediaRepository mediaRepository;

  @Mock private MediaMapper mediaMapper;

  @Mock private BucketComponent bucketComponent;

  @Mock private MultipartFileToMediaConverter mediaConverter;

  @Mock private UserService userService;

  @Mock private MultipartFile multipartFile;

  @InjectMocks private MediaService subject;

  private User testUser;
  private Media testMedia;
  private JMedia testJMedia;

  @BeforeEach
  void setUp() {
    testUser = User.builder().id(TEST_USER_ID).email(TEST_USER_EMAIL).build();

    testMedia =
        Media.builder()
            .id(TEST_MEDIA_ID)
            .fileName(TEST_FILENAME)
            .size(TEST_FILE_SIZE_MB)
            .sizeType(SizeType.MB)
            .fileExtension(FileExtension.MP4)
            .createdAt(now())
            .bucketKey(TEST_BUCKET_KEY)
            .owner(testUser)
            .build();

    testJMedia =
        JMedia.builder()
            .id(TEST_MEDIA_ID)
            .fileName(TEST_FILENAME)
            .size(TEST_FILE_SIZE_MB)
            .sizeType(SizeType.MB)
            .fileExtension(FileExtension.MP4)
            .createdAt(now())
            .bucketKey(TEST_BUCKET_KEY)
            .build();
  }

  @Test
  void upload_media_should_succeed_with_valid_input() throws Exception {
    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getSize()).thenReturn(TEST_FILE_SIZE_BYTES);
    when(multipartFile.getOriginalFilename()).thenReturn(TEST_FILENAME);
    when(multipartFile.getBytes()).thenReturn(new byte[100]);

    when(userService.findByEmail(TEST_USER_EMAIL)).thenReturn(testUser);
    when(mediaConverter.apply(multipartFile, testUser)).thenReturn(testMedia);
    when(mediaMapper.toPersistence(testMedia)).thenReturn(testJMedia);
    when(mediaRepository.save(testJMedia)).thenReturn(testJMedia);
    when(mediaMapper.toCoreModel(testJMedia)).thenReturn(testMedia);

    Media result = subject.uploadMedia(multipartFile, TEST_USER_EMAIL);

    assertNotNull(result);
    assertEquals(TEST_MEDIA_ID, result.getId());
    assertEquals(TEST_FILENAME, result.getFileName());
    assertEquals(TEST_BUCKET_KEY, result.getBucketKey());

    verify(userService).findByEmail(TEST_USER_EMAIL);
    verify(mediaConverter).apply(multipartFile, testUser);
    verify(bucketComponent).upload(any(File.class), eq(TEST_BUCKET_KEY));
    verify(mediaRepository).save(testJMedia);
    verify(mediaMapper).toCoreModel(testJMedia);
  }

  @Test
  void upload_media_should_throw_exception_when_file_is_empty() {
    when(multipartFile.isEmpty()).thenReturn(true);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> subject.uploadMedia(multipartFile, TEST_USER_EMAIL));

    assertEquals("File cannot be null or empty", exception.getMessage());
    verify(userService, never()).findByEmail(any());
    verify(bucketComponent, never()).upload(any(), any());
    verify(mediaRepository, never()).save(any());
  }

  @Test
  void upload_media_should_throw_exception_when_file_size_is_zero() {
    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getSize()).thenReturn(0L);
    when(multipartFile.getOriginalFilename()).thenReturn(TEST_FILENAME);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> subject.uploadMedia(multipartFile, TEST_USER_EMAIL));

    assertEquals("File cannot have zero size", exception.getMessage());
    verify(userService, never()).findByEmail(any());
    verify(bucketComponent, never()).upload(any(), any());
    verify(mediaRepository, never()).save(any());
  }

  @Test
  void upload_media_should_throw_exception_when_file_exceeds_max_size() {
    long oversizedFile = 101L * 1024 * 1024; // 101MB
    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getSize()).thenReturn(oversizedFile);
    when(multipartFile.getOriginalFilename()).thenReturn(TEST_FILENAME);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> subject.uploadMedia(multipartFile, TEST_USER_EMAIL));

    assertTrue(exception.getMessage().contains("exceeds maximum allowed size"));
    verify(userService, never()).findByEmail(any());
    verify(bucketComponent, never()).upload(any(), any());
    verify(mediaRepository, never()).save(any());
  }

  @Test
  void upload_media_should_call_user_service_with_correct_email() throws Exception {
    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getSize()).thenReturn(TEST_FILE_SIZE_BYTES);
    when(multipartFile.getOriginalFilename()).thenReturn(TEST_FILENAME);
    when(multipartFile.getBytes()).thenReturn(new byte[100]);

    when(userService.findByEmail(TEST_USER_EMAIL)).thenReturn(testUser);
    when(mediaConverter.apply(multipartFile, testUser)).thenReturn(testMedia);
    when(mediaMapper.toPersistence(testMedia)).thenReturn(testJMedia);
    when(mediaRepository.save(testJMedia)).thenReturn(testJMedia);
    when(mediaMapper.toCoreModel(testJMedia)).thenReturn(testMedia);

    subject.uploadMedia(multipartFile, TEST_USER_EMAIL);

    ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
    verify(userService).findByEmail(emailCaptor.capture());
    assertEquals(TEST_USER_EMAIL, emailCaptor.getValue());
  }

  @Test
  void upload_media_should_upload_to_correct_bucket_key() throws Exception {
    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getSize()).thenReturn(TEST_FILE_SIZE_BYTES);
    when(multipartFile.getOriginalFilename()).thenReturn(TEST_FILENAME);
    when(multipartFile.getBytes()).thenReturn(new byte[100]);

    when(userService.findByEmail(TEST_USER_EMAIL)).thenReturn(testUser);
    when(mediaConverter.apply(multipartFile, testUser)).thenReturn(testMedia);
    when(mediaMapper.toPersistence(testMedia)).thenReturn(testJMedia);
    when(mediaRepository.save(testJMedia)).thenReturn(testJMedia);
    when(mediaMapper.toCoreModel(testJMedia)).thenReturn(testMedia);

    subject.uploadMedia(multipartFile, TEST_USER_EMAIL);

    ArgumentCaptor<String> bucketKeyCaptor = ArgumentCaptor.forClass(String.class);
    verify(bucketComponent).upload(any(File.class), bucketKeyCaptor.capture());
    assertEquals(TEST_BUCKET_KEY, bucketKeyCaptor.getValue());
  }

  @Test
  void upload_media_should_save_jmedia_to_repository() throws Exception {
    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getSize()).thenReturn(TEST_FILE_SIZE_BYTES);
    when(multipartFile.getOriginalFilename()).thenReturn(TEST_FILENAME);
    when(multipartFile.getBytes()).thenReturn(new byte[100]);

    when(userService.findByEmail(TEST_USER_EMAIL)).thenReturn(testUser);
    when(mediaConverter.apply(multipartFile, testUser)).thenReturn(testMedia);
    when(mediaMapper.toPersistence(testMedia)).thenReturn(testJMedia);
    when(mediaRepository.save(testJMedia)).thenReturn(testJMedia);
    when(mediaMapper.toCoreModel(testJMedia)).thenReturn(testMedia);

    subject.uploadMedia(multipartFile, TEST_USER_EMAIL);

    ArgumentCaptor<JMedia> jMediaCaptor = ArgumentCaptor.forClass(JMedia.class);
    verify(mediaRepository).save(jMediaCaptor.capture());
    assertEquals(TEST_MEDIA_ID, jMediaCaptor.getValue().getId());
    assertEquals(TEST_FILENAME, jMediaCaptor.getValue().getFileName());
  }

  @Test
  void upload_media_should_return_core_model_not_persistence_model() throws Exception {
    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getSize()).thenReturn(TEST_FILE_SIZE_BYTES);
    when(multipartFile.getOriginalFilename()).thenReturn(TEST_FILENAME);
    when(multipartFile.getBytes()).thenReturn(new byte[100]);

    when(userService.findByEmail(TEST_USER_EMAIL)).thenReturn(testUser);
    when(mediaConverter.apply(multipartFile, testUser)).thenReturn(testMedia);
    when(mediaMapper.toPersistence(testMedia)).thenReturn(testJMedia);
    when(mediaRepository.save(testJMedia)).thenReturn(testJMedia);
    when(mediaMapper.toCoreModel(testJMedia)).thenReturn(testMedia);

    Media result = subject.uploadMedia(multipartFile, TEST_USER_EMAIL);

    assertNotNull(result);
    verify(mediaMapper).toCoreModel(testJMedia);
  }
}
