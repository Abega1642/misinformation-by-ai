package dev.razafindratelo.misinformation.service;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.razafindratelo.misinformation.conf.FacadeIT;
import dev.razafindratelo.misinformation.endpoint.rest.controller.model.UserRequest;
import dev.razafindratelo.misinformation.exception.ResourceDuplicatedException;
import dev.razafindratelo.misinformation.mapper.UserMapper;
import dev.razafindratelo.misinformation.model.User;
import dev.razafindratelo.misinformation.model.classifier.UserRole;
import dev.razafindratelo.misinformation.model.classifier.UserStatus;
import dev.razafindratelo.misinformation.repository.UserRepository;
import dev.razafindratelo.misinformation.service.util.Paginator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

public class UserServiceIT extends FacadeIT {

  private static final String TEST_EMAIL = "test@example.com";
  private static final String TEST_FULL_NAME = "test full name";
  private static final String MAIL_1 = "user1@example.com";
  private static final String MAIL_2 = "user2@example.com";
  private static final String MAIL_3 = "user3@example.com";
  private static final String CLERK_1 = randomUUID().toString();
  private static final String CLERK_2 = randomUUID().toString();
  private static final String CLERK_3 = randomUUID().toString();
  private static final String CLERK_ID = randomUUID().toString();

  @Autowired private UserService userService;
  @Autowired private UserRepository userRepository;
  @Autowired private UserMapper userMapper;
  @Autowired private Paginator paginator;

  @BeforeEach
  void setup() {
    userRepository.deleteAll();
  }

  @AfterEach
  void cleanup() {
    userRepository.deleteAll();
  }

  @Test
  void register_user_with_valid_data_succeeds() {
    var request =
        UserRequest.builder().email(TEST_EMAIL).fullName(TEST_FULL_NAME).clerkId(CLERK_ID).build();

    var result = userService.registerUser(request);

    assertNotNull(result);
    assertNotNull(result.getId());
    assertEquals(TEST_EMAIL, result.getEmail());
    assertEquals(CLERK_ID, result.getClerkId());
    assertEquals(UserRole.USER, result.getRole());
    assertEquals(UserStatus.INACTIVE, result.getStatus());

    var savedUser = userRepository.findById(result.getId());
    assertTrue(savedUser.isPresent());
    assertEquals(result.getEmail(), savedUser.get().getEmail());
  }

  @Test
  void register_user_with_custom_role_succeeds() {
    var request =
        UserRequest.builder()
            .email(TEST_EMAIL)
            .fullName(TEST_FULL_NAME)
            .clerkId(CLERK_ID)
            .role(UserRole.ADMIN)
            .build();

    var result = userService.registerUser(request);

    assertNotNull(result);
    assertEquals(UserRole.ADMIN, result.getRole());
  }

  @Test
  void register_user_with_custom_status_succeeds() {
    var request =
        UserRequest.builder()
            .email(TEST_EMAIL)
            .fullName(TEST_FULL_NAME)
            .clerkId(CLERK_ID)
            .status(UserStatus.ACTIVE)
            .build();

    var result = userService.registerUser(request);

    assertNotNull(result);
    assertEquals(UserStatus.ACTIVE, result.getStatus());
  }

  @Test
  void register_user_with_duplicate_email_throws_resource_duplicated_exception() {
    var request1 =
        UserRequest.builder().email(TEST_EMAIL).fullName(TEST_FULL_NAME).clerkId(CLERK_1).build();
    userService.registerUser(request1);

    var request2 =
        UserRequest.builder().email(TEST_EMAIL).fullName(TEST_FULL_NAME).clerkId(CLERK_2).build();

    assertThrows(ResourceDuplicatedException.class, () -> userService.registerUser(request2));
  }

  @Test
  void register_user_with_duplicate_clerk_id_throws_resource_duplicated_exception() {
    var request1 =
        UserRequest.builder().email(MAIL_1).fullName(TEST_FULL_NAME).clerkId(CLERK_ID).build();
    userService.registerUser(request1);

    var request2 =
        UserRequest.builder().email(MAIL_2).fullName(TEST_FULL_NAME).clerkId(CLERK_ID).build();

    assertThrows(ResourceDuplicatedException.class, () -> userService.registerUser(request2));
  }

  @Test
  void register_user_with_null_email_throws_validation_exception() {
    var request =
        UserRequest.builder().email(null).fullName(TEST_FULL_NAME).clerkId(CLERK_ID).build();

    assertThrows(ConstraintViolationException.class, () -> userService.registerUser(request));
  }

  @Test
  void register_user_with_blank_email_throws_validation_exception() {
    var request =
        UserRequest.builder().email("   ").fullName(TEST_FULL_NAME).clerkId(CLERK_ID).build();

    assertThrows(ConstraintViolationException.class, () -> userService.registerUser(request));
  }

  @Test
  void register_user_with_invalid_email_format_throws_validation_exception() {
    var request =
        UserRequest.builder()
            .email("invalid-email")
            .fullName(TEST_FULL_NAME)
            .clerkId(CLERK_ID)
            .build();

    assertThrows(ConstraintViolationException.class, () -> userService.registerUser(request));
  }

  @Test
  void register_user_with_null_clerk_id_throws_validation_exception() {
    var request =
        UserRequest.builder().email(TEST_EMAIL).fullName(TEST_FULL_NAME).clerkId(null).build();

    assertThrows(ConstraintViolationException.class, () -> userService.registerUser(request));
  }

  @Test
  void register_user_with_blank_clerk_id_throws_validation_exception() {
    var request =
        UserRequest.builder().email(TEST_EMAIL).fullName(TEST_FULL_NAME).clerkId("   ").build();

    assertThrows(ConstraintViolationException.class, () -> userService.registerUser(request));
  }

  @Test
  void register_user_with_null_full_name_throws_validation_exception() {
    var request = UserRequest.builder().email(TEST_EMAIL).fullName(null).clerkId(CLERK_ID).build();

    assertThrows(ConstraintViolationException.class, () -> userService.registerUser(request));
  }

  @Test
  void register_user_with_blank_full_name_throws_validation_exception() {
    var request = UserRequest.builder().email(TEST_EMAIL).fullName("   ").clerkId(CLERK_ID).build();

    assertThrows(ConstraintViolationException.class, () -> userService.registerUser(request));
  }

  @Test
  void register_user_with_null_request_throws_validation_exception() {
    assertThrows(ConstraintViolationException.class, () -> userService.registerUser(null));
  }

  @Test
  void register_multiple_users_with_unique_data_succeeds() {
    var request1 =
        UserRequest.builder().email(MAIL_1).fullName(TEST_FULL_NAME).clerkId(CLERK_1).build();
    var request2 =
        UserRequest.builder().email(MAIL_2).fullName(TEST_FULL_NAME).clerkId(CLERK_2).build();

    var result1 = userService.registerUser(request1);
    var result2 = userService.registerUser(request2);

    assertNotNull(result1);
    assertNotNull(result2);
    assertNotEquals(result1.getId(), result2.getId());
    assertEquals(2, userRepository.count());
  }

  @Test
  void find_by_email_with_existing_email_succeeds() {
    var registeredUser = createTestUser(TEST_EMAIL, CLERK_ID);

    var result = userService.findByEmail(TEST_EMAIL);

    assertNotNull(result);
    assertEquals(registeredUser.getId(), result.getId());
    assertEquals(TEST_EMAIL, result.getEmail());
    assertEquals(CLERK_ID, result.getClerkId());
  }

  @Test
  void find_by_email_with_nonexistent_email_throws_entity_not_found_exception() {
    assertThrows(
        EntityNotFoundException.class, () -> userService.findByEmail("nonexistent@example.com"));
  }

  @Test
  void find_by_email_with_null_email_throws_validation_exception() {
    assertThrows(ConstraintViolationException.class, () -> userService.findByEmail(null));
  }

  @Test
  void find_by_email_with_blank_email_throws_validation_exception() {
    assertThrows(ConstraintViolationException.class, () -> userService.findByEmail("   "));
  }

  @Test
  void find_by_email_with_invalid_email_format_throws_validation_exception() {
    assertThrows(
        ConstraintViolationException.class, () -> userService.findByEmail("invalid-email"));
  }

  @Test
  void find_by_clerk_id_with_existing_clerk_id_succeeds() {
    var registeredUser = createTestUser(TEST_EMAIL, CLERK_ID);

    var result = userService.findByClerkId(CLERK_ID);

    assertNotNull(result);
    assertEquals(registeredUser.getId(), result.getId());
    assertEquals(TEST_EMAIL, result.getEmail());
    assertEquals(CLERK_ID, result.getClerkId());
  }

  @Test
  void find_by_clerk_id_with_nonexistent_clerk_id_throws_entity_not_found_exception() {
    assertThrows(
        EntityNotFoundException.class, () -> userService.findByClerkId("nonexistent-clerk-id"));
  }

  @Test
  void find_by_clerk_id_with_null_clerk_id_throws_validation_exception() {
    assertThrows(ConstraintViolationException.class, () -> userService.findByClerkId(null));
  }

  @Test
  void find_by_clerk_id_with_blank_clerk_id_throws_validation_exception() {
    assertThrows(ConstraintViolationException.class, () -> userService.findByClerkId("   "));
  }

  @Test
  void authenticate_with_clerk_id_with_valid_credentials_succeeds() {
    var registeredUser = createTestUser(TEST_EMAIL, CLERK_ID);

    var result = userService.authenticateWithClerkId(TEST_EMAIL, CLERK_ID);

    assertNotNull(result);
    assertEquals(registeredUser.getId(), result.getId());
    assertEquals(TEST_EMAIL, result.getEmail());
    assertEquals(CLERK_ID, result.getClerkId());
  }

  @Test
  void authenticate_with_clerk_id_with_wrong_clerk_id_throws_entity_not_found_exception() {
    createTestUser(TEST_EMAIL, CLERK_1);

    assertThrows(
        EntityNotFoundException.class,
        () -> userService.authenticateWithClerkId(TEST_EMAIL, CLERK_2));
  }

  @Test
  void authenticate_with_clerk_id_with_nonexistent_email_throws_entity_not_found_exception() {
    assertThrows(
        EntityNotFoundException.class,
        () -> userService.authenticateWithClerkId("nonexistent@example.com", CLERK_ID));
  }

  @Test
  void authenticate_with_clerk_id_with_null_email_throws_validation_exception() {
    assertThrows(
        ConstraintViolationException.class,
        () -> userService.authenticateWithClerkId(null, CLERK_ID));
  }

  @Test
  void authenticate_with_clerk_id_with_blank_email_throws_validation_exception() {
    assertThrows(
        ConstraintViolationException.class,
        () -> userService.authenticateWithClerkId("   ", CLERK_ID));
  }

  @Test
  void authenticate_with_clerk_id_with_null_clerk_id_throws_validation_exception() {
    assertThrows(
        ConstraintViolationException.class,
        () -> userService.authenticateWithClerkId(TEST_EMAIL, null));
  }

  @Test
  void authenticate_with_clerk_id_with_blank_clerk_id_throws_validation_exception() {
    assertThrows(
        ConstraintViolationException.class,
        () -> userService.authenticateWithClerkId(TEST_EMAIL, "   "));
  }

  @Test
  void get_all_users_returns_empty_page_when_no_users_exist() {
    Page<User> result = userService.getAllUsers(0, 10);

    assertNotNull(result);
    assertEquals(0, result.getTotalElements());
    assertEquals(0, result.getContent().size());
    assertTrue(result.isEmpty());
  }

  @Test
  void get_all_users_returns_paginated_results() {
    createTestUser(MAIL_1, CLERK_1);
    createTestUser(MAIL_2, CLERK_2);
    createTestUser(MAIL_3, CLERK_3);

    Page<User> result = userService.getAllUsers(0, 2);

    assertNotNull(result);
    assertEquals(3, result.getTotalElements());
    assertEquals(2, result.getContent().size());
    assertEquals(2, result.getTotalPages());
  }

  @Test
  void get_all_users_returns_users_sorted_by_created_at_descending() {
    var user1 = createTestUser("first@example.com", CLERK_1);
    sleepBriefly();
    var user2 = createTestUser("second@example.com", CLERK_2);
    sleepBriefly();
    var user3 = createTestUser("third@example.com", CLERK_3);

    Page<User> result = userService.getAllUsers(0, 10);

    assertNotNull(result);
    assertEquals(3, result.getContent().size());
    assertEquals(user3.getEmail(), result.getContent().get(0).getEmail());
    assertEquals(user2.getEmail(), result.getContent().get(1).getEmail());
    assertEquals(user1.getEmail(), result.getContent().get(2).getEmail());
  }

  @Test
  void get_all_users_respects_page_size() {
    createTestUser(MAIL_1, CLERK_1);
    createTestUser(MAIL_2, CLERK_2);
    createTestUser(MAIL_3, CLERK_3);
    createTestUser("user4@example.com", randomUUID().toString());
    createTestUser("user5@example.com", randomUUID().toString());

    Page<User> firstPage = userService.getAllUsers(0, 2);
    Page<User> secondPage = userService.getAllUsers(1, 2);

    assertEquals(2, firstPage.getContent().size());
    assertEquals(2, secondPage.getContent().size());
    assertNotEquals(
        firstPage.getContent().getFirst().getId(), secondPage.getContent().getFirst().getId());
  }

  @Test
  void get_all_users_returns_empty_page_for_page_beyond_results() {
    createTestUser(MAIL_1, CLERK_1);
    createTestUser(MAIL_2, CLERK_2);

    Page<User> result = userService.getAllUsers(5, 10);

    assertNotNull(result);
    assertEquals(2, result.getTotalElements());
    assertEquals(0, result.getContent().size());
  }

  private User createTestUser(String email, String clerkId) {
    var request =
        UserRequest.builder().email(email).fullName(TEST_FULL_NAME).clerkId(clerkId).build();
    return userService.registerUser(request);
  }

  private void sleepBriefly() {
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
