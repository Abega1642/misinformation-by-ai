package dev.razafindratelo.unfaked.service.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.razafindratelo.unfaked.model.classifier.ProofType;
import dev.razafindratelo.unfaked.model.detection.Proof;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SearchResultParserTest {

  private static final String SAMPLE_TITLE = "Sample Article Title";
  private static final String SAMPLE_URL = "https://example.com/article";
  private static final String SAMPLE_SNIPPET = "This is a sample snippet from the article.";
  private static final String ANSWER_FIELD = "answer";
  private static final String TEXT_BLOCKS_FIELD = "text_blocks";
  private static final String TEXT_FIELD = "text";
  private static final String SOURCES_FIELD = "sources";
  private static final String ORGANIC_RESULTS_FIELD = "organic_results";
  private static final String TITLE_FIELD = "title";
  private static final String LINK_FIELD = "link";
  private static final String SNIPPET_FIELD = "snippet";

  private SearchResultParser parser;

  @BeforeEach
  void setUp() {
    parser = new SearchResultParser();
  }

  @Test
  void should_extract_judgment_from_answer_field() {
    var results = new JsonObject();
    String expectedJudgment = "This is the answer to the query";
    results.addProperty(ANSWER_FIELD, expectedJudgment);

    String actualJudgment = parser.extractJudgment(results);

    assertEquals(expectedJudgment, actualJudgment);
  }

  @Test
  void should_extract_judgment_from_text_blocks_when_answer_not_present() {
    var results = new JsonObject();
    var textBlocks = new JsonArray();
    var firstBlock = new JsonObject();
    String expectedJudgment = "Text from first block";
    firstBlock.addProperty(TEXT_FIELD, expectedJudgment);
    textBlocks.add(firstBlock);
    results.add(TEXT_BLOCKS_FIELD, textBlocks);

    String actualJudgment = parser.extractJudgment(results);

    assertEquals(expectedJudgment, actualJudgment);
  }

  @Test
  void should_return_default_message_when_no_judgment_found() {
    var results = new JsonObject();

    String actualJudgment = parser.extractJudgment(results);

    assertEquals(
        "Unable to determine a conclusive answer based on available information.", actualJudgment);
  }

  @Test
  void should_extract_proofs_from_sources_field() {
    var results = new JsonObject();
    var sources = new JsonArray();
    var source = createSourceObject(SAMPLE_TITLE, SAMPLE_URL, SAMPLE_SNIPPET);
    sources.add(source);
    results.add(SOURCES_FIELD, sources);

    List<Proof> proofs = parser.extractProofs(results);

    assertEquals(1, proofs.size());
    var proof = proofs.getFirst();
    assertEquals(SAMPLE_TITLE, proof.getTitle());
    assertEquals(SAMPLE_URL, proof.getUrl());
    assertEquals(SAMPLE_SNIPPET, proof.getSnippet());
    assertEquals(ProofType.NEUTRAL, proof.getType());
  }

  @Test
  void should_extract_proofs_from_organic_results_field() {
    var results = new JsonObject();
    var organicResults = new JsonArray();
    var result = createSourceObject(SAMPLE_TITLE, SAMPLE_URL, SAMPLE_SNIPPET);
    organicResults.add(result);
    results.add(ORGANIC_RESULTS_FIELD, organicResults);

    List<Proof> proofs = parser.extractProofs(results);

    assertEquals(1, proofs.size());
    assertEquals(SAMPLE_TITLE, proofs.getFirst().getTitle());
  }

  @Test
  void should_skip_proofs_without_url() {
    var results = new JsonObject();
    var sources = new JsonArray();
    var sourceWithoutUrl = new JsonObject();
    sourceWithoutUrl.addProperty(TITLE_FIELD, SAMPLE_TITLE);
    sourceWithoutUrl.addProperty(SNIPPET_FIELD, SAMPLE_SNIPPET);
    sources.add(sourceWithoutUrl);
    results.add(SOURCES_FIELD, sources);

    List<Proof> proofs = parser.extractProofs(results);

    assertEquals(0, proofs.size());
  }

  @Test
  void should_extract_multiple_proofs() {
    var results = new JsonObject();
    var sources = new JsonArray();
    sources.add(createSourceObject("Title 1", "https://example1.com", "Snippet 1"));
    sources.add(createSourceObject("Title 2", "https://example2.com", "Snippet 2"));
    sources.add(createSourceObject("Title 3", "https://example3.com", "Snippet 3"));
    results.add(SOURCES_FIELD, sources);

    List<Proof> proofs = parser.extractProofs(results);

    assertEquals(3, proofs.size());
  }

  @Test
  void should_return_empty_list_when_no_proofs_found() {
    var results = new JsonObject();

    List<Proof> proofs = parser.extractProofs(results);

    assertNotNull(proofs);
    assertTrue(proofs.isEmpty());
  }

  @Test
  void should_handle_missing_title_with_default_value() {
    var results = new JsonObject();
    var sources = new JsonArray();
    var source = new JsonObject();
    source.addProperty(LINK_FIELD, SAMPLE_URL);
    sources.add(source);
    results.add(SOURCES_FIELD, sources);

    List<Proof> proofs = parser.extractProofs(results);

    assertEquals(1, proofs.size());
    assertEquals("Untitled Source", proofs.getFirst().getTitle());
  }

  @Test
  void should_combine_sources_and_organic_results() {
    var results = new JsonObject();

    var sources = new JsonArray();
    sources.add(createSourceObject("Source Title", "https://source.com", "Source snippet"));
    results.add(SOURCES_FIELD, sources);

    var organicResults = new JsonArray();
    organicResults.add(
        createSourceObject("Organic Title", "https://organic.com", "Organic snippet"));
    results.add(ORGANIC_RESULTS_FIELD, organicResults);

    List<Proof> proofs = parser.extractProofs(results);

    assertEquals(2, proofs.size());
  }

  private JsonObject createSourceObject(String title, String url, String snippet) {
    var source = new JsonObject();
    source.addProperty(TITLE_FIELD, title);
    source.addProperty(LINK_FIELD, url);
    source.addProperty(SNIPPET_FIELD, snippet);
    return source;
  }
}
