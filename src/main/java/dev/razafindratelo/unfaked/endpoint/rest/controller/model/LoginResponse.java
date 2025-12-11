package dev.razafindratelo.unfaked.endpoint.rest.controller.model;

import dev.razafindratelo.unfaked.model.User;
import java.time.LocalDateTime;

public record LoginResponse(String message, String email, LocalDateTime requestTime, User user) {}
