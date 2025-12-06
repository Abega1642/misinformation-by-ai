package dev.razafindratelo.misinformation.endpoint.rest.controller;

import dev.razafindratelo.misinformation.endpoint.rest.controller.model.TextRequest;
import dev.razafindratelo.misinformation.model.Text;
import dev.razafindratelo.misinformation.service.TextService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/texts")
public class TextController {
  private final TextService textService;

  @PostMapping("/upload")
  public Text uploadText(@RequestBody @Valid TextRequest request) {
    return textService.uploadText(request);
  }
}
