package dev.razafindratelo.unfaked.exception;

public class GroqApiException extends RuntimeException {
  public GroqApiException(String message) {
    super(message);
  }

  public GroqApiException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
