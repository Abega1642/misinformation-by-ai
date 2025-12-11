package dev.razafindratelo.unfaked.exception;

import jakarta.validation.constraints.NotNull;

public class SearchException extends RuntimeException {
  public SearchException(@NotNull String message) {
    super(message);
  }

  public SearchException(String message, Throwable cause) {
    super(message, cause);
  }
}
