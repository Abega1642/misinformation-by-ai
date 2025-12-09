package dev.razafindratelo.misinformation.exception;

public class AccountInactiveException extends RuntimeException {
  public AccountInactiveException(String message) {
    super(message);
  }
}
