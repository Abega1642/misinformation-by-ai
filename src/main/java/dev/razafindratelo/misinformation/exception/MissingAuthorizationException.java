package dev.razafindratelo.misinformation.exception;

public class MissingAuthorizationException extends RuntimeException {
  public MissingAuthorizationException(String message) {
    super(message);
  }
}
