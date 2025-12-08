package dev.razafindratelo.misinformation.manager;

import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.razafindratelo.misinformation.conf.FacadeIT;
import dev.razafindratelo.misinformation.endpoint.rest.controller.model.AuthRequest;
import dev.razafindratelo.misinformation.model.User;
import dev.razafindratelo.misinformation.model.classifier.UserRole;
import dev.razafindratelo.misinformation.model.classifier.UserStatus;
import dev.razafindratelo.misinformation.repository.UserRepository;
import dev.razafindratelo.misinformation.repository.model.JUser;
import dev.razafindratelo.misinformation.service.UserService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthManagerIT extends FacadeIT {

  private static final String TEST_EMAIL = "test@example.com";
  private static final String TEST_FULL_NAME = "Test User";
  private static final String TEST_PASSWORD = "SecurePass123!";
  private static final String OAUTH_EMAIL = "oauth@example.com";
  private static final String CLERK_ID_1 = randomUUID().toString();
  private static final String CLERK_ID_2 = randomUUID().toString();

  @Autowired private AuthManager authManager;
  @Autowired private UserService userService;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setup() {
    userRepository.deleteAll();
  }

  @AfterEach
  void cleanup() {
    userRepository.deleteAll();
  }

  @Test
  void authenticate_with_valid_password_succeeds() {
    createUserWithPassword(TEST_EMAIL, TEST_PASSWORD, CLERK_ID_1, UserStatus.ACTIVE);

    var request = AuthRequest.builder().email(TEST_EMAIL).password(TEST_PASSWORD).build();

    var result = authManager.authenticate(request);

    assertNotNull(result);
    assertEquals(TEST_EMAIL, result.getEmail());
    assertNotNull(result.getId());
  }

  @Test
  void authenticate_with_invalid_password_throws_authorization_denied_exception() {
    createUserWithPassword(TEST_EMAIL, TEST_PASSWORD, CLERK_ID_1, UserStatus.ACTIVE);

    var request = AuthRequest.builder().email(TEST_EMAIL).password("WrongPassword123!").build();

    assertThrows(AuthorizationDeniedException.class, () -> authManager.authenticate(request));
  }

  @Test
  void authenticate_with_password_on_oauth_only_account_throws_authorization_denied_exception() {
    createOAuthUser(OAUTH_EMAIL, CLERK_ID_1, UserStatus.ACTIVE);

    var request = AuthRequest.builder().email(OAUTH_EMAIL).password("SomePassword123!").build();

    var exception =
        assertThrows(AuthorizationDeniedException.class, () -> authManager.authenticate(request));

    assertTrue(exception.getMessage().contains("social login"));
  }

  @Test
  void authenticate_with_nonexistent_email_throws_authorization_denied_exception() {
    var request =
        AuthRequest.builder().email("nonexistent@example.com").password(TEST_PASSWORD).build();

    assertThrows(AuthorizationDeniedException.class, () -> authManager.authenticate(request));
  }

  @Test
  void authenticate_request__with_blank_password_throws_exception() {
    assertThrows(
        IllegalArgumentException.class,
        () -> AuthRequest.builder().email(TEST_EMAIL).password("   ").build());
  }

  @Test
  void authenticate_with_valid_clerk_id_succeeds() {
    createOAuthUser(OAUTH_EMAIL, CLERK_ID_1, UserStatus.ACTIVE);

    var request = AuthRequest.builder().email(OAUTH_EMAIL).clerkId(CLERK_ID_1).build();

    var result = authManager.authenticate(request);

    assertNotNull(result);
    assertEquals(OAUTH_EMAIL, result.getEmail());
    assertEquals(CLERK_ID_1, result.getClerkId());
  }

  @Test
  void authenticate_with_invalid_clerk_id_throws_authorization_denied_exception() {
    createOAuthUser(OAUTH_EMAIL, CLERK_ID_1, UserStatus.ACTIVE);

    var request = AuthRequest.builder().email(OAUTH_EMAIL).clerkId(CLERK_ID_2).build();

    assertThrows(AuthorizationDeniedException.class, () -> authManager.authenticate(request));
  }

  @Test
  void authenticate_with_clerk_id_on_password_account_succeeds() {
    createUserWithPassword(TEST_EMAIL, TEST_PASSWORD, CLERK_ID_1, UserStatus.ACTIVE);

    var request = AuthRequest.builder().email(TEST_EMAIL).clerkId(CLERK_ID_1).build();

    var result = authManager.authenticate(request);

    assertNotNull(result);
    assertEquals(TEST_EMAIL, result.getEmail());
  }

  @Test
  void authenticate_suspended_user_throws_account_suspended_exception() {
    createUserWithPassword(TEST_EMAIL, TEST_PASSWORD, CLERK_ID_1, UserStatus.SUSPENDED);

    var request = AuthRequest.builder().email(TEST_EMAIL).password(TEST_PASSWORD).build();

    var exception =
        assertThrows(AuthorizationDeniedException.class, () -> authManager.authenticate(request));

    assertTrue(exception.getMessage().contains("suspended"));
  }

  @Test
  void authenticate_inactive_user_throws_account_inactive_exception() {
    createUserWithPassword(TEST_EMAIL, TEST_PASSWORD, CLERK_ID_1, UserStatus.INACTIVE);

    var request = AuthRequest.builder().email(TEST_EMAIL).password(TEST_PASSWORD).build();

    var exception =
        assertThrows(AuthorizationDeniedException.class, () -> authManager.authenticate(request));

    assertTrue(exception.getMessage().contains("inactive"));
  }

  @Test
  void authenticate_deleted_user_throws_account_deleted_exception() {
    createUserWithPassword(TEST_EMAIL, TEST_PASSWORD, CLERK_ID_1, UserStatus.DELETED);

    var request = AuthRequest.builder().email(TEST_EMAIL).password(TEST_PASSWORD).build();

    var exception =
        assertThrows(AuthorizationDeniedException.class, () -> authManager.authenticate(request));

    assertTrue(exception.getMessage().contains("no longer exists"));
  }

  @Test
  void authenticate_active_user_succeeds() {
    createUserWithPassword(TEST_EMAIL, TEST_PASSWORD, CLERK_ID_1, UserStatus.ACTIVE);

    var request = AuthRequest.builder().email(TEST_EMAIL).password(TEST_PASSWORD).build();

    var result = authManager.authenticate(request);

    assertNotNull(result);
    assertEquals(UserStatus.ACTIVE, result.getStatus());
  }

  @Test
  void authenticate_suspended_oauth_user_throws_account_suspended_exception() {
    createOAuthUser(OAUTH_EMAIL, CLERK_ID_1, UserStatus.SUSPENDED);

    var request = AuthRequest.builder().email(OAUTH_EMAIL).clerkId(CLERK_ID_1).build();

    assertThrows(AuthorizationDeniedException.class, () -> authManager.authenticate(request));
  }

  @Test
  void authenticate_with_null_email_throws_validation_exception() {
    var request = AuthRequest.builder().email(null).password(TEST_PASSWORD).build();

    assertThrows(ConstraintViolationException.class, () -> authManager.authenticate(request));
  }

  @Test
  void authenticate_with_blank_email_throws_validation_exception() {
    var request = AuthRequest.builder().email("   ").password(TEST_PASSWORD).build();

    assertThrows(ConstraintViolationException.class, () -> authManager.authenticate(request));
  }

  @Test
  void authenticate_with_invalid_email_format_throws_validation_exception() {
    var request = AuthRequest.builder().email("invalid-email").password(TEST_PASSWORD).build();

    assertThrows(ConstraintViolationException.class, () -> authManager.authenticate(request));
  }

  @Test
  void authenticate_without_password_or_clerk_id_throws_illegal_argument_exception() {
    assertThrows(
        IllegalArgumentException.class, () -> AuthRequest.builder().email(TEST_EMAIL).build());
  }

  @Test
  void authenticate_with_null_request_throws_validation_exception() {
    assertThrows(ConstraintViolationException.class, () -> authManager.authenticate(null));
  }

  @Test
  void authenticate_different_users_with_same_password_succeeds() {
    createUserWithPassword("user1@example.com", TEST_PASSWORD, CLERK_ID_1, UserStatus.ACTIVE);
    createUserWithPassword("user2@example.com", TEST_PASSWORD, CLERK_ID_2, UserStatus.ACTIVE);

    var request1 = AuthRequest.builder().email("user1@example.com").password(TEST_PASSWORD).build();
    var request2 = AuthRequest.builder().email("user2@example.com").password(TEST_PASSWORD).build();

    var result1 = authManager.authenticate(request1);
    var result2 = authManager.authenticate(request2);

    assertNotNull(result1);
    assertNotNull(result2);
    assertNotEquals(result1.getId(), result2.getId());
  }

  @Test
  void authenticate_returns_user_with_all_fields() {
    createUserWithPassword(TEST_EMAIL, TEST_PASSWORD, CLERK_ID_1, UserStatus.ACTIVE);

    var request = AuthRequest.builder().email(TEST_EMAIL).password(TEST_PASSWORD).build();

    var result = authManager.authenticate(request);

    assertNotNull(result.getId());
    assertNotNull(result.getEmail());
    assertNotNull(result.getFullName());
    assertNotNull(result.getClerkId());
    assertNotNull(result.getRole());
    assertNotNull(result.getStatus());
    assertNotNull(result.getCreatedAt());
  }

  private User createUserWithPassword(
      String email, String password, String clerkId, UserStatus status) {
    var encodedPassword = passwordEncoder.encode(password);
    var jUser =
        JUser.builder()
            .id(randomUUID().toString())
            .email(email)
            .fullName(TEST_FULL_NAME)
            .clerkId(clerkId)
            .password(encodedPassword)
            .role(UserRole.USER)
            .status(status)
            .isEmailVerified(false)
            .createdAt(now())
            .build();

    userRepository.save(jUser);
    return userService.findByEmail(email);
  }

  private User createOAuthUser(String email, String clerkId, UserStatus status) {
    var jUser =
        JUser.builder()
            .id(randomUUID().toString())
            .email(email)
            .fullName(TEST_FULL_NAME)
            .clerkId(clerkId)
            .password(null)
            .role(UserRole.USER)
            .status(status)
            .isEmailVerified(true)
            .createdAt(now())
            .build();

    userRepository.save(jUser);
    return userService.findByEmail(email);
  }
}
