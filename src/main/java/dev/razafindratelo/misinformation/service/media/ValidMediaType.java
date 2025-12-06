package dev.razafindratelo.misinformation.service.media;

import dev.razafindratelo.misinformation.model.classifier.FileType;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MediaTypeValidator.class)
public @interface ValidMediaType {
  String message() default "Invalid media type";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  FileType value();
}
