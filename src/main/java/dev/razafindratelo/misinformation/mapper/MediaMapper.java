package dev.razafindratelo.misinformation.mapper;

import dev.razafindratelo.misinformation.model.Media;
import dev.razafindratelo.misinformation.repository.model.JMedia;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MediaMapper {
  Media toCoreModel(JMedia jMedia);

  JMedia toPersistence(Media media);

  List<Media> toCoreModel(List<JMedia> jMedias);

  List<JMedia> toPersistence(List<Media> medias);
}
