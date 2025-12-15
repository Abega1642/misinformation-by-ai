package dev.razafindratelo.unfaked.endpoint.rest.client;

import static org.owasp.encoder.Encode.forJava;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.razafindratelo.unfaked.exception.GroqApiException;
import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "groq.api.key")
public class GroqApiClient {

  private static final String CONTENT_PROPERTY = "content";
  private static final Duration CONNECT_TIMEOUT_DURATION = Duration.ofSeconds(30);
  private static final int REDIRECTION_CODE = 300;
  private static final int OK_CODE = 200;
  private final HttpClient httpClient;

  @Value("${groq.api.url}")
  private String apiUrl;

  @Value("${groq.api.key}")
  private String apiKey;

  @Value("${groq.api.model}")
  private String model;

  @Value("${groq.api.max-tokens}")
  private int maxTokens;

  @Value("${groq.api.temperature}")
  private double temperature;

  public GroqApiClient() {
    this.httpClient =
        HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT_DURATION)
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
  }

  public String complete(@NotBlank String systemPrompt, @NotBlank String userPrompt) {
    log.info("Sending request to Groq API");

    JsonObject requestBody = buildRequestBody(systemPrompt, userPrompt);

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
            .timeout(CONNECT_TIMEOUT_DURATION)
            .build();

    try {
      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() < OK_CODE || response.statusCode() >= REDIRECTION_CODE) {
        log.error("Groq API request failed with status: {}", response.statusCode());
        throw new GroqApiException("Groq API returned status: " + response.statusCode());
      }

      String responseBody = response.body();
      log.debug("Groq API response received");

      return extractContent(responseBody);

    } catch (IOException ex) {
      log.error("Failed to call Groq API: {}", forJava(ex.getMessage()), ex);
      throw new GroqApiException("Failed to call Groq API", ex);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      log.error("Groq API request interrupted: {}", forJava(ex.getMessage()), ex);
      throw new GroqApiException("Groq API request was interrupted", ex);
    }
  }

  private JsonObject buildRequestBody(String systemPrompt, String userPrompt) {
    JsonObject requestBody = new JsonObject();
    requestBody.addProperty("model", model);
    requestBody.addProperty("max_tokens", maxTokens);
    requestBody.addProperty("temperature", temperature);

    JsonArray messages = new JsonArray();

    JsonObject systemMessage = new JsonObject();
    systemMessage.addProperty("role", "system");
    systemMessage.addProperty(CONTENT_PROPERTY, systemPrompt);
    messages.add(systemMessage);

    JsonObject userMessage = new JsonObject();
    userMessage.addProperty("role", "user");
    userMessage.addProperty(CONTENT_PROPERTY, userPrompt);
    messages.add(userMessage);

    requestBody.add("messages", messages);

    return requestBody;
  }

  private String extractContent(String responseBody) {
    try {
      JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

      JsonArray choices = jsonResponse.getAsJsonArray("choices");
      if (choices != null && !choices.isEmpty()) {
        JsonObject firstChoice = choices.get(0).getAsJsonObject();
        JsonObject message = firstChoice.getAsJsonObject("message");

        if (message != null && message.has(CONTENT_PROPERTY)) {
          return message.get(CONTENT_PROPERTY).getAsString();
        }
      }

      log.error("Unable to extract content from Groq response");
      throw new GroqApiException("Invalid response format from Groq API");

    } catch (Exception ex) {
      log.error("Error parsing Groq response: {}", forJava(ex.getMessage()), ex);
      throw new GroqApiException("Failed to parse Groq response", ex);
    }
  }
}
