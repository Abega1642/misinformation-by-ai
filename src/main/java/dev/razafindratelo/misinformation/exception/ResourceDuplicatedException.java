package dev.razafindratelo.misinformation.exception;

public class ResourceDuplicatedException extends RuntimeException {
  public ResourceDuplicatedException(String message) {
    super(message);
  }

  public ResourceDuplicatedException(String message, Throwable cause) {
    super(message, cause);
  }
}
