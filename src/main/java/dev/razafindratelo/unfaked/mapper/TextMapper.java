package dev.razafindratelo.unfaked.mapper;

import dev.razafindratelo.unfaked.model.Text;
import dev.razafindratelo.unfaked.repository.model.JText;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(
    componentModel = "spring",
    uses = {UserMapper.class})
public interface TextMapper {

  Text toCoreModel(JText jText);

  JText toPersistenceModel(Text text);

  List<Text> toCoreModel(List<JText> jTexts);

  List<JText> toPersistenceModel(List<Text> texts);
}
