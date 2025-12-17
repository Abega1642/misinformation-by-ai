package dev.razafindratelo.unfaked.exception;

public class MediaUploadException extends RuntimeException {
  public MediaUploadException(String message) {
    super(message);
  }

  public MediaUploadException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
