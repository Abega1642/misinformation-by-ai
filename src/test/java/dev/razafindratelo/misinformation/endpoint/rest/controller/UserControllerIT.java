package dev.razafindratelo.misinformation.endpoint.rest.controller;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.razafindratelo.misinformation.conf.FacadeIT;
import dev.razafindratelo.misinformation.endpoint.rest.controller.model.UserRequest;
import dev.razafindratelo.misinformation.model.User;
import dev.razafindratelo.misinformation.repository.UserRepository;
import dev.razafindratelo.misinformation.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

public class UserControllerIT extends FacadeIT {

  private static final String TEST_EMAIL = "test@example.com";
  private static final String CLERK_ID = randomUUID().toString();
  private static final String USERS_SIGN_UP_ENDPOINT = "/users/sign-up";
  private static final String USERS_ENDPOINT = "/users";
  private static final String TEST_EMAIL_1 = "user1@example.com";
  private static final String TEST_EMAIL_2 = "user2@example.com";
  private static final String TEST_EMAIL_3 = "user3@example.com";
  private static final String CLERK_ID_1 = "clerk_1";
  private static final String CLERK_ID_2 = "clerk_2";
  private static final String CLERK_ID_3 = "clerk_3";
  private static final String PAGE_PARAM = "page";
  private static final String SIZE_PARAM = "size";
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserRepository userRepository;
  @Autowired private UserService userService;

  @BeforeEach
  void setup() {
    userRepository.deleteAll();
  }

  @AfterEach
  void cleanup() {
    userRepository.deleteAll();
  }

  @Test
  void register_user_with_valid_data_returns_200_and_user() throws Exception {
    var userRequest = new UserRequest(TEST_EMAIL, CLERK_ID);
    var request = generateRequest(userRequest);

    var response =
        mockMvc
            .perform(post(USERS_SIGN_UP_ENDPOINT).contentType(APPLICATION_JSON).content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.email").value(TEST_EMAIL))
            .andExpect(jsonPath("$.clerk_id").value(CLERK_ID))
            .andReturn();

    var content = response.getResponse().getContentAsString();
    var user = objectMapper.readValue(content, User.class);

    assertNotNull(user.id());
    assertEquals(1, userRepository.count());
  }

  @Test
  void register_user_with_null_email_returns_400() throws Exception {
    var userRequest = new UserRequest(null, CLERK_ID);
    var request = generateRequest(userRequest);

    mockMvc
        .perform(post(USERS_SIGN_UP_ENDPOINT).contentType(APPLICATION_JSON).content(request))
        .andExpect(status().isBadRequest());

    assertEquals(0, userRepository.count());
  }

  @Test
  void register_user_with_blank_email_returns_400() throws Exception {
    var userRequest = new UserRequest("   ", CLERK_ID);
    var request = generateRequest(userRequest);

    mockMvc
        .perform(post(USERS_SIGN_UP_ENDPOINT).contentType(APPLICATION_JSON).content(request))
        .andExpect(status().isBadRequest());

    assertEquals(0, userRepository.count());
  }

  @Test
  void register_user_with_invalid_email_format_returns_400() throws Exception {
    var userRequest = new UserRequest("invalid-email", CLERK_ID);
    var request = generateRequest(userRequest);

    mockMvc
        .perform(post(USERS_SIGN_UP_ENDPOINT).contentType(APPLICATION_JSON).content(request))
        .andExpect(status().isBadRequest());

    assertEquals(0, userRepository.count());
  }

  @Test
  void register_user_with_null_clerk_id_returns_400() throws Exception {
    var userRequest = new UserRequest(TEST_EMAIL, null);
    var request = generateRequest(userRequest);

    mockMvc
        .perform(post(USERS_SIGN_UP_ENDPOINT).contentType(APPLICATION_JSON).content(request))
        .andExpect(status().isBadRequest());

    assertEquals(0, userRepository.count());
  }

  @Test
  void register_user_with_blank_clerk_id_returns_400() throws Exception {
    var request = new UserRequest(TEST_EMAIL, "   ");

    mockMvc
        .perform(
            post(USERS_SIGN_UP_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    assertEquals(0, userRepository.count());
  }

  @Test
  void register_user_with_empty_body_returns_400() throws Exception {
    mockMvc
        .perform(post(USERS_SIGN_UP_ENDPOINT).contentType(APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());

    assertEquals(0, userRepository.count());
  }

  @Test
  void register_user_with_malformed_json_returns_400() throws Exception {
    var invalidJson = "{invalid json}";
    mockMvc
        .perform(post(USERS_SIGN_UP_ENDPOINT).contentType(APPLICATION_JSON).content(invalidJson))
        .andExpect(status().isBadRequest());
  }

  @Test
  void get_all_users_returns_200_and_empty_page_when_no_users() throws Exception {
    mockMvc
        .perform(get(USERS_ENDPOINT))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content").isEmpty())
        .andExpect(jsonPath("$.total_elements").value(0))
        .andExpect(jsonPath("$.total_pages").value(0))
        .andExpect(jsonPath("$.size").exists())
        .andExpect(jsonPath("$.number").exists());
  }

  @Test
  void get_all_users_returns_200_and_all_users() throws Exception {
    createTestUser(TEST_EMAIL_1, CLERK_ID_1);
    createTestUser(TEST_EMAIL_2, CLERK_ID_2);
    createTestUser(TEST_EMAIL_3, CLERK_ID_3);

    mockMvc
        .perform(get(USERS_ENDPOINT))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(3))
        .andExpect(jsonPath("$.total_elements").value(3));
  }

  @Test
  void get_all_users_with_pagination_returns_200_and_paginated_results() throws Exception {
    createTestUser(TEST_EMAIL_1, CLERK_ID_1);
    createTestUser(TEST_EMAIL_2, CLERK_ID_2);
    createTestUser(TEST_EMAIL_3, CLERK_ID_3);

    mockMvc
        .perform(get(USERS_ENDPOINT).param(PAGE_PARAM, "0").param(SIZE_PARAM, "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.total_elements").value(3))
        .andExpect(jsonPath("$.total_pages").value(2))
        .andExpect(jsonPath("$.number").value(0))
        .andExpect(jsonPath("$.size").value(2));
  }

  @Test
  void get_all_users_with_second_page_returns_200_and_correct_page() throws Exception {
    createTestUser(TEST_EMAIL_1, CLERK_ID_1);
    createTestUser(TEST_EMAIL_2, CLERK_ID_2);
    createTestUser(TEST_EMAIL_3, CLERK_ID_3);

    mockMvc
        .perform(get(USERS_ENDPOINT).param(PAGE_PARAM, "1").param(SIZE_PARAM, "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.total_elements").value(3))
        .andExpect(jsonPath("$.number").value(1));
  }

  @Test
  void get_all_users_with_only_page_param_uses_default_size() throws Exception {
    createTestUser(TEST_EMAIL_1, CLERK_ID_1);
    createTestUser(TEST_EMAIL_2, CLERK_ID_2);

    mockMvc
        .perform(get(USERS_ENDPOINT).param(PAGE_PARAM, "0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.total_elements").value(2));
  }

  @Test
  void get_all_users_with_only_size_param_uses_default_page() throws Exception {
    createTestUser(TEST_EMAIL_1, CLERK_ID_1);
    createTestUser(TEST_EMAIL_2, CLERK_ID_2);

    mockMvc
        .perform(get(USERS_ENDPOINT).param(SIZE_PARAM, "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.total_elements").value(2));
  }

  @Test
  void get_all_users_returns_users_sorted_by_created_at_descending() throws Exception {
    var user1 = createTestUser("first@example.com", CLERK_ID_1);
    sleepBriefly();
    var user2 = createTestUser("second@example.com", CLERK_ID_2);
    sleepBriefly();
    var user3 = createTestUser("third@example.com", CLERK_ID_3);

    mockMvc
        .perform(get(USERS_ENDPOINT))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].email").value(user3.email()))
        .andExpect(jsonPath("$.content[1].email").value(user2.email()))
        .andExpect(jsonPath("$.content[2].email").value(user1.email()));
  }

  @Test
  void get_all_users_with_page_beyond_results_returns_200_and_empty_content() throws Exception {
    createTestUser(TEST_EMAIL_1, CLERK_ID_1);

    mockMvc
        .perform(get(USERS_ENDPOINT).param(PAGE_PARAM, "10").param(SIZE_PARAM, "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isEmpty())
        .andExpect(jsonPath("$.total_elements").value(1));
  }

  private User createTestUser(String email, String clerkId) {
    var request = new UserRequest(email, clerkId);
    return userService.registerUser(request);
  }

  private String generateRequest(UserRequest userRequest) throws JsonProcessingException {
    return objectMapper.writeValueAsString(userRequest);
  }

  private void sleepBriefly() {
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
