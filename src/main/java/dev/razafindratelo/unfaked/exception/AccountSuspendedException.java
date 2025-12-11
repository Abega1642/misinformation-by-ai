package dev.razafindratelo.unfaked.exception;

public class AccountSuspendedException extends RuntimeException {
  public AccountSuspendedException(String message) {
    super(message);
  }
}
