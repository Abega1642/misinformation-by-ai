package dev.razafindratelo.misinformation.endpoint.rest.controller.health;

import dev.razafindratelo.misinformation.InfraGenerated;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@InfraGenerated
@RestController
@AllArgsConstructor
public class PingPongController {

  @GetMapping("/ping")
  public String ping() {
    return "pong";
  }
}
