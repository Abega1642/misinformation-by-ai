package dev.razafindratelo.misinformation.service;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;

import dev.razafindratelo.misinformation.conf.FacadeIT;
import dev.razafindratelo.misinformation.endpoint.rest.controller.model.TokenRequest;
import dev.razafindratelo.misinformation.endpoint.rest.controller.model.UserRequest;
import dev.razafindratelo.misinformation.model.Token;
import dev.razafindratelo.misinformation.model.User;
import dev.razafindratelo.misinformation.repository.TokenRepository;
import dev.razafindratelo.misinformation.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TokenServiceIT extends FacadeIT {

  private static final String TEST_EMAIL = "test@example.com";
  private static final String TEST_FULL_NAME = "test full name";
  private static final String CLERK_ID = randomUUID().toString();
  private static final String TOKEN_VALUE = "test-token-value";
  private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(7);
  private static final LocalDate PAST_DATE = LocalDate.now().minusDays(1);
  private static final String CLERK_2 = randomUUID().toString();
  private static final String EMAIL_2 = "user2@example.com";

  @Autowired private TokenService tokenService;
  @Autowired private TokenRepository tokenRepository;
  @Autowired private UserService userService;
  @Autowired private UserRepository userRepository;

  @BeforeEach
  void setup() {
    tokenRepository.deleteAll();
    userRepository.deleteAll();
  }

  @AfterEach
  void cleanup() {
    tokenRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void save_user_token_with_valid_data_succeeds() {
    var user = createTestUser(TEST_EMAIL, CLERK_ID);
    var request =
        TokenRequest.builder()
            .clerkId(user.clerkId())
            .token(TOKEN_VALUE)
            .expirationDate(FUTURE_DATE)
            .build();

    var result = tokenService.saveUserToken(request);

    assertNotNull(result);
    assertNotNull(result.id());
    assertEquals(TOKEN_VALUE, result.token());
    assertEquals(FUTURE_DATE, result.expirationDate());
    assertEquals(user.email(), result.owner().email());

    var savedToken = tokenRepository.findById(result.id());
    assertTrue(savedToken.isPresent());
    assertEquals(TOKEN_VALUE, savedToken.get().getToken());
  }

  @Test
  void save_user_token_with_null_clerk_id_throws_validation_exception() {
    var request =
        TokenRequest.builder().clerkId(null).token(TOKEN_VALUE).expirationDate(FUTURE_DATE).build();

    assertThrows(ConstraintViolationException.class, () -> tokenService.saveUserToken(request));
  }

  @Test
  void save_user_token_with_blank_clerk_id_throws_validation_exception() {
    var request =
        TokenRequest.builder()
            .clerkId("   ")
            .token(TOKEN_VALUE)
            .expirationDate(FUTURE_DATE)
            .build();

    assertThrows(ConstraintViolationException.class, () -> tokenService.saveUserToken(request));
  }

  @Test
  void save_user_token_with_null_token_throws_validation_exception() {
    createTestUser(TEST_EMAIL, CLERK_ID);
    var request =
        TokenRequest.builder().clerkId(CLERK_ID).token(null).expirationDate(FUTURE_DATE).build();

    assertThrows(ConstraintViolationException.class, () -> tokenService.saveUserToken(request));
  }

  @Test
  void save_user_token_with_blank_token_throws_validation_exception() {
    createTestUser(TEST_EMAIL, CLERK_ID);
    var request =
        TokenRequest.builder().clerkId(CLERK_ID).token("   ").expirationDate(FUTURE_DATE).build();

    assertThrows(ConstraintViolationException.class, () -> tokenService.saveUserToken(request));
  }

  @Test
  void save_user_token_with_null_expiration_date_throws_validation_exception() {
    createTestUser(TEST_EMAIL, CLERK_ID);
    var request =
        TokenRequest.builder().clerkId(CLERK_ID).token(TOKEN_VALUE).expirationDate(null).build();

    assertThrows(ConstraintViolationException.class, () -> tokenService.saveUserToken(request));
  }

  @Test
  void save_user_token_with_null_request_throws_validation_exception() {
    assertThrows(ConstraintViolationException.class, () -> tokenService.saveUserToken(null));
  }

  @Test
  void save_user_token_with_nonexistent_clerk_id_throws_entity_not_found_exception() {
    var request =
        TokenRequest.builder()
            .clerkId("nonexistent-clerk-id")
            .token(TOKEN_VALUE)
            .expirationDate(FUTURE_DATE)
            .build();

    assertThrows(EntityNotFoundException.class, () -> tokenService.saveUserToken(request));
  }

  @Test
  void save_user_token_with_duplicate_token_value_succeeds() {
    var user1 = createTestUser(TEST_EMAIL, CLERK_ID);
    var user2 = createTestUser(EMAIL_2, CLERK_2);

    var request1 =
        TokenRequest.builder()
            .clerkId(user1.clerkId())
            .token(TOKEN_VALUE)
            .expirationDate(FUTURE_DATE)
            .build();

    var request2 =
        TokenRequest.builder()
            .clerkId(user2.clerkId())
            .token(TOKEN_VALUE)
            .expirationDate(FUTURE_DATE)
            .build();

    var result1 = tokenService.saveUserToken(request1);
    var result2 = tokenService.saveUserToken(request2);

    assertNotNull(result1);
    assertNotNull(result2);
    assertNotEquals(result1.id(), result2.id());
    assertEquals(TOKEN_VALUE, result1.token());
    assertEquals(TOKEN_VALUE, result2.token());
    assertEquals(2, tokenRepository.count());
  }

  @Test
  void find_token_by_value_with_valid_token_succeeds() {
    var user = createTestUser(TEST_EMAIL, CLERK_ID);
    var token = createTestToken(user, TOKEN_VALUE, FUTURE_DATE);

    var result = tokenService.findTokenByValue(TOKEN_VALUE);

    assertNotNull(result);
    assertEquals(token.id(), result.id());
    assertEquals(TOKEN_VALUE, result.token());
    assertEquals(user.email(), result.owner().email());
  }

  @Test
  void find_token_by_value_with_nonexistent_token_throws_entity_not_found_exception() {
    assertThrows(
        EntityNotFoundException.class, () -> tokenService.findTokenByValue("nonexistent-token"));
  }

  @Test
  void find_token_by_value_with_null_token_throws_validation_exception() {
    assertThrows(ConstraintViolationException.class, () -> tokenService.findTokenByValue(null));
  }

  @Test
  void find_token_by_value_with_blank_token_throws_validation_exception() {
    assertThrows(ConstraintViolationException.class, () -> tokenService.findTokenByValue("   "));
  }

  @Test
  void is_valid_returns_true_for_future_expiration_date() {
    var user = createTestUser(TEST_EMAIL, CLERK_ID);
    createTestToken(user, TOKEN_VALUE, FUTURE_DATE);

    var isValid = tokenService.isValid(TOKEN_VALUE);

    assertTrue(isValid);
  }

  @Test
  void is_valid_returns_false_for_past_expiration_date() {
    var user = createTestUser(TEST_EMAIL, CLERK_ID);
    createTestToken(user, TOKEN_VALUE, PAST_DATE);

    var isValid = tokenService.isValid(TOKEN_VALUE);

    assertFalse(isValid);
  }

  @Test
  void is_valid_with_nonexistent_token_throws_entity_not_found_exception() {
    assertThrows(EntityNotFoundException.class, () -> tokenService.isValid("nonexistent-token"));
  }

  @Test
  void is_valid_with_null_token_throws_validation_exception() {
    assertThrows(ConstraintViolationException.class, () -> tokenService.isValid(null));
  }

  @Test
  void is_valid_with_blank_token_throws_validation_exception() {
    assertThrows(ConstraintViolationException.class, () -> tokenService.isValid("   "));
  }

  @Test
  void multiple_tokens_for_same_user_succeeds() {
    var user = createTestUser(TEST_EMAIL, CLERK_ID);
    var token1Value = "token-1";
    var token2Value = "token-2";

    var token1 = createTestToken(user, token1Value, FUTURE_DATE);
    var token2 = createTestToken(user, token2Value, FUTURE_DATE.plusDays(1));

    var foundToken1 = tokenService.findTokenByValue(token1Value);
    var foundToken2 = tokenService.findTokenByValue(token2Value);

    assertNotNull(foundToken1);
    assertNotNull(foundToken2);
    assertEquals(token1.id(), foundToken1.id());
    assertEquals(token2.id(), foundToken2.id());
    assertEquals(2, tokenRepository.count());
  }

  private User createTestUser(String email, String clerkId) {
    var request =
        UserRequest.builder().email(email).fullName(TEST_FULL_NAME).clerkId(clerkId).build();
    return userService.registerUser(request);
  }

  private Token createTestToken(User owner, String tokenValue, LocalDate expirationDate) {
    var request =
        TokenRequest.builder()
            .clerkId(owner.clerkId())
            .token(tokenValue)
            .expirationDate(expirationDate)
            .build();
    return tokenService.saveUserToken(request);
  }
}
