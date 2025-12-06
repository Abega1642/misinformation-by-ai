package dev.razafindratelo.misinformation.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record Token(
    @NotNull @NotBlank String id,
    @NotNull User owner,
    @NotNull @NotBlank String token,
    LocalDate expirationDate) {}
