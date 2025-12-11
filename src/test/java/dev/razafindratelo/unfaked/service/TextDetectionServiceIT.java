package dev.razafindratelo.unfaked.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.google.gson.JsonObject;
import dev.razafindratelo.unfaked.endpoint.rest.client.SerpApiClient;
import dev.razafindratelo.unfaked.model.Text;
import dev.razafindratelo.unfaked.model.User;
import dev.razafindratelo.unfaked.model.classifier.ProofType;
import dev.razafindratelo.unfaked.model.detection.Proof;
import dev.razafindratelo.unfaked.model.detection.TextDetectionResult;
import dev.razafindratelo.unfaked.service.util.QueryFormatter;
import dev.razafindratelo.unfaked.service.util.SearchResultParser;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TextDetectionServiceIT {

  private static final String TEXT_ID = "text-123";
  private static final String TEXT_VALUE = "Elon Musk is broke";
  private static final String OPTIMIZED_QUERY = "What is Elon Musk's current net worth?";
  private static final String JUDGMENT = "Based on available evidence, Elon Musk is not broke.";
  private static final String PROOF_TITLE = "Forbes: Elon Musk Net Worth";
  private static final String PROOF_URL = "https://forbes.com/elon-musk";
  private static final String PROOF_SNIPPET = "Elon Musk's net worth is estimated at $200 billion.";

  @Mock private QueryFormatter queryFormatter;

  @Mock private SerpApiClient serpApiClient;

  @Mock private SearchResultParser searchResultParser;

  @Mock private User mockUser;

  private TextDetectionService service;

  @BeforeEach
  void setUp() {
    service = new TextDetectionService(queryFormatter, serpApiClient, searchResultParser);
  }

  @Test
  void should_detect_text_successfully() {
    var text = createSampleText();
    var searchResults = new JsonObject();
    var proofs = createSampleProofs();

    when(queryFormatter.format(TEXT_VALUE)).thenReturn(OPTIMIZED_QUERY);
    when(serpApiClient.search(OPTIMIZED_QUERY)).thenReturn(searchResults);
    when(searchResultParser.extractJudgment(searchResults)).thenReturn(JUDGMENT);
    when(searchResultParser.extractProofs(searchResults)).thenReturn(proofs);

    TextDetectionResult result = service.detect(text);

    assertNotNull(result);
    assertEquals(text, result.getText());
    assertEquals(JUDGMENT, result.getJudgment());
    assertEquals(OPTIMIZED_QUERY, result.getSearchQuery());
    assertEquals(2, result.getProofs().size());
    assertNotNull(result.getDetectedAt());

    verify(queryFormatter, times(1)).format(TEXT_VALUE);
    verify(serpApiClient, times(1)).search(OPTIMIZED_QUERY);
    verify(searchResultParser, times(1)).extractJudgment(searchResults);
    verify(searchResultParser, times(1)).extractProofs(searchResults);
  }

  @Test
  void should_handle_empty_proofs_list() {
    var text = createSampleText();
    var searchResults = new JsonObject();

    when(queryFormatter.format(TEXT_VALUE)).thenReturn(OPTIMIZED_QUERY);
    when(serpApiClient.search(OPTIMIZED_QUERY)).thenReturn(searchResults);
    when(searchResultParser.extractJudgment(searchResults)).thenReturn(JUDGMENT);
    when(searchResultParser.extractProofs(searchResults)).thenReturn(Collections.emptyList());

    TextDetectionResult result = service.detect(text);

    assertNotNull(result);
    assertEquals(0, result.getProofs().size());
  }

  @Test
  void should_throw_exception_when_text_is_null() {
    assertThrows(Exception.class, () -> service.detect(null));

    verify(queryFormatter, never()).format(anyString());
    verify(serpApiClient, never()).search(anyString());
  }

  @Test
  void should_propagate_query_formatter_exception() {
    var text = createSampleText();
    when(queryFormatter.format(TEXT_VALUE)).thenThrow(new RuntimeException("Formatting error"));

    assertThrows(RuntimeException.class, () -> service.detect(text));

    verify(serpApiClient, never()).search(anyString());
    verify(searchResultParser, never()).extractJudgment(any());
  }

  @Test
  void should_propagate_serp_api_exception() {
    var text = createSampleText();
    when(queryFormatter.format(TEXT_VALUE)).thenReturn(OPTIMIZED_QUERY);
    when(serpApiClient.search(OPTIMIZED_QUERY)).thenThrow(new RuntimeException("API error"));

    assertThrows(RuntimeException.class, () -> service.detect(text));

    verify(searchResultParser, never()).extractJudgment(any());
    verify(searchResultParser, never()).extractProofs(any());
  }

  @Test
  void should_set_detected_at_timestamp() {
    var text = createSampleText();
    var searchResults = new JsonObject();
    var beforeDetection = LocalDateTime.now();

    when(queryFormatter.format(TEXT_VALUE)).thenReturn(OPTIMIZED_QUERY);
    when(serpApiClient.search(OPTIMIZED_QUERY)).thenReturn(searchResults);
    when(searchResultParser.extractJudgment(searchResults)).thenReturn(JUDGMENT);
    when(searchResultParser.extractProofs(searchResults)).thenReturn(Collections.emptyList());

    TextDetectionResult result = service.detect(text);
    var afterDetection = LocalDateTime.now();

    assertNotNull(result.getDetectedAt());
    assertTrue(
        result.getDetectedAt().isAfter(beforeDetection)
            || result.getDetectedAt().isEqual(beforeDetection));
    assertTrue(
        result.getDetectedAt().isBefore(afterDetection)
            || result.getDetectedAt().isEqual(afterDetection));
  }

  @Test
  void should_include_original_text_in_result() {
    var text = createSampleText();
    var searchResults = new JsonObject();

    when(queryFormatter.format(TEXT_VALUE)).thenReturn(OPTIMIZED_QUERY);
    when(serpApiClient.search(OPTIMIZED_QUERY)).thenReturn(searchResults);
    when(searchResultParser.extractJudgment(searchResults)).thenReturn(JUDGMENT);
    when(searchResultParser.extractProofs(searchResults)).thenReturn(Collections.emptyList());

    TextDetectionResult result = service.detect(text);

    assertEquals(TEXT_ID, result.getText().id());
    assertEquals(TEXT_VALUE, result.getText().value());
  }

  private Text createSampleText() {
    return new Text(TEXT_ID, mockUser, TEXT_VALUE, LocalDateTime.now());
  }

  private List<Proof> createSampleProofs() {
    var proof1 =
        Proof.builder()
            .title(PROOF_TITLE)
            .url(PROOF_URL)
            .snippet(PROOF_SNIPPET)
            .type(ProofType.NEUTRAL)
            .build();

    var proof2 =
        Proof.builder()
            .title("Another Source")
            .url("https://example.com")
            .snippet("Additional information")
            .type(ProofType.NEUTRAL)
            .build();

    return Arrays.asList(proof1, proof2);
  }
}
