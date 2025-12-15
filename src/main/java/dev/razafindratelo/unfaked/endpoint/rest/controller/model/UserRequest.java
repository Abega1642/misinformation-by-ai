package dev.razafindratelo.unfaked.endpoint.rest.controller.model;

import dev.razafindratelo.unfaked.model.classifier.UserRole;
import dev.razafindratelo.unfaked.model.classifier.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;
import org.springframework.lang.Nullable;

@Builder
public record UserRequest(
    @Email @NotNull @NotBlank String email,
    @NotNull @NotBlank String fullName,
    @NotNull @NotBlank String clerkId,
    @Nullable @Length(min = 8, message = "Password must be at least 8 characters long")
        String password,
    @Nullable UserRole role,
    @Nullable UserStatus status) {}
