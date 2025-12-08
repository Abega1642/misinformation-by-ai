package dev.razafindratelo.misinformation.mapper;

import dev.razafindratelo.misinformation.model.Token;
import dev.razafindratelo.misinformation.repository.model.JToken;
import org.mapstruct.Mapper;

@Mapper(
    componentModel = "spring",
    uses = {UserMapper.class})
public interface TokenMapper {

  Token toCoreModel(JToken jToken);

  JToken toPersistenceModel(Token token);
}
