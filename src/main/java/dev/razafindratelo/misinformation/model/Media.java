package dev.razafindratelo.misinformation.model;

import dev.razafindratelo.misinformation.model.classifier.FileExtension;
import dev.razafindratelo.misinformation.model.classifier.FileType;
import dev.razafindratelo.misinformation.model.classifier.SizeType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Media {
  private String id;
  private String fileName;
  private double size;
  private SizeType sizeType;
  private FileExtension fileExtension;
  private LocalDateTime createdAt;
  private String bucketKey;
  private User owner;

  public FileType getFileType() {
    return fileExtension != null ? fileExtension.getFileType() : null;
  }
}
