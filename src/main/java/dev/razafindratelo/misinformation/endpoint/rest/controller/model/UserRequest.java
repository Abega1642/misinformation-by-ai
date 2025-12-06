package dev.razafindratelo.misinformation.endpoint.rest.controller.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRequest(
    @Email @NotNull @NotBlank String email,
    @NotNull @NotBlank String fullName,
    @NotNull @NotBlank String clerkId) {}
