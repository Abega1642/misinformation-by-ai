package dev.razafindratelo.misinformation.manager;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;

import dev.razafindratelo.misinformation.endpoint.rest.controller.model.LoginRequest;
import dev.razafindratelo.misinformation.endpoint.rest.controller.model.LoginResponse;
import dev.razafindratelo.misinformation.endpoint.rest.controller.model.TokenRequest;
import dev.razafindratelo.misinformation.model.Token;
import dev.razafindratelo.misinformation.service.TokenService;
import dev.razafindratelo.misinformation.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@AllArgsConstructor
public class AuthManager {
  private final UserService userService;
  private final TokenService tokenService;

  public LoginResponse authenticate(LoginRequest loginRequest) {
    var requestTime = now();
    var authenticatedUser =
        userService.authenticateUser(loginRequest.clerkId(), loginRequest.email());
    return new LoginResponse(
        format(
            "User with email=%s authenticated successfully at date=%s",
            authenticatedUser.email(), now()),
        authenticatedUser.email(),
        requestTime,
        authenticatedUser);
  }

  public Token saveUserToken(@NotNull @Valid TokenRequest tokenRequest) {
    return tokenService.saveUserToken(tokenRequest);
  }
}
