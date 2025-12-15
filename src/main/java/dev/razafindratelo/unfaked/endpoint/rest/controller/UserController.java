package dev.razafindratelo.unfaked.endpoint.rest.controller;

import dev.razafindratelo.unfaked.endpoint.rest.controller.model.UserRequest;
import dev.razafindratelo.unfaked.model.User;
import dev.razafindratelo.unfaked.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @GetMapping
  public Page<User> getAllUsers(
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "size", required = false) Integer size) {
    return userService.getAllUsers(page, size);
  }

  @PostMapping("/sign-up")
  public User registerUser(@RequestBody UserRequest user) {
    return userService.registerUser(user);
  }
}
