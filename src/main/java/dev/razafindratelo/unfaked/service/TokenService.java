package dev.razafindratelo.unfaked.service;

import static java.time.LocalDate.now;
import static java.util.UUID.randomUUID;

import dev.razafindratelo.unfaked.endpoint.rest.controller.model.TokenRequest;
import dev.razafindratelo.unfaked.mapper.TokenMapper;
import dev.razafindratelo.unfaked.model.Token;
import dev.razafindratelo.unfaked.repository.TokenRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Slf4j
@Validated
@RequiredArgsConstructor
public class TokenService {
  private final UserService userService;
  private final TokenMapper tokenMapper;
  private final TokenRepository tokenRepository;

  public Token saveUserToken(@NotNull @Valid TokenRequest tokenRequest) {
    var id = randomUUID().toString();
    var owner = userService.findByClerkId(tokenRequest.clerkId());

    log.info("Saving token of user with email={}", owner.getEmail());

    var tokenRequested = new Token(id, owner, tokenRequest.token(), tokenRequest.expirationDate());

    var savedToken = tokenRepository.save(tokenMapper.toPersistenceModel(tokenRequested));

    return tokenMapper.toCoreModel(savedToken);
  }

  public Token findTokenByValue(@NotNull @NotBlank String value) {
    var token =
        tokenRepository
            .findByToken(value)
            .orElseThrow(() -> new EntityNotFoundException("No Token of value " + value));

    return tokenMapper.toCoreModel(token);
  }

  public boolean isValid(@NotNull @NotBlank String tokenValue) {
    var token =
        tokenRepository
            .findByToken(tokenValue)
            .orElseThrow(() -> new EntityNotFoundException("No Token of value " + tokenValue));
    var owner = token.getOwner();

    log.info("Checking if token is valid for userEmail={}", owner.getEmail());

    return token.getExpirationDate().isAfter(now());
  }
}
