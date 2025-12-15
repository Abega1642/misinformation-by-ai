package dev.razafindratelo.unfaked.endpoint.rest.controller.health;

import dev.razafindratelo.unfaked.InfraGenerated;
import dev.razafindratelo.unfaked.service.HealthBucketService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@InfraGenerated
@RestController
@AllArgsConstructor
public class HealthBucketController {

  private final HealthBucketService healthBucketService;

  @GetMapping("/health/bucket")
  public ResponseEntity<String> checkBucketHealth() {
    return ResponseEntity.ok(healthBucketService.performHealthCheck().toString());
  }
}
