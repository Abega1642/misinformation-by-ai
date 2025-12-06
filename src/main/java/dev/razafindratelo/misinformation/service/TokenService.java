package dev.razafindratelo.misinformation.service;

import static java.time.LocalDate.now;
import static java.util.UUID.randomUUID;

import dev.razafindratelo.misinformation.endpoint.rest.controller.model.TokenRequest;
import dev.razafindratelo.misinformation.mapper.TokenMapper;
import dev.razafindratelo.misinformation.model.Token;
import dev.razafindratelo.misinformation.repository.TokenRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {
  private final UserService userService;
  private final TokenMapper tokenMapper;
  private final TokenRepository tokenRepository;

  public Token saveUserToken(@NotNull TokenRequest tokenRequest) {
    var id = randomUUID().toString();
    var owner = userService.findByClerkId(tokenRequest.clerkId());

    log.info("Saving token of user with email={}", owner.email());

    var tokenRequested = new Token(id, owner, tokenRequest.token(), tokenRequest.expirationDate());

    var savedToken = tokenRepository.save(tokenMapper.toPersistenceModel(tokenRequested));

    return tokenMapper.toCoreModel(savedToken);
  }

  public boolean isValid(@NotNull @NotBlank String tokenValue, String email) {
    var token =
        tokenRepository
            .findByToken(tokenValue)
            .orElseThrow(() -> new EntityNotFoundException("No Token of value " + tokenValue));
    var owner = userService.findByClerkId(email);

    log.info(
        "Checking if token is valid for token={} and userEmail={}",
        token.getToken(),
        owner.email());

    return token.getExpirationDate().isAfter(now());
  }
}
