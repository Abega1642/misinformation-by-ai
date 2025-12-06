package dev.razafindratelo.misinformation.exception;

public class AuthCodeInvalidException extends RuntimeException {
  public AuthCodeInvalidException(String message) {
    super(message);
  }
}
