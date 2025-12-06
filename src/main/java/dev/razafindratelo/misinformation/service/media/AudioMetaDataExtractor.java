package dev.razafindratelo.misinformation.service.media;

import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;

import dev.razafindratelo.misinformation.model.Audio;
import dev.razafindratelo.misinformation.model.classifier.AudioCodec;
import dev.razafindratelo.misinformation.model.classifier.ContainerFormat;
import dev.razafindratelo.misinformation.model.classifier.FileType;
import dev.razafindratelo.misinformation.model.classifier.SizeType;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class AudioMetaDataExtractor implements MediaMetadataExtractor<Audio> {
  private FFprobe ffprobe;

  @Override
  public Audio apply(File file) {
    try {
      FFmpegProbeResult probeResult = ffprobe.probe(file.getAbsolutePath());

      FFmpegStream audioStream = findStreamByType(probeResult);

      return Audio.builder()
          .id(randomUUID().toString())
          .fileName(file.getName())
          .fileType(FileType.AUDIO)
          .size(file.length())
          .sizeType(SizeType.BYTES)
          .createdAt(now())
          .duration(probeResult.getFormat().duration)
          .codec(extractAudioCodec(audioStream))
          .bitRate(extractBitRate(audioStream))
          .sampleRate(extractSampleRate(audioStream))
          .channels(extractChannels(audioStream))
          .format(extractContainerFormat(probeResult, audioStream))
          .build();

    } catch (IOException e) {
      log.error("Error extracting metadata from audio: {}", file.getAbsolutePath(), e);
      throw new UncheckedIOException(e);
    }
  }

  private FFmpegStream findStreamByType(FFmpegProbeResult probeResult) {
    return probeResult.getStreams().stream()
        .filter(s -> s.codec_type == FFmpegStream.CodecType.AUDIO)
        .findFirst()
        .orElse(null);
  }

  private AudioCodec extractAudioCodec(FFmpegStream audioStream) {
    return audioStream != null ? AudioCodec.fromString(audioStream.codec_name) : null;
  }

  private int extractBitRate(FFmpegStream audioStream) {
    return audioStream != null ? (int) audioStream.bit_rate : 0;
  }

  private int extractSampleRate(FFmpegStream audioStream) {
    return audioStream != null ? audioStream.sample_rate : 0;
  }

  private int extractChannels(FFmpegStream audioStream) {
    return audioStream != null ? audioStream.channels : 0;
  }

  private ContainerFormat extractContainerFormat(
      FFmpegProbeResult probeResult, FFmpegStream audioStream) {
    if (audioStream == null || probeResult.getFormat() == null) {
      return null;
    }
    return ContainerFormat.fromString(probeResult.getFormat().format_name);
  }
}
