package dev.razafindratelo.unfaked.endpoint.rest.controller.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record TokenRequest(
    @NotNull @NotBlank String clerkId, @NotNull @NotBlank String token, LocalDate expirationDate) {}
