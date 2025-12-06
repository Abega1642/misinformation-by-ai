package dev.razafindratelo.misinformation.config;

import dev.razafindratelo.misinformation.InfraGenerated;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@InfraGenerated
@Configuration
public class SwaggerDocConf implements WebMvcConfigurer {

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addRedirectViewController("/", "/doc");
  }
}
