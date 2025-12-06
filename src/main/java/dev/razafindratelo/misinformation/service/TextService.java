package dev.razafindratelo.misinformation.service;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;

import dev.razafindratelo.misinformation.endpoint.rest.controller.model.TextRequest;
import dev.razafindratelo.misinformation.mapper.TextMapper;
import dev.razafindratelo.misinformation.model.Text;
import dev.razafindratelo.misinformation.repository.TextRepository;
import dev.razafindratelo.misinformation.service.util.Paginator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Slf4j
@Validated
@RequiredArgsConstructor
public class TextService {
  private final UserService userService;
  private final Paginator paginator;
  private final TextMapper textMapper;
  private final TextRepository textRepository;

  public Text uploadText(@NotNull TextRequest request) {

    var owner = userService.findByEmail(request.email());
    log.info("Request Text upload by user with email={}", owner.email());

    var id = randomUUID().toString();
    var creationTime = now();
    var textRequest = new Text(id, owner, request.text(), creationTime);

    var createdTextInstance = textRepository.save(textMapper.toPersistenceModel(textRequest));

    return textMapper.toCoreModel(createdTextInstance);
  }

  public Text findTextInstance(
      @NotNull @NotBlank String id, @Email @NotBlank @NotNull String email) {
    var jText =
        textRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("No text instance found with id=" + id));

    if (!jText.getOwner().getEmail().equals(email))
      throw new AuthorizationDeniedException(
          format("Email %s cannot access text instance of another user", email));

    log.info("Get text instance with id={}", jText.getId());
    return textMapper.toCoreModel(jText);
  }

  public Page<Text> findAllByEmail(
      Integer page, Integer size, @Email @NotNull @NotBlank String email) {

    var owner = userService.findByEmail(email);
    log.info(
        "Requesting for all Texts instances of user with email={} with page={} and size={}",
        owner.email(),
        page,
        size);
    var pagination = paginator.apply(page, size);

    Pageable pageable =
        PageRequest.of(
            pagination.get("page"), pagination.get("size"), Sort.by("createdAt").descending());

    var results = textRepository.findAllByOwnerEmail(owner.email(), pageable);
    return results.map(textMapper::toCoreModel);
  }
}
