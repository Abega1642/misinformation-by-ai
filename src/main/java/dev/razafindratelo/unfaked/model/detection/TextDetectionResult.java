package dev.razafindratelo.unfaked.model.detection;

import dev.razafindratelo.unfaked.model.Text;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
public class TextDetectionResult {
  @NotNull private final Text text;

  @NotNull private final String judgment;
  private final LocalDateTime detectedAt;
  private final String searchQuery;
  @NotNull private List<Proof> proofs;
}
