package dev.razafindratelo.misinformation.service.media;

import static org.junit.jupiter.api.Assertions.*;

import dev.razafindratelo.misinformation.model.classifier.FileType;
import dev.razafindratelo.misinformation.model.classifier.ImageFormat;
import java.io.File;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ImageMetaDataExtractorTest {
  private static final String PREFIX = "/images/";
  private static final String IMG_PNG = "test-image-1.png";
  private static final String IMG_JPG = "test-image-2.jpg";
  private static final String NON_EXISTENT_PATH = "/non/existent/image.png";

  private ImageMetaDataExtractor subject;

  @BeforeEach
  void setUp() {
    subject = new ImageMetaDataExtractor();
  }

  @Test
  void should_be_able_to_detect_format_png_from_an_image() throws URISyntaxException {
    var resource = getClass().getResource(PREFIX + IMG_PNG);
    assertNotNull(resource);

    File image = new File(resource.toURI());
    System.out.println("file: " + image.getAbsolutePath());
    var actual = subject.apply(image);

    assertNotNull(actual);
    System.out.println(actual.getFormat());
    assertEquals(ImageFormat.PNG, actual.getFormat());
  }

  @Test
  void should_be_able_to_detect_format_jpeg_from_an_image() throws URISyntaxException {
    var resource = getClass().getResource(PREFIX + IMG_JPG);
    assertNotNull(resource);

    File image = new File(resource.toURI());
    var actual = subject.apply(image);

    assertNotNull(actual);
    assertSame(ImageFormat.JPEG, actual.getFormat());
  }

  @Test
  void should_extract_basic_image_metadata() throws URISyntaxException {
    var resource = getClass().getResource(PREFIX + IMG_PNG);
    assertNotNull(resource);

    File image = new File(resource.toURI());
    var actual = subject.apply(image);

    assertNotNull(actual);
    assertEquals(FileType.IMAGE, actual.getFileType());
    assertNotNull(actual.getFileName());
    assertTrue(actual.getSize() > 0);
    assertTrue(actual.getWidth() > 0);
    assertTrue(actual.getHeight() > 0);
  }

  @Test
  void should_extract_color_model_and_bit_depth() throws URISyntaxException {
    var resource = getClass().getResource(PREFIX + IMG_PNG);
    assertNotNull(resource);

    File image = new File(resource.toURI());
    var actual = subject.apply(image);

    assertNotNull(actual);
    assertNotNull(actual.getColorModel());
    assertTrue(actual.getBitDepth() > 0);
  }

  @Test
  void should_extract_dimensions_correctly() throws URISyntaxException {
    var resource = getClass().getResource(PREFIX + IMG_JPG);
    assertNotNull(resource);

    File image = new File(resource.toURI());
    var actual = subject.apply(image);

    assertNotNull(actual);
    assertTrue(actual.getWidth() > 0);
    assertTrue(actual.getHeight() > 0);
    assertTrue(actual.getBitDepth() > 0);
  }

  @Test
  void should_throw_when_file_not_exists() {
    File missingFile = new File(NON_EXISTENT_PATH);
    assertThrows(UncheckedIOException.class, () -> subject.apply(missingFile));
  }

  @Test
  void should_generate_unique_id_for_each_image() throws URISyntaxException {
    var resource = getClass().getResource(PREFIX + IMG_PNG);
    assertNotNull(resource);

    File image = new File(resource.toURI());
    var actual1 = subject.apply(image);
    var actual2 = subject.apply(image);

    assertNotNull(actual1.getId());
    assertNotNull(actual2.getId());
    assertNotEquals(actual1.getId(), actual2.getId());
  }

  @Test
  void should_set_created_at_timestamp() throws URISyntaxException {
    var resource = getClass().getResource(PREFIX + IMG_PNG);
    assertNotNull(resource);

    File image = new File(resource.toURI());
    var actual = subject.apply(image);

    assertNotNull(actual);
    assertNotNull(actual.getCreatedAt());
  }
}
