package dev.razafindratelo.misinformation.exception;

public class AccountDeletedException extends RuntimeException {
  public AccountDeletedException(String message) {
    super(message);
  }
}
