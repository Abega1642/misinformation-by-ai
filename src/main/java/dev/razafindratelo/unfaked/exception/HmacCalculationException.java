package dev.razafindratelo.unfaked.exception;

public class HmacCalculationException extends RuntimeException {
  public HmacCalculationException(String message) {
    super(message);
  }

  public HmacCalculationException(String message, Throwable cause) {
    super(message, cause);
  }
}
