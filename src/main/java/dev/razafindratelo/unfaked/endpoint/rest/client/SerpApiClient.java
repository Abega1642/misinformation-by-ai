package dev.razafindratelo.unfaked.endpoint.rest.client;

import static org.owasp.encoder.Encode.forJava;

import com.google.gson.JsonObject;
import dev.razafindratelo.unfaked.exception.SearchException;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import serpapi.GoogleSearch;
import serpapi.SerpApiSearchException;

@Slf4j
@Component
@ConditionalOnProperty(name = "google.api-key")
public class SerpApiClient {

  @Value("${google.api}")
  private String apiBaseUrl;

  @Value("${google.api-key}")
  private String apiKey;

  @Value("${google.api-engine:google_ai_mode}")
  private String searchEngine;

  public JsonObject search(@NotBlank String query) {
    log.info("Executing search for query: {}", forJava(query));

    Map<String, String> parameters = buildSearchParameters(query);
    GoogleSearch search = new GoogleSearch(parameters);

    try {
      JsonObject results = search.getJson();
      log.info("Search completed successfully for query: {}", forJava(query));
      return results;
    } catch (SerpApiSearchException ex) {
      log.error(
          "SerpAPI search failed for query: {}. Error: {}",
          forJava(query),
          forJava(ex.getMessage()),
          ex);
      throw new SearchException("Failed to execute search", ex);
    }
  }

  private Map<String, String> buildSearchParameters(String query) {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("engine", searchEngine);
    parameters.put("q", query);
    parameters.put("api_key", apiKey);
    return parameters;
  }
}
