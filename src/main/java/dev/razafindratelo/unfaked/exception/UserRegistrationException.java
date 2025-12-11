package dev.razafindratelo.unfaked.exception;

public class UserRegistrationException extends RuntimeException {
  public UserRegistrationException(String message) {
    super(message);
  }

  public UserRegistrationException(String message, Throwable cause) {
    super(message, cause);
  }
}
