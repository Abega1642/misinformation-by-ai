package dev.razafindratelo.misinformation.mapper;

import dev.razafindratelo.misinformation.model.Video;
import dev.razafindratelo.misinformation.repository.model.JVideo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VideoMapper {
  private final UserMapper userMapper;

  public Video toCoreModel(JVideo jVideo) {
    return Video.builder()
        .id(jVideo.getId())
        .fileName(jVideo.getFileName())
        .bucketKey(jVideo.getBucketKey())
        .size(jVideo.getSize())
        .sizeType(jVideo.getSizeType())
        .fileType(jVideo.getFileType())
        .createdAt(jVideo.getCreatedAt())
        .owner(userMapper.toCoreModel(jVideo.getOwner()))
        .duration(jVideo.getDuration())
        .codec(jVideo.getCodec())
        .width(jVideo.getWidth())
        .height(jVideo.getHeight())
        .frameRate(jVideo.getFrameRate())
        .aspectRatio(jVideo.getAspectRatio())
        .containerFormat(jVideo.getContainerFormat())
        .bitRate(jVideo.getBitRate())
        .audioCodec(jVideo.getAudioCodec())
        .audioChannels(jVideo.getAudioChannels())
        .audioSampleRate(jVideo.getAudioSampleRate())
        .build();
  }

  public JVideo toPersistenceModel(Video video) {
    return JVideo.builder()
        .id(video.getId())
        .fileName(video.getFileName())
        .owner(userMapper.toPersistenceModel(video.getOwner()))
        .size(video.getSize())
        .sizeType(video.getSizeType())
        .bucketKey(video.getBucketKey())
        .fileType(video.getFileType())
        .createdAt(video.getCreatedAt())
        .duration(video.getDuration())
        .codec(video.getCodec())
        .width(video.getWidth())
        .height(video.getHeight())
        .frameRate(video.getFrameRate())
        .aspectRatio(video.getAspectRatio())
        .containerFormat(video.getContainerFormat())
        .bitRate(video.getBitRate())
        .audioCodec(video.getAudioCodec())
        .audioChannels(video.getAudioChannels())
        .audioSampleRate(video.getAudioSampleRate())
        .build();
  }
}
