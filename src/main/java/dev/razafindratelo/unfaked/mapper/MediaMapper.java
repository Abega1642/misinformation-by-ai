package dev.razafindratelo.unfaked.mapper;

import dev.razafindratelo.unfaked.model.Media;
import dev.razafindratelo.unfaked.repository.model.JMedia;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MediaMapper {
  Media toCoreModel(JMedia jMedia);

  JMedia toPersistence(Media media);

  List<Media> toCoreModel(List<JMedia> jMedias);

  List<JMedia> toPersistence(List<Media> medias);
}
