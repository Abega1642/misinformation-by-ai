package dev.razafindratelo.unfaked.endpoint.rest.controller.health;

import static java.util.UUID.randomUUID;

import dev.razafindratelo.unfaked.InfraGenerated;
import dev.razafindratelo.unfaked.file.BucketComponent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@InfraGenerated
@RestController
@AllArgsConstructor
public class HealthBucketController {

  private static final String HEALTH_KEY = "health";
  private final BucketComponent bucketComponent;

  @GetMapping("/health/bucket")
  public ResponseEntity<String> file_can_be_uploaded_then_signed() throws IOException {
    String fileSuffix = ".txt";
    String filePrefix = randomUUID().toString();
    File fileToUpload = Files.createTempFile(filePrefix, fileSuffix).toFile();
    writeRandomContent(fileToUpload);

    String fileBucketKey = HEALTH_KEY + "/" + filePrefix + fileSuffix;
    bucketComponent.upload(fileToUpload, fileBucketKey);

    File downloaded = bucketComponent.download(fileBucketKey);

    if (!Files.readString(fileToUpload.toPath()).equals(Files.readString(downloaded.toPath())))
      throw new RuntimeException("Uploaded and downloaded content mismatch");

    String dirPrefix = "dir-" + randomUUID();
    File dir = Files.createTempDirectory(dirPrefix).toFile();
    File fInDir = new File(dir, randomUUID() + ".txt");
    writeRandomContent(fInDir);
    String dirBucketKey = HEALTH_KEY + "/" + dirPrefix;
    bucketComponent.upload(dir, dirBucketKey);

    return ResponseEntity.ok(
        bucketComponent.presign(fileBucketKey, Duration.ofMinutes(2)).toString());
  }

  private void writeRandomContent(File file) throws IOException {
    try (FileWriter writer = new FileWriter(file)) {
      writer.write(randomUUID().toString());
    }
  }
}
