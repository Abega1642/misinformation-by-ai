package dev.razafindratelo.unfaked.exception;

public class MultipartFileConversionException extends RuntimeException {
  public MultipartFileConversionException(String message) {
    super(message);
  }

  public MultipartFileConversionException(String message, Throwable cause) {
    super(message, cause);
  }
}
