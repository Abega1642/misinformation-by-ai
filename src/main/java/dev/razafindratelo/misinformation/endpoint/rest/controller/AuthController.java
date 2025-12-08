package dev.razafindratelo.misinformation.endpoint.rest.controller;

import dev.razafindratelo.misinformation.endpoint.rest.controller.model.AuthRequest;
import dev.razafindratelo.misinformation.manager.AuthManager;
import dev.razafindratelo.misinformation.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthManager authManager;

  @PostMapping("/login")
  public User login(@Valid @RequestBody AuthRequest loginRequest) {
    return authManager.authenticate(loginRequest);
  }
}
