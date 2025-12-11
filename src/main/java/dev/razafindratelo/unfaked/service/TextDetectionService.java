package dev.razafindratelo.unfaked.service;

import static org.owasp.encoder.Encode.forJava;

import com.google.gson.JsonObject;
import dev.razafindratelo.unfaked.endpoint.rest.client.SerpApiClient;
import dev.razafindratelo.unfaked.model.Text;
import dev.razafindratelo.unfaked.model.detection.Proof;
import dev.razafindratelo.unfaked.model.detection.TextDetectionResult;
import dev.razafindratelo.unfaked.service.util.QueryFormatter;
import dev.razafindratelo.unfaked.service.util.SearchResultParser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class TextDetectionService {

  private final QueryFormatter queryFormatter;
  private final SerpApiClient serpApiClient;
  private final SearchResultParser searchResultParser;

  public TextDetectionResult detect(@NotNull @Valid Text text) {
    log.info("Starting text detection for text ID: {}", forJava(text.id()));

    String optimizedQuery = queryFormatter.format(text.value());
    log.info("Query optimized: {}", forJava(optimizedQuery));

    JsonObject searchResults = serpApiClient.search(optimizedQuery);

    String judgment = searchResultParser.extractJudgment(searchResults);
    List<Proof> proofs = searchResultParser.extractProofs(searchResults);

    TextDetectionResult result =
        TextDetectionResult.builder()
            .text(text)
            .judgment(judgment)
            .proofs(proofs)
            .detectedAt(LocalDateTime.now())
            .searchQuery(optimizedQuery)
            .build();

    log.info(
        "Text detection completed for text ID: {}. Query: {} | Proofs found: {}",
        forJava(text.id()),
        forJava(optimizedQuery),
        proofs.size());

    return result;
  }
}
