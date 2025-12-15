package dev.razafindratelo.unfaked.exception;

public class ApiKeyException extends RuntimeException {
  public ApiKeyException(String message) {
    super(message);
  }

  public ApiKeyException(String message, Throwable cause) {
    super(message, cause);
  }
}
