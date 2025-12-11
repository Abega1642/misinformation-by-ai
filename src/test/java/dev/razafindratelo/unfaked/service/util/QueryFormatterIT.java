package dev.razafindratelo.unfaked.service.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.razafindratelo.unfaked.endpoint.rest.client.GroqApiClient;
import dev.razafindratelo.unfaked.exception.GroqApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QueryFormatterIT {

  private static final String SAMPLE_INPUT = "Elon Musk is broke";
  private static final String OPTIMIZED_QUERY =
      "What is Elon Musk's current net worth? Is there evidence of financial difficulties?";
  private static final String QUESTION_INPUT = "Is it true that mermaids exist?";

  @Mock private GroqApiClient groqApiClient;

  private QueryFormatter formatter;

  @BeforeEach
  void setUp() {
    formatter = new QueryFormatter(groqApiClient);
  }

  @Test
  void should_format_query_successfully() {
    when(groqApiClient.complete(anyString(), eq(SAMPLE_INPUT))).thenReturn(OPTIMIZED_QUERY);

    String result = formatter.format(SAMPLE_INPUT);

    assertEquals(OPTIMIZED_QUERY, result);
    verify(groqApiClient, times(1)).complete(anyString(), eq(SAMPLE_INPUT));
  }

  @Test
  void should_remove_leading_and_trailing_quotes() {
    String queryWithQuotes = "\"" + OPTIMIZED_QUERY + "\"";
    when(groqApiClient.complete(anyString(), eq(SAMPLE_INPUT))).thenReturn(queryWithQuotes);

    String result = formatter.format(SAMPLE_INPUT);

    assertEquals(OPTIMIZED_QUERY, result);
  }

  @Test
  void should_sanitize_input_with_extra_whitespace() {
    String inputWithWhitespace = "  Elon  Musk   is   broke  ";
    when(groqApiClient.complete(anyString(), eq("Elon Musk is broke"))).thenReturn(OPTIMIZED_QUERY);

    String result = formatter.format(inputWithWhitespace);

    assertEquals(OPTIMIZED_QUERY, result);
  }

  @Test
  void should_sanitize_input_with_newlines() {
    String inputWithNewlines = "Elon Musk\nis\nbroke";
    when(groqApiClient.complete(anyString(), eq("Elon Musk is broke"))).thenReturn(OPTIMIZED_QUERY);

    String result = formatter.format(inputWithNewlines);

    assertEquals(OPTIMIZED_QUERY, result);
  }

  @Test
  void should_throw_exception_when_input_is_null() {
    assertThrows(IllegalArgumentException.class, () -> formatter.format(null));
    verify(groqApiClient, never()).complete(anyString(), anyString());
  }

  @Test
  void should_throw_exception_when_input_is_blank() {
    assertThrows(IllegalArgumentException.class, () -> formatter.format("   "));
    verify(groqApiClient, never()).complete(anyString(), anyString());
  }

  @Test
  void should_use_fallback_when_groq_api_fails() {
    when(groqApiClient.complete(anyString(), eq(SAMPLE_INPUT)))
        .thenThrow(new GroqApiException("API Error"));

    String result = formatter.format(SAMPLE_INPUT);

    assertNotNull(result);
    assertTrue(result.contains("Is it true that"));
    assertTrue(result.contains("evidence"));
  }

  @Test
  void should_use_fallback_for_question_input() {
    when(groqApiClient.complete(anyString(), eq(QUESTION_INPUT)))
        .thenThrow(new GroqApiException("API Error"));

    String result = formatter.format(QUESTION_INPUT);

    assertNotNull(result);
    assertTrue(result.contains("evidence"));
  }

  @Test
  void should_handle_runtime_exception_with_fallback() {
    when(groqApiClient.complete(anyString(), anyString()))
        .thenThrow(new RuntimeException("Unexpected error"));

    String result = formatter.format(SAMPLE_INPUT);

    assertNotNull(result);
    assertFalse(result.isEmpty());
  }
}
