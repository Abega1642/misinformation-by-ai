package dev.razafindratelo.misinformation.mapper;

import dev.razafindratelo.misinformation.model.Audio;
import dev.razafindratelo.misinformation.repository.model.JAudio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AudioMapper {
  private final UserMapper userMapper;

  public Audio toCoreModel(JAudio jAudio) {
    return Audio.builder()
        .id(jAudio.getId())
        .fileName(jAudio.getFileName())
        .bucketKey(jAudio.getBucketKey())
        .size(jAudio.getSize())
        .sizeType(jAudio.getSizeType())
        .fileType(jAudio.getFileType())
        .createdAt(jAudio.getCreatedAt())
        .owner(userMapper.toCoreModel(jAudio.getOwner()))
        .duration(jAudio.getDuration())
        .bitRate(jAudio.getBitRate())
        .sampleRate(jAudio.getSampleRate())
        .channels(jAudio.getChannels())
        .codec(jAudio.getCodec())
        .format(jAudio.getFormat())
        .build();
  }

  public JAudio toPersistenceModel(Audio audio) {
    return JAudio.builder()
        .id(audio.getId())
        .fileName(audio.getFileName())
        .owner(userMapper.toPersistenceModel(audio.getOwner()))
        .size(audio.getSize())
        .sizeType(audio.getSizeType())
        .bucketKey(audio.getBucketKey())
        .fileType(audio.getFileType())
        .createdAt(audio.getCreatedAt())
        .duration(audio.getDuration())
        .bitRate(audio.getBitRate())
        .sampleRate(audio.getSampleRate())
        .channels(audio.getChannels())
        .codec(audio.getCodec())
        .format(audio.getFormat())
        .build();
  }
}
