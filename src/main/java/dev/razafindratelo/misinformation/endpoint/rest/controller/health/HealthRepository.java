package dev.razafindratelo.misinformation.endpoint.rest.controller.health;

import dev.razafindratelo.misinformation.repository.model.Dummy;
import dev.razafindratelo.misinformation.service.DummyService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/health/db")
public class HealthRepository {
  private final DummyService dummyService;

  @GetMapping
  public Page<Dummy> checkDbHealth(
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "size", required = false) Integer size) {
    return dummyService.getAll(page, size);
  }
}
