package dev.razafindratelo.misinformation.manager;

import static dev.razafindratelo.misinformation.model.classifier.AuthType.OAUTH2;
import static dev.razafindratelo.misinformation.model.classifier.AuthType.PASSWORD;

import dev.razafindratelo.misinformation.endpoint.rest.controller.model.AuthRequest;
import dev.razafindratelo.misinformation.exception.AccountDeletedException;
import dev.razafindratelo.misinformation.exception.AccountInactiveException;
import dev.razafindratelo.misinformation.exception.AccountSuspendedException;
import dev.razafindratelo.misinformation.model.User;
import dev.razafindratelo.misinformation.model.classifier.UserStatus;
import dev.razafindratelo.misinformation.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Slf4j
@Validated
@RequiredArgsConstructor
public class AuthManager {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;

  public User authenticate(@Valid @NotNull AuthRequest authRequest) {
    log.info(
        "Starting user auth - email={}, auth_type={}",
        authRequest.email(),
        authRequest.password() != null ? PASSWORD : OAUTH2);

    try {
      User user;

      if (authRequest.password() != null && !authRequest.password().isBlank())
        user = authenticateWithPassword(authRequest.email(), authRequest.password());
      else if (authRequest.clerkId() != null && !authRequest.clerkId().isBlank())
        user = authenticateWithClerk(authRequest.email(), authRequest.clerkId());
      else throw new AuthorizationDeniedException("Auth requires either password or clerk_id");

      log.info(
          "Successfully authenticated user - id={}, email={}, auth_type={}",
          user.getId(),
          user.getEmail(),
          user.getPassword() != null ? PASSWORD : OAUTH2);

      return user;

    } catch (EntityNotFoundException e) {
      log.warn("Auth failed - user not found: email={}", authRequest.email());
      throw new AuthorizationDeniedException("Invalid credentials: " + e);
    } catch (AuthorizationDeniedException e) {
      log.warn("Auth failed - invalid request: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Auth error - email={}", authRequest.email(), e);
      throw new AuthorizationDeniedException("Auth failed. Please try again. " + e);
    }
  }

  private User authenticateWithPassword(
      @Email @NotNull @NotBlank String email,
      @NotNull
          @NotBlank
          @Length(min = 8, message = "Password should be equal or longer than 8 characters")
          String password) {

    User user = userService.findByEmail(email);

    if (user.getPassword() == null || user.getPassword().isBlank()) {
      log.warn("Password auth attempted on OAuth2-only account - email={}", email);
      throw new AuthorizationDeniedException(
          "This account uses social login. Please sign in with your connected account.");
    }

    if (!passwordEncoder.matches(password, user.getPassword())) {
      log.warn("Invalid password attempt - email={}", email);
      throw new AuthorizationDeniedException("Invalid credentials");
    }

    validateUserStatus(user);

    return user;
  }

  private User authenticateWithClerk(String email, String clerkId) {
    User user = userService.authenticateWithClerkId(email, clerkId);

    validateUserStatus(user);

    return user;
  }

  private void validateUserStatus(User user) {
    if (user.getStatus() == UserStatus.SUSPENDED) {
      log.warn("Suspended account login attempt - email={}", user.getEmail());
      throw new AccountSuspendedException(
          "Your account has been suspended. Please contact support.");
    }

    if (user.getStatus() == UserStatus.DELETED) {
      log.warn("Deleted account login attempt - email={}", user.getEmail());
      throw new AccountDeletedException("This account no longer exists.");
    }

    if (user.getStatus() == UserStatus.INACTIVE) {
      log.warn("Inactive account login attempt - email={}", user.getEmail());
      throw new AccountInactiveException(
          "Your account is inactive. Please verify your email or contact support.");
    }
  }
}
