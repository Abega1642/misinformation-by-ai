package dev.razafindratelo.misinformation.endpoint.rest.controller;

import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.razafindratelo.misinformation.conf.FacadeIT;
import dev.razafindratelo.misinformation.endpoint.rest.controller.model.AuthRequest;
import dev.razafindratelo.misinformation.model.classifier.UserRole;
import dev.razafindratelo.misinformation.model.classifier.UserStatus;
import dev.razafindratelo.misinformation.repository.UserRepository;
import dev.razafindratelo.misinformation.repository.model.JUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

public class AuthControllerIT extends FacadeIT {

  private static final String TEST_EMAIL = "test@example.com";
  private static final String TEST_FULL_NAME = "Test User";
  private static final String TEST_PASSWORD = "SecurePass123!";
  private static final String OAUTH_EMAIL = "oauth@example.com";
  private static final String CLERK_ID = randomUUID().toString();
  private static final String CLERK_ID_2 = randomUUID().toString();
  private static final String AUTH_LOGIN_ENDPOINT = "/auth/login";
  private static final String ERROR_FORBIDDEN = "Forbidden";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
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
  void login_with_valid_password_returns_200_and_user() throws Exception {
    createUserWithPassword(UserStatus.ACTIVE);

    var authRequest = AuthRequest.builder().email(TEST_EMAIL).password(TEST_PASSWORD).build();

    mockMvc
        .perform(
            post(AUTH_LOGIN_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.email").value(TEST_EMAIL))
        .andExpect(jsonPath("$.clerk_id").value(CLERK_ID))
        .andExpect(jsonPath("$.role").value("USER"))
        .andExpect(jsonPath("$.status").value("ACTIVE"));
  }

  @Test
  void login_with_invalid_password_returns_403() throws Exception {
    createUserWithPassword(UserStatus.ACTIVE);

    var authRequest = AuthRequest.builder().email(TEST_EMAIL).password("WrongPassword123!").build();

    mockMvc
        .perform(
            post(AUTH_LOGIN_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value(ERROR_FORBIDDEN))
        .andExpect(jsonPath("$.message").value("Invalid credentials"));
  }

  @Test
  void login_with_password_on_oauth_only_account_returns_403() throws Exception {
    createOAuthUser();

    var authRequest = AuthRequest.builder().email(OAUTH_EMAIL).password("SomePassword123!").build();

    mockMvc
        .perform(
            post(AUTH_LOGIN_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value(ERROR_FORBIDDEN))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(jsonPath("$.message").value(containsString("social login")));
  }

  @Test
  void login_with_nonexistent_email_returns_403() throws Exception {
    var authRequest =
        AuthRequest.builder().email("nonexistent@example.com").password(TEST_PASSWORD).build();

    mockMvc
        .perform(
            post(AUTH_LOGIN_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value(ERROR_FORBIDDEN));
  }

  @Test
  void login_with_valid_clerk_id_returns_200_and_user() throws Exception {
    createOAuthUser();

    var authRequest = AuthRequest.builder().email(OAUTH_EMAIL).clerkId(CLERK_ID).build();

    mockMvc
        .perform(
            post(AUTH_LOGIN_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(OAUTH_EMAIL))
        .andExpect(jsonPath("$.clerk_id").value(CLERK_ID))
        .andExpect(jsonPath("$.email_verified").value(false));
  }

  @Test
  void login_with_invalid_clerk_id_returns_403() throws Exception {
    createOAuthUser();

    var authRequest = AuthRequest.builder().email(OAUTH_EMAIL).clerkId(CLERK_ID_2).build();

    mockMvc
        .perform(
            post(AUTH_LOGIN_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value(ERROR_FORBIDDEN));
  }

  @Test
  void login_with_clerk_id_on_password_account_returns_200() throws Exception {
    createUserWithPassword(UserStatus.ACTIVE);

    var authRequest = AuthRequest.builder().email(TEST_EMAIL).clerkId(CLERK_ID).build();

    mockMvc
        .perform(
            post(AUTH_LOGIN_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(TEST_EMAIL));
  }

  @Test
  void login_with_suspended_account_returns_403() throws Exception {
    createUserWithPassword(UserStatus.SUSPENDED);

    var authRequest = AuthRequest.builder().email(TEST_EMAIL).password(TEST_PASSWORD).build();

    mockMvc
        .perform(
            post(AUTH_LOGIN_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value(containsString("suspended")));
  }

  @Test
  void login_with_inactive_account_returns_403() throws Exception {
    createUserWithPassword(UserStatus.INACTIVE);

    var authRequest = AuthRequest.builder().email(TEST_EMAIL).password(TEST_PASSWORD).build();

    mockMvc
        .perform(
            post(AUTH_LOGIN_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value(ERROR_FORBIDDEN))
        .andExpect(jsonPath("$.message").value(containsString("inactive")));
  }

  @Test
  void login_with_deleted_account_returns_403() throws Exception {
    createUserWithPassword(UserStatus.DELETED);

    var authRequest = AuthRequest.builder().email(TEST_EMAIL).password(TEST_PASSWORD).build();

    mockMvc
        .perform(
            post(AUTH_LOGIN_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value(ERROR_FORBIDDEN))
        .andExpect(jsonPath("$.message").value(containsString("no longer exists")));
  }

  @Test
  void login_with_null_email_returns_400() throws Exception {
    var authRequest = AuthRequest.builder().email(null).password(TEST_PASSWORD).build();

    mockMvc
        .perform(
            post(AUTH_LOGIN_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void login_with_blank_email_returns_400() throws Exception {
    var authRequest = AuthRequest.builder().email("   ").password(TEST_PASSWORD).build();

    mockMvc
        .perform(
            post(AUTH_LOGIN_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void login_with_invalid_email_format_returns_400() throws Exception {
    var authRequest = AuthRequest.builder().email("invalid-email").password(TEST_PASSWORD).build();

    mockMvc
        .perform(
            post(AUTH_LOGIN_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void login_with_empty_body_returns_400() throws Exception {
    mockMvc
        .perform(post(AUTH_LOGIN_ENDPOINT).contentType(APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void login_with_malformed_json_returns_400() throws Exception {
    var invalidJson = "{invalid json}";
    mockMvc
        .perform(post(AUTH_LOGIN_ENDPOINT).contentType(APPLICATION_JSON).content(invalidJson))
        .andExpect(status().isBadRequest());
  }

  private void createUserWithPassword(UserStatus status) {
    var encodedPassword = passwordEncoder.encode(AuthControllerIT.TEST_PASSWORD);
    var jUser =
        JUser.builder()
            .id(randomUUID().toString())
            .email(AuthControllerIT.TEST_EMAIL)
            .fullName(TEST_FULL_NAME)
            .clerkId(AuthControllerIT.CLERK_ID)
            .password(encodedPassword)
            .role(UserRole.USER)
            .status(status)
            .isEmailVerified(false)
            .createdAt(now())
            .build();

    userRepository.save(jUser);
  }

  private void createOAuthUser() {
    var jUser =
        JUser.builder()
            .id(randomUUID().toString())
            .email(AuthControllerIT.OAUTH_EMAIL)
            .fullName(TEST_FULL_NAME)
            .clerkId(AuthControllerIT.CLERK_ID)
            .password(null)
            .role(UserRole.USER)
            .status(UserStatus.ACTIVE)
            .isEmailVerified(true)
            .createdAt(now())
            .build();

    userRepository.save(jUser);
  }
}
