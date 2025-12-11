package dev.razafindratelo.unfaked.service.util;

import static org.owasp.encoder.Encode.forJava;

import dev.razafindratelo.unfaked.endpoint.rest.client.GroqApiClient;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueryFormatter {

  private static final String SYSTEM_PROMPT =
      """
      You are an expert at transforming user statements and questions into optimized fact-checking search queries.

      Your task:
      1. Analyze the user's input (claim, statement, or question)
      2. Transform it into a clear, neutral, fact-checking query
      3. The query should be designed to retrieve evidence-based, verifiable information
      4. Include multiple angles: supporting evidence, refuting evidence, and neutral facts

      Guidelines:
      - Convert statements into questions (e.g., "Elon Musk is broke" → "What is Elon Musk's current net worth? Is there evidence of financial difficulties?")
      - Keep queries concise but comprehensive (aim for 10-25 words)
      - Use neutral, objective language
      - Include terms like "evidence", "facts", "current status", "verified information"
      - For controversial claims, ask for both supporting and opposing evidence
      - Avoid biased framing

      Output ONLY the optimized search query, nothing else. No explanations, no preamble.
      """;

  private final GroqApiClient groqApiClient;

  public String format(@NotBlank String userInput) {
    log.info("Formatting query for user input: {}", forJava(userInput));

    String sanitizedInput = sanitizeInput(userInput);

    try {
      String optimizedQuery = groqApiClient.complete(SYSTEM_PROMPT, sanitizedInput);
      String cleanedQuery = cleanQuery(optimizedQuery);

      log.info(
          "Original input: {} | Optimized query: {}",
          forJava(sanitizedInput),
          forJava(cleanedQuery));

      return cleanedQuery;

    } catch (Exception ex) {
      log.error(
          "Query formatting failed, falling back to sanitized input: {}",
          forJava(ex.getMessage()),
          ex);
      return fallbackFormat(sanitizedInput);
    }
  }

  private String sanitizeInput(String input) {
    if (input == null || input.isBlank()) {
      throw new IllegalArgumentException("Input cannot be null or blank");
    }

    return input.trim().replaceAll("\\s+", " ").replaceAll("[\\r\\n]+", " ");
  }

  private String cleanQuery(String query) {
    return query.trim().replaceAll("^[\"']+|[\"']+$", "").replaceAll("\\s+", " ");
  }

  private String fallbackFormat(String input) {
    log.warn("Using fallback formatting for input: {}", forJava(input));

    if (input.endsWith("?")) return input + " What is the evidence?";

    return "Is it true that " + input + "? What does the evidence say?";
  }
}
