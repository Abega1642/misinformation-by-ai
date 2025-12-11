package dev.razafindratelo.unfaked.model.detection;

import dev.razafindratelo.unfaked.model.classifier.ProofType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
public class Proof {
  @NotBlank private String title;

  @NotBlank private String url;

  private String snippet;

  @NotNull private ProofType type;
}
