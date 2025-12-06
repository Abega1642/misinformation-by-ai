package dev.razafindratelo.misinformation.mapper;

import dev.razafindratelo.misinformation.model.Image;
import dev.razafindratelo.misinformation.repository.model.JImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImageMapper {
  private final UserMapper userMapper;

  public Image toCoreModel(JImage jImage) {
    return Image.builder()
        .id(jImage.getId())
        .fileName(jImage.getFileName())
        .bucketKey(jImage.getBucketKey())
        .size(jImage.getSize())
        .sizeType(jImage.getSizeType())
        .fileType(jImage.getFileType())
        .createdAt(jImage.getCreatedAt())
        .owner(userMapper.toCoreModel(jImage.getOwner()))
        .width(jImage.getWidth())
        .height(jImage.getHeight())
        .format(jImage.getFormat())
        .bitDepth(jImage.getBitDepth())
        .colorModel(jImage.getColorModel())
        .dpi(jImage.getDpi())
        .iso(jImage.getIso())
        .build();
  }

  public JImage toPersistenceModel(Image image) {
    return JImage.builder()
        .id(image.getId())
        .fileName(image.getFileName())
        .owner(userMapper.toPersistenceModel(image.getOwner()))
        .size(image.getSize())
        .sizeType(image.getSizeType())
        .bucketKey(image.getBucketKey())
        .fileType(image.getFileType())
        .createdAt(image.getCreatedAt())
        .width(image.getWidth())
        .height(image.getHeight())
        .format(image.getFormat())
        .bitDepth(image.getBitDepth())
        .colorModel(image.getColorModel())
        .dpi(image.getDpi())
        .iso(image.getIso())
        .build();
  }
}
