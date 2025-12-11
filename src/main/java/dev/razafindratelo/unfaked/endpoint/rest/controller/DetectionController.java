package dev.razafindratelo.unfaked.endpoint.rest.controller;

import dev.razafindratelo.unfaked.model.detection.TextDetectionResult;
import dev.razafindratelo.unfaked.service.TextDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/detections")
@RequiredArgsConstructor
public class DetectionController {
  private final TextDetectionService textDetectionService;

  @PostMapping("/texts")
  public TextDetectionResult analyseText(
      @RequestParam("from-userEmail") String userEmail, @RequestParam("text-id") String textId) {
    return textDetectionService.detect(textId, userEmail);
  }
}
