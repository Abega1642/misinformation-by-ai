package dev.razafindratelo.misinformation.service;

import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.razafindratelo.misinformation.exception.MediaUploadException;
import dev.razafindratelo.misinformation.file.BucketComponent;
import dev.razafindratelo.misinformation.mapper.ImageMapper;
import dev.razafindratelo.misinformation.mapper.VideoMapper;
import dev.razafindratelo.misinformation.model.Image;
import dev.razafindratelo.misinformation.model.User;
import dev.razafindratelo.misinformation.model.Video;
import dev.razafindratelo.misinformation.model.classifier.ContainerFormat;
import dev.razafindratelo.misinformation.model.classifier.FileType;
import dev.razafindratelo.misinformation.model.classifier.ImageFormat;
import dev.razafindratelo.misinformation.model.classifier.SizeType;
import dev.razafindratelo.misinformation.model.classifier.VideoCodec;
import dev.razafindratelo.misinformation.repository.ImageRepository;
import dev.razafindratelo.misinformation.repository.VideoRepository;
import dev.razafindratelo.misinformation.repository.model.JImage;
import dev.razafindratelo.misinformation.repository.model.JVideo;
import dev.razafindratelo.misinformation.service.media.ImageMetaDataExtractor;
import dev.razafindratelo.misinformation.service.media.MultipartFileConverter;
import dev.razafindratelo.misinformation.service.media.VideoMetaDataExtractor;
import java.io.File;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@ExtendWith(MockitoExtension.class)
class MediaServiceIT {

  private static final String TEST_EMAIL = "rakoto@gmail.com";
  private static final String VIDEO_PREFIX = "videos/";
  private static final String FAILED_TO_UPLOAD_VIDEO_MESSAGE = "Failed to upload video";

  private static final String IMAGE_PREFIX = "images/";
  private static final String FAILED_TO_UPLOAD_IMAGE_MESSAGE = "Failed to upload image";

  @Mock private ImageMetaDataExtractor imageExtractor;
  @Mock private ImageRepository imageRepository;
  @Mock private ImageMapper imageMapper;
  @Mock private BucketComponent bucket;
  @Mock private VideoMetaDataExtractor videoExtractor;
  @Mock private VideoRepository videoRepository;
  @Mock private UserService userService;
  @Mock private MultipartFileConverter fileConverter;
  @Mock private VideoMapper videoMapper;
  @InjectMocks private MediaService mediaService;

  @Test
  void should_upload_video_successfully() throws IOException {
    var multipartFile = mock(MultipartFile.class);
    var videoFile = mock(File.class);
    var activeUser = createActiveUser();
    var video = createTestVideo(videoFile);
    var jVideo = mock(JVideo.class);

    when(userService.findByEmail(TEST_EMAIL)).thenReturn(activeUser);
    when(fileConverter.convert(multipartFile)).thenReturn(videoFile);
    when(videoExtractor.apply(videoFile)).thenReturn(video);
    when(videoMapper.toPersistenceModel(any(Video.class))).thenReturn(jVideo);

    var result = mediaService.uploadVideo(multipartFile, TEST_EMAIL);

    assertThat(result).isNotNull();
    assertThat(result.getOwner()).isEqualTo(activeUser);
    assertThat(result.getBucketKey()).isNotNull();
    assertThat(result.getBucketKey()).startsWith(VIDEO_PREFIX);

    verify(userService).findByEmail(TEST_EMAIL);
    verify(fileConverter).convert(multipartFile);
    verify(videoExtractor).apply(videoFile);
    verify(bucket).upload(eq(videoFile), startsWith(VIDEO_PREFIX));
    verify(videoMapper).toPersistenceModel(any(Video.class));
    verify(videoRepository).save(jVideo);
  }

  @Test
  void should_set_correct_owner_and_file_path_on_video() throws IOException {
    var multipartFile = mock(MultipartFile.class);
    var videoFile = mock(File.class);
    var activeUser = createActiveUser();
    var video = createTestVideo(videoFile);
    var jVideo = mock(JVideo.class);

    when(userService.findByEmail(TEST_EMAIL)).thenReturn(activeUser);
    when(fileConverter.convert(multipartFile)).thenReturn(videoFile);
    when(videoExtractor.apply(videoFile)).thenReturn(video);
    when(videoMapper.toPersistenceModel(any(Video.class))).thenReturn(jVideo);

    var result = mediaService.uploadVideo(multipartFile, TEST_EMAIL);

    assertThat(result.getOwner()).isEqualTo(activeUser);
    assertThat(result.getBucketKey()).matches("videos/[a-f0-9-]{36}");
  }

  @Test
  void should_throw_exception_when_file_conversion_fails() throws IOException {
    var multipartFile = mock(MultipartFile.class);
    var activeUser = createActiveUser();
    var conversionException = new IOException("File conversion failed");

    when(userService.findByEmail(TEST_EMAIL)).thenReturn(activeUser);
    when(fileConverter.convert(multipartFile)).thenThrow(conversionException);

    assertThatThrownBy(() -> mediaService.uploadVideo(multipartFile, TEST_EMAIL))
        .isInstanceOf(MediaUploadException.class)
        .hasMessageContaining(FAILED_TO_UPLOAD_VIDEO_MESSAGE);

    verify(bucket, never()).upload(any(), any());
    verify(videoMapper, never()).toPersistenceModel(any());
    verify(videoRepository, never()).save(any());
  }

  @Test
  void should_generate_unique_bucket_keys_for_multiple_uploads() throws IOException {
    var multipartFile1 = mock(MultipartFile.class);
    var multipartFile2 = mock(MultipartFile.class);
    var videoFile1 = mock(File.class);
    var videoFile2 = mock(File.class);
    var activeUser = createActiveUser();
    var video1 = createTestVideo(videoFile1);
    var video2 = createTestVideo(videoFile2);
    var jVideo = mock(JVideo.class);

    when(userService.findByEmail(TEST_EMAIL)).thenReturn(activeUser);
    when(fileConverter.convert(multipartFile1)).thenReturn(videoFile1);
    when(fileConverter.convert(multipartFile2)).thenReturn(videoFile2);
    when(videoExtractor.apply(videoFile1)).thenReturn(video1);
    when(videoExtractor.apply(videoFile2)).thenReturn(video2);
    when(videoMapper.toPersistenceModel(any(Video.class))).thenReturn(jVideo);

    Video result1 = mediaService.uploadVideo(multipartFile1, TEST_EMAIL);
    Video result2 = mediaService.uploadVideo(multipartFile2, TEST_EMAIL);

    assertThat(result1.getBucketKey()).isNotEqualTo(result2.getBucketKey());
    assertThat(result1.getBucketKey()).startsWith(VIDEO_PREFIX);
    assertThat(result2.getBucketKey()).startsWith(VIDEO_PREFIX);
  }

  @Test
  void should_save_video_with_correct_mapping() throws IOException {
    var multipartFile = mock(MultipartFile.class);
    var videoFile = mock(File.class);
    var activeUser = createActiveUser();
    var video = createTestVideo(videoFile);
    var jVideo = mock(JVideo.class);

    when(userService.findByEmail(TEST_EMAIL)).thenReturn(activeUser);
    when(fileConverter.convert(multipartFile)).thenReturn(videoFile);
    when(videoExtractor.apply(videoFile)).thenReturn(video);
    when(videoMapper.toPersistenceModel(any(Video.class))).thenReturn(jVideo);

    mediaService.uploadVideo(multipartFile, TEST_EMAIL);

    ArgumentCaptor<JVideo> captor = ArgumentCaptor.forClass(JVideo.class);
    verify(videoRepository).save(captor.capture());

    var savedVideo = captor.getValue();
    assertThat(savedVideo).isNotNull();
    assertThat(savedVideo).isEqualTo(jVideo);
  }

  @Test
  void should_upload_image_successfully() throws IOException {
    var multipartFile = mock(MultipartFile.class);
    var imageFile = mock(File.class);
    var activeUser = createActiveUser();
    var image = createTestImage(imageFile);
    var jImage = mock(JImage.class);

    when(userService.findByEmail(TEST_EMAIL)).thenReturn(activeUser);
    when(fileConverter.convert(multipartFile)).thenReturn(imageFile);
    when(imageExtractor.apply(imageFile)).thenReturn(image);
    when(imageMapper.toPersistenceModel(any(Image.class))).thenReturn(jImage);

    var result = mediaService.uploadImage(multipartFile, TEST_EMAIL);

    assertThat(result).isNotNull();
    assertThat(result.getOwner()).isEqualTo(activeUser);
    assertThat(result.getBucketKey()).isNotNull();
    assertThat(result.getBucketKey()).startsWith(IMAGE_PREFIX);

    verify(userService).findByEmail(TEST_EMAIL);
    verify(fileConverter).convert(multipartFile);
    verify(imageExtractor).apply(imageFile);
    verify(bucket).upload(eq(imageFile), startsWith(IMAGE_PREFIX));
    verify(imageMapper).toPersistenceModel(any(Image.class));
    verify(imageRepository).save(jImage);
  }

  @Test
  void should_set_correct_owner_and_bucket_key_on_image() throws IOException {
    var multipartFile = mock(MultipartFile.class);
    var imageFile = mock(File.class);
    var activeUser = createActiveUser();
    var image = createTestImage(imageFile);
    var jImage = mock(JImage.class);

    when(userService.findByEmail(TEST_EMAIL)).thenReturn(activeUser);
    when(fileConverter.convert(multipartFile)).thenReturn(imageFile);
    when(imageExtractor.apply(imageFile)).thenReturn(image);
    when(imageMapper.toPersistenceModel(any(Image.class))).thenReturn(jImage);

    var result = mediaService.uploadImage(multipartFile, TEST_EMAIL);

    assertThat(result.getOwner()).isEqualTo(activeUser);
    assertThat(result.getBucketKey()).matches("images/[a-f0-9-]{36}");
  }

  @Test
  void should_throw_exception_when_image_conversion_fails() throws IOException {
    var multipartFile = mock(MultipartFile.class);
    var activeUser = createActiveUser();
    var conversionException = new IOException("File conversion failed");

    when(userService.findByEmail(TEST_EMAIL)).thenReturn(activeUser);
    when(fileConverter.convert(multipartFile)).thenThrow(conversionException);

    assertThatThrownBy(() -> mediaService.uploadImage(multipartFile, TEST_EMAIL))
        .isInstanceOf(MediaUploadException.class)
        .hasMessageContaining(FAILED_TO_UPLOAD_IMAGE_MESSAGE);

    verify(bucket, never()).upload(any(), any());
    verify(imageMapper, never()).toPersistenceModel(any());
    verify(imageRepository, never()).save(any());
  }

  @Test
  void should_generate_unique_bucket_keys_for_multiple_image_uploads() throws IOException {
    var multipartFile1 = mock(MultipartFile.class);
    var multipartFile2 = mock(MultipartFile.class);
    var imageFile1 = mock(File.class);
    var imageFile2 = mock(File.class);
    var activeUser = createActiveUser();
    var image1 = createTestImage(imageFile1);
    var image2 = createTestImage(imageFile2);
    var jImage = mock(JImage.class);

    when(userService.findByEmail(TEST_EMAIL)).thenReturn(activeUser);
    when(fileConverter.convert(multipartFile1)).thenReturn(imageFile1);
    when(fileConverter.convert(multipartFile2)).thenReturn(imageFile2);
    when(imageExtractor.apply(imageFile1)).thenReturn(image1);
    when(imageExtractor.apply(imageFile2)).thenReturn(image2);
    when(imageMapper.toPersistenceModel(any(Image.class))).thenReturn(jImage);

    Image result1 = mediaService.uploadImage(multipartFile1, TEST_EMAIL);
    Image result2 = mediaService.uploadImage(multipartFile2, TEST_EMAIL);

    assertThat(result1.getBucketKey()).isNotEqualTo(result2.getBucketKey());
    assertThat(result1.getBucketKey()).startsWith(IMAGE_PREFIX);
    assertThat(result2.getBucketKey()).startsWith(IMAGE_PREFIX);
  }

  @Test
  void should_save_image_with_correct_mapping() throws IOException {
    var multipartFile = mock(MultipartFile.class);
    var imageFile = mock(File.class);
    var activeUser = createActiveUser();
    var image = createTestImage(imageFile);
    var jImage = mock(JImage.class);

    when(userService.findByEmail(TEST_EMAIL)).thenReturn(activeUser);
    when(fileConverter.convert(multipartFile)).thenReturn(imageFile);
    when(imageExtractor.apply(imageFile)).thenReturn(image);
    when(imageMapper.toPersistenceModel(any(Image.class))).thenReturn(jImage);

    mediaService.uploadImage(multipartFile, TEST_EMAIL);

    ArgumentCaptor<JImage> captor = ArgumentCaptor.forClass(JImage.class);
    verify(imageRepository).save(captor.capture());

    var savedImage = captor.getValue();
    assertThat(savedImage).isNotNull();
    assertThat(savedImage).isEqualTo(jImage);
  }

  private Image createTestImage(File file) {
    return Image.builder()
        .id(randomUUID().toString())
        .fileName(file.getName())
        .fileType(FileType.IMAGE)
        .size(file.length())
        .sizeType(SizeType.BYTES)
        .createdAt(now())
        .width(1920)
        .height(1080)
        .format(ImageFormat.PNG)
        .bitDepth(24)
        .colorModel("DirectColorModel")
        .dpi(72.0)
        .iso(100)
        .build();
  }

  private User createActiveUser() {
    return new User(
        randomUUID().toString(), TEST_EMAIL, "random-full-name", randomUUID().toString(), now());
  }

  private Video createTestVideo(File file) {
    return Video.builder()
        .id(randomUUID().toString())
        .fileName(file.getName())
        .fileType(FileType.VIDEO)
        .size(file.length())
        .sizeType(SizeType.BYTES)
        .createdAt(now())
        .duration(120.0)
        .width(1920)
        .height(1080)
        .frameRate(30.0)
        .codec(VideoCodec.H264)
        .containerFormat(ContainerFormat.WEBM)
        .build();
  }
}
