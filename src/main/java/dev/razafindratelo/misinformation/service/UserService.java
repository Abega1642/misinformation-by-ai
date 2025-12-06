package dev.razafindratelo.misinformation.service;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;

import dev.razafindratelo.misinformation.endpoint.rest.controller.model.UserRequest;
import dev.razafindratelo.misinformation.mapper.UserMapper;
import dev.razafindratelo.misinformation.model.User;
import dev.razafindratelo.misinformation.repository.UserRepository;
import dev.razafindratelo.misinformation.repository.model.JUser;
import dev.razafindratelo.misinformation.service.util.Paginator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class UserService {
  private final UserMapper userMapper;
  private final Paginator paginator;
  private final UserRepository userRepository;

  public User authenticateUser(@NotNull @NotBlank String clerkId, @Email @NotBlank String email) {
    var jUser =
        userRepository
            .findByEmailAndClerkId(email, clerkId)
            .orElseThrow(
                () ->
                    new AuthorizationDeniedException(
                        format("Authorization denied from email=%s", email)));

    return userMapper.toCoreModel(jUser);
  }

  public User registerUser(@Valid @NotNull UserRequest userRequest) {
    var id = randomUUID().toString();
    var createdAt = now();
    var newUser = new JUser(id, userRequest.email(), userRequest.clerkId(), createdAt);

    return userMapper.toCoreModel(userRepository.save(newUser));
  }

  public User findUserByEmail(@Email @NotNull @NotBlank String email) {
    var jUser =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () -> new EntityNotFoundException(format("No user with email=%s.", email)));

    return userMapper.toCoreModel(jUser);
  }

  public Page<User> getAllUsers(Integer page, Integer size) {
    var pagination = paginator.apply(page, size);

    Pageable pageable =
        PageRequest.of(
            pagination.get("page"), pagination.get("size"), Sort.by("createdAt").descending());

    var results = userRepository.findAll(pageable);

    return results.map(userMapper::toCoreModel);
  }
}
