package dev.razafindratelo.unfaked.endpoint.rest.controller.health;

import dev.razafindratelo.unfaked.InfraGenerated;
import dev.razafindratelo.unfaked.service.health.HealthEventService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@InfraGenerated
@RestController
@AllArgsConstructor
public class HealthEventController {
  private final HealthEventService healthEventService;

  @GetMapping("/health/message")
  public List<String> triggerDummyEvents(
      @RequestParam(defaultValue = "1") int nbEvent,
      @RequestParam(defaultValue = "2") int waitInSeconds) {
    return healthEventService.triggerDummyEvents(nbEvent, waitInSeconds);
  }
}
