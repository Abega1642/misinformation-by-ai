package dev.razafindratelo.misinformation.service;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;
import static org.owasp.encoder.Encode.forJava;

import dev.razafindratelo.misinformation.endpoint.rest.controller.model.UserRequest;
import dev.razafindratelo.misinformation.exception.InvalidUserDataException;
import dev.razafindratelo.misinformation.exception.ResourceDuplicatedException;
import dev.razafindratelo.misinformation.exception.UserRegistrationException;
import dev.razafindratelo.misinformation.mapper.UserMapper;
import dev.razafindratelo.misinformation.model.User;
import dev.razafindratelo.misinformation.model.classifier.UserRole;
import dev.razafindratelo.misinformation.model.classifier.UserStatus;
import dev.razafindratelo.misinformation.repository.UserRepository;
import dev.razafindratelo.misinformation.repository.model.JUser;
import dev.razafindratelo.misinformation.service.util.Paginator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Slf4j
@Validated
@RequiredArgsConstructor
public class UserService {
  private final UserMapper userMapper;
  private final Paginator paginator;
  private final UserRepository userRepository;

  public User findByClerkId(@NotNull @NotBlank String id) {
    var jUser =
        userRepository
            .findByClerkId(id)
            .orElseThrow(
                () -> new EntityNotFoundException(format("User with id %s not found", id)));

    log.info("Found user by clerk_id: {}", forJava(id));
    return userMapper.toCoreModel(jUser);
  }

  public User findByEmail(@Email @NotBlank @NotNull String email) {
    var jUser =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () -> new EntityNotFoundException(format("User with email=%s not found", email)));

    log.info("Found user by email: {}", forJava(email));
    return userMapper.toCoreModel(jUser);
  }

  public User authenticateWithClerkId(
      @Email @NotBlank String email, @NotNull @NotBlank String clerkId) {
    var jUser =
        userRepository
            .findByEmailAndClerkId(email, clerkId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        format("User with email=%s and clerk_id=%s not found", email, clerkId)));

    log.info("Found user by email and clerk_id - email={}", forJava(email));
    return userMapper.toCoreModel(jUser);
  }

  public User registerUser(@Valid @NotNull UserRequest userRequest) {
    log.info(
        "Starting user registration - email={}, clerk_id={}",
        userRequest.email(),
        userRequest.clerkId());

    validateUserRequest(userRequest);

    var id = randomUUID().toString();
    var createdAt = now();

    var newUser =
        JUser.builder()
            .id(id)
            .email(userRequest.email())
            .fullName(userRequest.fullName())
            .clerkId(userRequest.clerkId())
            .password(userRequest.password())
            .role(userRequest.role() != null ? userRequest.role() : UserRole.USER)
            .status(userRequest.status() != null ? userRequest.status() : UserStatus.INACTIVE)
            .isEmailVerified(userRequest.password() == null)
            .createdAt(createdAt)
            .build();

    log.debug(
        "Created user entity - id={}, role={}, status={}, is_email_verified={}",
        newUser.getId(),
        newUser.getRole(),
        newUser.getStatus(),
        newUser.isEmailVerified());

    try {
      var savedUser = userRepository.save(newUser);
      log.info(
          "Successfully registered user - id={}, email={}, role={}",
          savedUser.getId(),
          savedUser.getEmail(),
          savedUser.getRole());
      return userMapper.toCoreModel(savedUser);
    } catch (DataIntegrityViolationException e) {
      log.error(
          "Duplicate user detected - email={}, clerk_id={}",
          userRequest.email(),
          userRequest.clerkId());
      throw new ResourceDuplicatedException(
          format(
              "User with email '%s' or clerk_id '%s' already exists",
              userRequest.email(), userRequest.clerkId()),
          e);
    } catch (Exception e) {
      log.error(
          "Failed to register user - email={}, clerk_id={}, error={}",
          userRequest.email(),
          userRequest.clerkId(),
          e.getMessage(),
          e);
      throw new UserRegistrationException(
          format(
              "Failed to register user with email '%s'. Please try again later.",
              userRequest.email()),
          e);
    }
  }

  private void validateUserRequest(UserRequest userRequest) {
    if (userRepository.existsByEmail(userRequest.email())) {
      log.warn("Registration attempt with existing email - email={}", userRequest.email());
      throw new ResourceDuplicatedException(
          format("User with email '%s' already exists", userRequest.email()));
    }

    if (userRepository.existsByClerkId(userRequest.clerkId())) {
      log.warn("Registration attempt with existing clerk_id - clerk_id={}", userRequest.clerkId());
      throw new ResourceDuplicatedException(
          format("User with clerk_id '%s' already exists", userRequest.clerkId()));
    }

    if (userRequest.password() != null && !userRequest.password().isBlank())
      if (userRequest.password().length() < 8)
        throw new InvalidUserDataException("Password must be at least 8 characters long");
  }

  public Page<User> getAllUsers(Integer page, Integer size) {
    log.info("Requesting all users with page={} and size={}", page, size);
    var pagination = paginator.apply(page, size);

    Pageable pageable =
        PageRequest.of(
            pagination.get("page"), pagination.get("size"), Sort.by("createdAt").descending());

    var results = userRepository.findAll(pageable);
    return results.map(userMapper::toCoreModel);
  }
}
