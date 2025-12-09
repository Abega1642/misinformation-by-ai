package dev.razafindratelo.misinformation.exception;

public class AccountSuspendedException extends RuntimeException {
  public AccountSuspendedException(String message) {
    super(message);
  }
}
