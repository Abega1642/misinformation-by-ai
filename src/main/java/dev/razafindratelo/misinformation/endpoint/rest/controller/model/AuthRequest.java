package dev.razafindratelo.misinformation.endpoint.rest.controller.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.lang.Nullable;

@Builder
public record AuthRequest(
    @Email(message = "Invalid email format") @NotNull @NotBlank String email,
    @Nullable String password,
    @Nullable String clerkId) {
  public AuthRequest {
    if ((password == null || password.isBlank()) && (clerkId == null || clerkId.isBlank())) {
      throw new IllegalArgumentException(
          "Either password or clerkId must be provided for authentication");
    }
  }
}
