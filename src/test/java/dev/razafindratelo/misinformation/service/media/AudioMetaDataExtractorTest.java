package dev.razafindratelo.misinformation.service.media;

import static org.junit.jupiter.api.Assertions.*;

import dev.razafindratelo.misinformation.model.classifier.ContainerFormat;
import dev.razafindratelo.misinformation.model.classifier.FileType;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import net.bramp.ffmpeg.FFprobe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AudioMetaDataExtractorTest {
  private static final String PREFIX = "/audios/";
  private static final String AUDIO_MP3 = "audio-test.mp3";
  private static final String NON_EXISTENT_PATH = "/non/existent/audio.mp3";

  private AudioMetaDataExtractor subject;

  @BeforeEach
  void setUp() throws IOException {
    try {
      var ffprobe = new FFprobe("/usr/bin/ffprobe");
      subject = new AudioMetaDataExtractor(ffprobe);
    } catch (IOException e) {
      throw new IOException(e);
    }
  }

  @Test
  void should_be_able_to_detect_format_mp3_from_audio() throws URISyntaxException {
    var resource = getClass().getResource(PREFIX + AUDIO_MP3);
    assertNotNull(resource);

    File audio = new File(resource.toURI());
    var actual = subject.apply(audio);

    assertNotNull(actual);
    assertSame(ContainerFormat.MP3, actual.getFormat());
  }

  @Test
  void should_extract_basic_audio_metadata() throws URISyntaxException {
    var resource = getClass().getResource(PREFIX + AUDIO_MP3);
    assertNotNull(resource);

    File audio = new File(resource.toURI());
    var actual = subject.apply(audio);

    assertNotNull(actual);
    assertEquals(FileType.AUDIO, actual.getFileType());
    assertNotNull(actual.getFileName());
    assertTrue(actual.getSize() > 0);
    assertTrue(actual.getDuration() > 0);
  }

  @Test
  void should_extract_audio_codec_and_channels() throws URISyntaxException {
    var resource = getClass().getResource(PREFIX + AUDIO_MP3);
    assertNotNull(resource);

    File audio = new File(resource.toURI());
    var actual = subject.apply(audio);

    assertNotNull(actual);
    assertNotNull(actual.getCodec());
    assertTrue(actual.getChannels() > 0);
    assertTrue(actual.getSampleRate() > 0);
  }

  @Test
  void should_throw_when_file_not_exists() {
    File missingFile = new File(NON_EXISTENT_PATH);
    assertThrows(UncheckedIOException.class, () -> subject.apply(missingFile));
  }

  @Test
  void should_generate_unique_id_for_each_audio() throws URISyntaxException {
    var resource = getClass().getResource(PREFIX + AUDIO_MP3);
    assertNotNull(resource);

    File audio = new File(resource.toURI());
    var actual1 = subject.apply(audio);
    var actual2 = subject.apply(audio);

    assertNotNull(actual1.getId());
    assertNotNull(actual2.getId());
    assertNotEquals(actual1.getId(), actual2.getId());
  }

  @Test
  void should_set_created_at_timestamp() throws URISyntaxException {
    var resource = getClass().getResource(PREFIX + AUDIO_MP3);
    assertNotNull(resource);

    File audio = new File(resource.toURI());
    var actual = subject.apply(audio);

    assertNotNull(actual);
    assertNotNull(actual.getCreatedAt());
  }
}
