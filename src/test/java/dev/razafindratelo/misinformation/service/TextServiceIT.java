package dev.razafindratelo.misinformation.service;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;

import dev.razafindratelo.misinformation.conf.FacadeIT;
import dev.razafindratelo.misinformation.endpoint.rest.controller.model.TextRequest;
import dev.razafindratelo.misinformation.endpoint.rest.controller.model.UserRequest;
import dev.razafindratelo.misinformation.model.Text;
import dev.razafindratelo.misinformation.model.User;
import dev.razafindratelo.misinformation.repository.TextRepository;
import dev.razafindratelo.misinformation.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.authorization.AuthorizationDeniedException;

public class TextServiceIT extends FacadeIT {

  private static final String TEST_EMAIL = "test@example.com";
  private static final String TEST_FULL_NAME = "test full name";
  private static final String CLERK_ID = randomUUID().toString();
  private static final String EMAIL_1 = "user1@example.com";
  private static final String EMAIL_2 = "user2@example.com";
  private static final String CLERK_1 = randomUUID().toString();
  private static final String CLERK_2 = randomUUID().toString();

  @Autowired private TextService textService;
  @Autowired private TextRepository textRepository;
  @Autowired private UserService userService;
  @Autowired private UserRepository userRepository;

  @BeforeEach
  void setup() {
    textRepository.deleteAll();
    userRepository.deleteAll();
  }

  @AfterEach
  void cleanup() {
    textRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void upload_text_with_valid_data_succeeds() {
    var user = createTestUser(TEST_EMAIL, CLERK_ID);
    var textContent = "This is a test text content for upload.";
    var request = TextRequest.builder().email(user.email()).text(textContent).build();

    var result = textService.uploadText(request);

    assertNotNull(result);
    assertNotNull(result.id());
    assertEquals(textContent, result.value());
    assertEquals(user.email(), result.owner().email());

    var savedText = textRepository.findById(result.id());
    assertTrue(savedText.isPresent());
    assertEquals(textContent, savedText.get().getValue());
  }

  @Test
  void upload_text_with_null_email_throws_validation_exception() {
    var request = TextRequest.builder().email(null).text("Some text").build();

    assertThrows(ConstraintViolationException.class, () -> textService.uploadText(request));
  }

  @Test
  void upload_text_with_blank_email_throws_validation_exception() {
    var request = TextRequest.builder().email("   ").text("Some text").build();

    assertThrows(ConstraintViolationException.class, () -> textService.uploadText(request));
  }

  @Test
  void upload_text_with_invalid_email_format_throws_validation_exception() {
    var request = TextRequest.builder().email("invalid-email").text("Some text").build();

    assertThrows(ConstraintViolationException.class, () -> textService.uploadText(request));
  }

  @Test
  void upload_text_with_null_text_throws_validation_exception() {
    var request = TextRequest.builder().email(TEST_EMAIL).text(null).build();

    assertThrows(ConstraintViolationException.class, () -> textService.uploadText(request));
  }

  @Test
  void upload_text_with_blank_text_throws_validation_exception() {
    var request = TextRequest.builder().email(TEST_EMAIL).text("   ").build();

    assertThrows(ConstraintViolationException.class, () -> textService.uploadText(request));
  }

  @Test
  void upload_text_with_null_request_throws_validation_exception() {
    assertThrows(ConstraintViolationException.class, () -> textService.uploadText(null));
  }

  @Test
  void find_text_instance_with_valid_id_and_email_succeeds() {
    var user = createTestUser(TEST_EMAIL, CLERK_ID);
    var text = createTestText(user, "Test text content");

    var result = textService.findTextInstance(text.id(), user.email());

    assertNotNull(result);
    assertEquals(text.id(), result.id());
    assertEquals("Test text content", result.value());
    assertEquals(user.email(), result.owner().email());
  }

  @Test
  void find_text_instance_with_nonexistent_id_throws_entity_not_found_exception() {
    var user = createTestUser(TEST_EMAIL, CLERK_ID);
    var nonExistentId = randomUUID().toString();

    assertThrows(
        EntityNotFoundException.class,
        () -> textService.findTextInstance(nonExistentId, user.email()));
  }

  @Test
  void find_text_instance_with_different_user_email_throws_authorization_denied_exception() {
    var user1 = createTestUser(EMAIL_1, CLERK_1);
    var user2 = createTestUser(EMAIL_2, CLERK_2);
    var text = createTestText(user1, "User1's text");

    assertThrows(
        AuthorizationDeniedException.class,
        () -> textService.findTextInstance(text.id(), user2.email()));
  }

  @Test
  void find_all_by_email_returns_empty_page_when_no_texts_exist() {
    var user = createTestUser(TEST_EMAIL, CLERK_ID);

    Page<Text> result = textService.findAllByEmail(0, 10, user.email());

    assertNotNull(result);
    assertEquals(0, result.getTotalElements());
    assertEquals(0, result.getContent().size());
    assertTrue(result.isEmpty());
  }

  @Test
  void find_all_by_email_returns_paginated_results() {
    var user = createTestUser(TEST_EMAIL, CLERK_ID);
    createTestText(user, "Text 1");
    createTestText(user, "Text 2");
    createTestText(user, "Text 3");

    Page<Text> result = textService.findAllByEmail(0, 2, user.email());

    assertNotNull(result);
    assertEquals(3, result.getTotalElements());
    assertEquals(2, result.getContent().size());
    assertEquals(2, result.getTotalPages());
  }

  @Test
  void find_all_by_email_returns_texts_sorted_by_created_at_descending() {
    var user = createTestUser(TEST_EMAIL, CLERK_ID);
    var text1 = createTestText(user, "First text");
    sleepBriefly();
    var text2 = createTestText(user, "Second text");
    sleepBriefly();
    var text3 = createTestText(user, "Third text");

    Page<Text> result = textService.findAllByEmail(0, 10, user.email());

    assertNotNull(result);
    assertEquals(3, result.getContent().size());
    assertEquals(text3.id(), result.getContent().get(0).id());
    assertEquals(text2.id(), result.getContent().get(1).id());
    assertEquals(text1.id(), result.getContent().get(2).id());
  }

  @Test
  void find_all_by_email_respects_page_size() {
    var user = createTestUser(TEST_EMAIL, CLERK_ID);
    createTestText(user, "Text 1");
    createTestText(user, "Text 2");
    createTestText(user, "Text 3");
    createTestText(user, "Text 4");
    createTestText(user, "Text 5");

    Page<Text> firstPage = textService.findAllByEmail(0, 2, user.email());
    Page<Text> secondPage = textService.findAllByEmail(1, 2, user.email());

    assertEquals(2, firstPage.getContent().size());
    assertEquals(2, secondPage.getContent().size());
    assertNotEquals(
        firstPage.getContent().getFirst().id(), secondPage.getContent().getFirst().id());
  }

  @Test
  void find_all_by_email_returns_empty_page_for_page_beyond_results() {
    var user = createTestUser(TEST_EMAIL, CLERK_ID);
    createTestText(user, "Text 1");
    createTestText(user, "Text 2");

    Page<Text> result = textService.findAllByEmail(5, 10, user.email());

    assertNotNull(result);
    assertEquals(2, result.getTotalElements());
    assertEquals(0, result.getContent().size());
  }

  @Test
  void find_all_by_email_only_returns_texts_of_specific_user() {
    var user1 = createTestUser(EMAIL_1, CLERK_1);
    var user2 = createTestUser(EMAIL_2, CLERK_2);

    createTestText(user1, "User1 Text 1");
    createTestText(user1, "User1 Text 2");
    createTestText(user2, "User2 Text 1");

    Page<Text> user1Texts = textService.findAllByEmail(0, 10, user1.email());
    Page<Text> user2Texts = textService.findAllByEmail(0, 10, user2.email());

    assertEquals(2, user1Texts.getTotalElements());
    assertEquals(1, user2Texts.getTotalElements());
  }

  private User createTestUser(String email, String clerkId) {
    var request =
        UserRequest.builder().email(email).fullName(TEST_FULL_NAME).clerkId(clerkId).build();
    return userService.registerUser(request);
  }

  private Text createTestText(User owner, String textContent) {
    var request = TextRequest.builder().email(owner.email()).text(textContent).build();
    return textService.uploadText(request);
  }

  private void sleepBriefly() {
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
