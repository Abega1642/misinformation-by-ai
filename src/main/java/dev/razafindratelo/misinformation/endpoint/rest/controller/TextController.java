package dev.razafindratelo.misinformation.endpoint.rest.controller;

import dev.razafindratelo.misinformation.endpoint.rest.controller.model.TextRequest;
import dev.razafindratelo.misinformation.model.Text;
import dev.razafindratelo.misinformation.service.TextService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

  @GetMapping("/{userEmail}")
  public Page<Text> getAllTextsInstanceByEmail(
      @PathVariable String userEmail,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "size", required = false) Integer size) {
    return textService.findAllByEmail(page, size, userEmail);
  }
}
