package dev.razafindratelo.unfaked.exception;

public class AccountDeletedException extends RuntimeException {
  public AccountDeletedException(String message) {
    super(message);
  }
}
