package dev.razafindratelo.misinformation.service;

import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;

import dev.razafindratelo.misinformation.endpoint.rest.controller.model.TextRequest;
import dev.razafindratelo.misinformation.mapper.TextMapper;
import dev.razafindratelo.misinformation.model.Text;
import dev.razafindratelo.misinformation.repository.TextRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Slf4j
@Validated
@RequiredArgsConstructor
public class TextService {
  private final UserService userService;
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
}
