package dev.razafindratelo.misinformation.service.media;

import dev.razafindratelo.misinformation.model.Video;
import dev.razafindratelo.misinformation.model.classifier.AudioCodec;
import dev.razafindratelo.misinformation.model.classifier.ContainerFormat;
import dev.razafindratelo.misinformation.model.classifier.FileType;
import dev.razafindratelo.misinformation.model.classifier.SizeType;
import dev.razafindratelo.misinformation.model.classifier.VideoCodec;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class VideoMetaDataExtractor implements MediaMetadataExtractor<Video> {
  private FFprobe ffprobe;

  @Override
  public Video apply(File file) {
    try {
      FFmpegProbeResult probeResult = ffprobe.probe(file.getAbsolutePath());

      FFmpegStream videoStream = findStreamByType(probeResult, FFmpegStream.CodecType.VIDEO);
      FFmpegStream audioStream = findStreamByType(probeResult, FFmpegStream.CodecType.AUDIO);

      return Video.builder()
          .id(UUID.randomUUID().toString())
          .fileName(file.getName())
          .fileType(FileType.VIDEO)
          .size(file.length())
          .sizeType(SizeType.BYTES)
          .createdAt(LocalDateTime.now())
          .duration(probeResult.getFormat().duration)
          .codec(extractVideoCodec(videoStream))
          .width(extractWidth(videoStream))
          .height(extractHeight(videoStream))
          .frameRate(extractFrameRate(videoStream))
          .aspectRatio(extractAspectRatio(videoStream))
          .bitRate(extractBitRate(videoStream))
          .containerFormat(extractContainerFormat(probeResult, videoStream))
          .audioCodec(extractAudioCodec(audioStream))
          .audioChannels(extractAudioChannels(audioStream))
          .audioSampleRate(extractAudioSampleRate(audioStream))
          .build();

    } catch (IOException e) {
      log.error("Error extracting metadata from video: {}", file.getAbsolutePath(), e);
      throw new UncheckedIOException(e);
    }
  }

  private FFmpegStream findStreamByType(
      FFmpegProbeResult probeResult, FFmpegStream.CodecType type) {
    return probeResult.getStreams().stream()
        .filter(s -> s.codec_type == type)
        .findFirst()
        .orElse(null);
  }

  private VideoCodec extractVideoCodec(FFmpegStream videoStream) {
    return videoStream != null ? VideoCodec.fromString(videoStream.codec_name) : null;
  }

  private int extractWidth(FFmpegStream videoStream) {
    return videoStream != null ? videoStream.width : 0;
  }

  private int extractHeight(FFmpegStream videoStream) {
    return videoStream != null ? videoStream.height : 0;
  }

  private double extractFrameRate(FFmpegStream videoStream) {
    if (videoStream == null || videoStream.avg_frame_rate == null) return 0.0;

    int denominator = videoStream.avg_frame_rate.getDenominator();

    if (denominator == 0) return 0.0;

    return videoStream.avg_frame_rate.getNumerator() / (double) denominator;
  }

  private String extractAspectRatio(FFmpegStream videoStream) {
    return videoStream != null ? videoStream.display_aspect_ratio : null;
  }

  private long extractBitRate(FFmpegStream videoStream) {
    return videoStream != null ? videoStream.bit_rate : 0L;
  }

  private ContainerFormat extractContainerFormat(
      FFmpegProbeResult probeResult, FFmpegStream videoStream) {
    if (videoStream == null || probeResult.getFormat() == null) {
      return null;
    }
    return ContainerFormat.fromString(probeResult.getFormat().format_name);
  }

  private AudioCodec extractAudioCodec(FFmpegStream audioStream) {
    return audioStream != null ? AudioCodec.fromString(audioStream.codec_name) : null;
  }

  private int extractAudioChannels(FFmpegStream audioStream) {
    return audioStream != null ? audioStream.channels : 0;
  }

  private int extractAudioSampleRate(FFmpegStream audioStream) {
    return audioStream != null ? audioStream.sample_rate : 0;
  }
}
