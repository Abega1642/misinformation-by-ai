package dev.razafindratelo.unfaked.exception;

public class AuthCodeInvalidException extends RuntimeException {
  public AuthCodeInvalidException(String message) {
    super(message);
  }
}
