package dev.razafindratelo.unfaked.exception;

public class AccountInactiveException extends RuntimeException {
  public AccountInactiveException(String message) {
    super(message);
  }
}
