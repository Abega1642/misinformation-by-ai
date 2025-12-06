package dev.razafindratelo.misinformation.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record User(
    @NotNull @NotBlank String id,
    @Email @NotBlank @NotNull String email,
    @NotNull @NotBlank String clerkId,
    LocalDateTime createdAt) {}
