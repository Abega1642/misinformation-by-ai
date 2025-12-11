package dev.razafindratelo.unfaked.exception;

public class MissingAuthorizationException extends RuntimeException {
  public MissingAuthorizationException(String message) {
    super(message);
  }
}
