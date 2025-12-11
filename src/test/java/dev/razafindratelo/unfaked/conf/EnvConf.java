package dev.razafindratelo.unfaked.conf;

import dev.razafindratelo.unfaked.InfraGenerated;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;

@InfraGenerated
@TestConfiguration
public class EnvConf {

  public void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.rabbitmq.exchange", () -> "infra-event-exchange");
    registry.add("spring.rabbitmq.queue", () -> "infra-health-queue");
    registry.add("spring.rabbitmq.routing-key", () -> "spring.event.key");
    registry.add("groq.api.model", () -> "mixtral-8x7b-32768");
    registry.add("groq.api.max-tokens", () -> "1024");
    registry.add("groq.api.temperature", () -> "0.7");
    registry.add("groq.api.url", () -> "https://api.groq.com/openai/v1/chat/completions");
    registry.add("groq.api.key", () -> "test-groq-api-key");
    registry.add("google.api", () -> "https://serpapi.com");
    registry.add("google.api-key", () -> "test-google-api-key");
    registry.add("google.api-key", () -> "test-google-api-key");
    registry.add("google.api.engine", () -> "google_ai_mode");
    registry.add("google.api.engine", () -> "google_ai_mode");
  }
}
