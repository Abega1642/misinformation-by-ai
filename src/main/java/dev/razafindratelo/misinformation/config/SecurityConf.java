package dev.razafindratelo.misinformation.config;

import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.razafindratelo.misinformation.InfraGenerated;
import dev.razafindratelo.misinformation.endpoint.rest.controller.model.ErrorResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@InfraGenerated
@EnableWebSecurity
@Configuration
@AllArgsConstructor
public class SecurityConf {
  private final TokenFilter tokenFilter;
  private final ObjectMapper om;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(GET, "/ping")
                    .permitAll()
                    .requestMatchers(GET, "/health/**")
                    .permitAll()
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .requestMatchers(GET, "/")
                    .permitAll()
                    .requestMatchers(POST, "/users/sign-up")
                    .permitAll()
                    .requestMatchers(POST, "/auth/token")
                    .permitAll()
                    .requestMatchers(POST, "/auth/login")
                    .permitAll()
                    .requestMatchers(
                        "/",
                        "/doc",
                        "/doc/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(
            exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint()))
        .build();
  }

  @Bean
  public AuthenticationEntryPoint authenticationEntryPoint() {
    return (request, response, authException) -> {
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);

      var errorResponse =
          new ErrorResponse(
              now(),
              HttpStatus.UNAUTHORIZED.value(),
              HttpStatus.UNAUTHORIZED.getReasonPhrase(),
              "Authentication required",
              request.getServletPath(),
              "AUTHENTICATION_REQUIRED");

      try (var writer = response.getWriter()) {
        writer.write(om.writeValueAsString(errorResponse));
      }
    };
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    var configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(List.of("*"));
    configuration.setAllowedMethods(
        List.of(GET.name(), POST.name(), PUT.name(), DELETE.name(), OPTIONS.name(), PATCH.name()));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);

    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
