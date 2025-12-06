package dev.razafindratelo.misinformation.endpoint.rest.controller;

import dev.razafindratelo.misinformation.endpoint.rest.controller.model.LoginRequest;
import dev.razafindratelo.misinformation.endpoint.rest.controller.model.LoginResponse;
import dev.razafindratelo.misinformation.manager.AuthManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthManager authManager;

  @PostMapping("/login")
  public LoginResponse login(@RequestBody LoginRequest loginRequest) {
    return authManager.authenticate(loginRequest);
  }
}
