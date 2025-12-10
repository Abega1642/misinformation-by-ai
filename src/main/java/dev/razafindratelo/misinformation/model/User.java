package dev.razafindratelo.misinformation.model;

import dev.razafindratelo.misinformation.model.classifier.UserRole;
import dev.razafindratelo.misinformation.model.classifier.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class User {
  @NotNull @NotBlank private String id;
  @Email @NotBlank @NotNull private String email;
  @NotNull @NotBlank private String fullName;
  @NotNull @NotBlank private String clerkId;
  @NotNull private UserRole role;
  @NotNull private UserStatus status;
  private LocalDateTime updatedAt;
  @NotNull private LocalDateTime createdAt;
}
