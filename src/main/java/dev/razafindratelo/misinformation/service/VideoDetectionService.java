package dev.razafindratelo.misinformation.service;

import dev.razafindratelo.misinformation.file.BucketComponent;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import lombok.AllArgsConstructor;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class VideoDetectionService {

  private final BucketComponent bucketComponent;

  @Value("${sightengine.api.user}")
  private String apiUser;

  @Value("${sightengine.api.secret}")
  private String apiSecret;

  public String analyzeLongVideo(String bucketKey, String callbackUrl) throws IOException {
    URL presignedUrl = bucketComponent.presign(bucketKey, Duration.ofMinutes(30));

    try (CloseableHttpClient client = HttpClients.createDefault()) {
      String baseUrl = "https://api.sightengine.com/1.0";
      HttpPost post = new HttpPost(baseUrl + "/video/check.json");

      MultipartEntityBuilder builder =
          MultipartEntityBuilder.create()
              .addTextBody("url", presignedUrl.toString())
              .addTextBody("models", "genai")
              .addTextBody("callback_url", callbackUrl)
              .addTextBody("api_user", apiUser)
              .addTextBody("api_secret", apiSecret);

      post.setEntity((HttpEntity) builder.build());

      try (CloseableHttpResponse response = client.execute(post)) {
        return EntityUtils.toString(response.getEntity());
      }
    }
  }
}
