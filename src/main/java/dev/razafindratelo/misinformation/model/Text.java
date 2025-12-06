package dev.razafindratelo.misinformation.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record Text(
    @NotNull @NotBlank String id,
    @NotNull User owner,
    @NotNull @NotBlank String value,
    LocalDateTime createdAt) {}
