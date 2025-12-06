package dev.razafindratelo.misinformation.service;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;

import dev.razafindratelo.misinformation.conf.FacadeIT;
import dev.razafindratelo.misinformation.endpoint.rest.controller.model.UserRequest;
import dev.razafindratelo.misinformation.mapper.UserMapper;
import dev.razafindratelo.misinformation.model.User;
import dev.razafindratelo.misinformation.repository.UserRepository;
import dev.razafindratelo.misinformation.service.util.Paginator;
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
  private static final String CLERK_1 = randomUUID().toString();
  private static final String CLERK_2 = randomUUID().toString();
  private static final String CLERK_3 = randomUUID().toString();
  private static final String CLERK_ID = randomUUID().toString();
  private static final String MAIL_3 = "user3@example.com";
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
    var request = new UserRequest(TEST_EMAIL, TEST_FULL_NAME, CLERK_ID);

    var result = userService.registerUser(request);

    assertNotNull(result);
    assertNotNull(result.id());
    assertEquals(TEST_EMAIL, result.email());
    assertEquals(CLERK_ID, result.clerkId());

    var savedUser = userRepository.findById(result.id());
    assertTrue(savedUser.isPresent());
    assertEquals(result.email(), savedUser.get().getEmail());
  }

  @Test
  void register_user_with_null_email_throws_validation_exception() {
    var request = new UserRequest(null, TEST_FULL_NAME, CLERK_ID);

    assertThrows(
        ConstraintViolationException.class,
        () -> {
          userService.registerUser(request);
        });
  }

  @Test
  void register_user_with_blank_email_throws_validation_exception() {
    var request = new UserRequest("   ", TEST_FULL_NAME, CLERK_ID);

    assertThrows(
        ConstraintViolationException.class,
        () -> {
          userService.registerUser(request);
        });
  }

  @Test
  void register_user_with_invalid_email_format_throws_validation_exception() {
    var request = new UserRequest("invalid-email", TEST_FULL_NAME, CLERK_ID);

    assertThrows(
        ConstraintViolationException.class,
        () -> {
          userService.registerUser(request);
        });
  }

  @Test
  void register_user_with_null_clerk_id_throws_validation_exception() {
    var request = new UserRequest(TEST_EMAIL, TEST_FULL_NAME, null);

    assertThrows(
        ConstraintViolationException.class,
        () -> {
          userService.registerUser(request);
        });
  }

  @Test
  void register_user_with_blank_clerk_id_throws_validation_exception() {
    var request = new UserRequest(TEST_EMAIL, TEST_FULL_NAME, "   ");

    assertThrows(
        ConstraintViolationException.class,
        () -> {
          userService.registerUser(request);
        });
  }

  @Test
  void register_user_with_null_request_throws_validation_exception() {
    assertThrows(
        ConstraintViolationException.class,
        () -> {
          userService.registerUser(null);
        });
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
    assertEquals(user3.email(), result.getContent().get(0).email());
    assertEquals(user2.email(), result.getContent().get(1).email());
    assertEquals(user1.email(), result.getContent().get(2).email());
  }

  @Test
  void get_all_users_respects_page_size() {
    createTestUser(MAIL_1, CLERK_1);
    createTestUser(MAIL_2, CLERK_2);
    createTestUser(MAIL_3, CLERK_3);
    createTestUser("user4@example.com", "clerk_4");
    createTestUser("user5@example.com", "clerk_5");

    Page<User> firstPage = userService.getAllUsers(0, 2);
    Page<User> secondPage = userService.getAllUsers(1, 2);

    assertEquals(2, firstPage.getContent().size());
    assertEquals(2, secondPage.getContent().size());
    assertNotEquals(
        firstPage.getContent().getFirst().id(), secondPage.getContent().getFirst().id());
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

  @Test
  void register_multiple_users_with_unique_emails_succeeds() {
    var request1 = new UserRequest(MAIL_1, TEST_FULL_NAME, CLERK_1);
    var request2 = new UserRequest(MAIL_2, TEST_FULL_NAME, CLERK_2);

    var result1 = userService.registerUser(request1);
    var result2 = userService.registerUser(request2);

    assertNotNull(result1);
    assertNotNull(result2);
    assertNotEquals(result1.id(), result2.id());
    assertEquals(2, userRepository.count());
  }

  private User createTestUser(String email, String clerkId) {
    var request = new UserRequest(email, TEST_FULL_NAME, clerkId);
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
