package dev.razafindratelo.misinformation.mapper;

import dev.razafindratelo.misinformation.model.Token;
import dev.razafindratelo.misinformation.repository.model.JToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenMapper {
  private final UserMapper userMapper;

  public Token toCoreModel(JToken jToken) {
    return new Token(
        jToken.getId(),
        userMapper.toCoreModel(jToken.getOwner()),
        jToken.getToken(),
        jToken.getExpirationDate());
  }

  public JToken toPersistenceModel(Token token) {
    return new JToken(
        token.id(),
        userMapper.toPersistenceModel(token.owner()),
        token.token(),
        token.expirationDate());
  }
}
