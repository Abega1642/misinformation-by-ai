package dev.razafindratelo.unfaked.service.util;

import static org.owasp.encoder.Encode.forJava;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.razafindratelo.unfaked.model.classifier.ProofType;
import dev.razafindratelo.unfaked.model.detection.Proof;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SearchResultParser {

  public String extractJudgment(JsonObject results) {
    try {
      if (results.has("answer")) {
        return results.get("answer").getAsString();
      }

      if (results.has("text_blocks")) {
        JsonArray textBlocks = results.getAsJsonArray("text_blocks");
        if (!textBlocks.isEmpty()) {
          JsonObject firstBlock = textBlocks.get(0).getAsJsonObject();
          if (firstBlock.has("text")) {
            return firstBlock.get("text").getAsString();
          }
        }
      }

      log.warn("No judgment found in search results");
      return "Unable to determine a conclusive answer based on available information.";
    } catch (Exception ex) {
      log.error("Error extracting judgment: {}", forJava(ex.getMessage()), ex);
      return "Error processing search results.";
    }
  }

  public List<Proof> extractProofs(JsonObject results) {
    List<Proof> proofs = new ArrayList<>();

    try {
      if (results.has("sources")) {
        JsonArray sources = results.getAsJsonArray("sources");
        proofs.addAll(parseSourcesArray(sources));
      }

      if (results.has("organic_results")) {
        JsonArray organicResults = results.getAsJsonArray("organic_results");
        proofs.addAll(parseOrganicResults(organicResults));
      }

      log.info("Extracted {} proofs from search results", proofs.size());
    } catch (Exception ex) {
      log.error("Error extracting proofs: {}", forJava(ex.getMessage()), ex);
    }

    return proofs;
  }

  private List<Proof> parseSourcesArray(JsonArray sources) {
    List<Proof> proofs = new ArrayList<>();

    for (JsonElement element : sources) {
      JsonObject source = element.getAsJsonObject();

      String title = getStringValue(source, "title", "Untitled Source");
      String url = getStringValue(source, "link", "");
      String snippet = getStringValue(source, "snippet", "");

      if (!url.isEmpty()) {
        proofs.add(
            Proof.builder().title(title).url(url).snippet(snippet).type(ProofType.NEUTRAL).build());
      }
    }

    return proofs;
  }

  private List<Proof> parseOrganicResults(JsonArray organicResults) {
    List<Proof> proofs = new ArrayList<>();

    for (JsonElement element : organicResults) {
      JsonObject result = element.getAsJsonObject();

      String title = getStringValue(result, "title", "Untitled Result");
      String url = getStringValue(result, "link", "");
      String snippet = getStringValue(result, "snippet", "");

      if (!url.isEmpty()) {
        proofs.add(
            Proof.builder().title(title).url(url).snippet(snippet).type(ProofType.NEUTRAL).build());
      }
    }

    return proofs;
  }

  private String getStringValue(JsonObject obj, String key, String defaultValue) {
    if (obj.has(key) && !obj.get(key).isJsonNull()) {
      return obj.get(key).getAsString();
    }
    return defaultValue;
  }
}
