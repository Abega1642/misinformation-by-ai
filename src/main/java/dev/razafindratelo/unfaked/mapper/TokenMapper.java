package dev.razafindratelo.unfaked.mapper;

import dev.razafindratelo.unfaked.model.Token;
import dev.razafindratelo.unfaked.repository.model.JToken;
import org.mapstruct.Mapper;

@Mapper(
    componentModel = "spring",
    uses = {UserMapper.class})
public interface TokenMapper {

  Token toCoreModel(JToken jToken);

  JToken toPersistenceModel(Token token);
}
