package dev.razafindratelo.misinformation.mapper;

import dev.razafindratelo.misinformation.model.Text;
import dev.razafindratelo.misinformation.repository.model.JText;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TextMapper {
  private final UserMapper userMapper;

  public Text toCoreModel(JText jText) {
    return new Text(
        jText.getId(),
        userMapper.toCoreModel(jText.getOwner()),
        jText.getValue(),
        jText.getCreatedAt());
  }

  public JText toPersistenceModel(Text text) {
    return new JText(
        text.id(), userMapper.toPersistenceModel(text.owner()), text.value(), text.createdAt());
  }
}
