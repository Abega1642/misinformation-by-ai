package dev.razafindratelo.unfaked.endpoint.rest.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class GroqApiClientTest {

  private static final String TEST_API_URL = "https://api.groq.com/openai/v1/chat/completions";
  private static final String TEST_API_KEY = "test-groq-key";
  private static final String TEST_MODEL = "llama-3.1-8b-instant";
  private static final int TEST_MAX_TOKENS = 200;
  private static final double TEST_TEMPERATURE = 0.3;
  private static final String SYSTEM_PROMPT = "You are a helpful assistant";
  private static final String USER_PROMPT = "Test prompt";

  private GroqApiClient client;

  @BeforeEach
  void setUp() {
    client = new GroqApiClient();
    ReflectionTestUtils.setField(client, "apiUrl", TEST_API_URL);
    ReflectionTestUtils.setField(client, "apiKey", TEST_API_KEY);
    ReflectionTestUtils.setField(client, "model", TEST_MODEL);
    ReflectionTestUtils.setField(client, "maxTokens", TEST_MAX_TOKENS);
    ReflectionTestUtils.setField(client, "temperature", TEST_TEMPERATURE);
  }

  @Test
  void should_throw_exception_when_system_prompt_is_null() {
    assertThrows(Exception.class, () -> client.complete(null, USER_PROMPT));
  }

  @Test
  void should_throw_exception_when_user_prompt_is_null() {
    assertThrows(Exception.class, () -> client.complete(SYSTEM_PROMPT, null));
  }

  @Test
  void should_throw_exception_when_system_prompt_is_blank() {
    assertThrows(Exception.class, () -> client.complete("   ", USER_PROMPT));
  }

  @Test
  void should_throw_exception_when_user_prompt_is_blank() {
    assertThrows(Exception.class, () -> client.complete(SYSTEM_PROMPT, "   "));
  }

  @Test
  void should_initialize_http_client() {
    // Same explanation with the SerpApiClientIT::should_build_search_parameters_correctly
    assertNotNull(client);
  }
}
