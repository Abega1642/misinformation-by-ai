package dev.razafindratelo.misinformation.endpoint.rest.controller.model;

import dev.razafindratelo.misinformation.model.User;
import java.time.LocalDateTime;

public record LoginResponse(String message, String email, LocalDateTime requestTime, User user) {}
