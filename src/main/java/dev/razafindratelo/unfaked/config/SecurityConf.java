package dev.razafindratelo.unfaked.config;

import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.razafindratelo.unfaked.InfraGenerated;
import dev.razafindratelo.unfaked.endpoint.rest.controller.model.ErrorResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
  private static final String PING_ENDPOINT = "/ping";
  private static final String HEALTH_ENDPOINT = "/health/**";
  private static final String ACTUATOR_ENDPOINT = "/actuator/**";
  private static final String USERS_SIGN_UP_ENDPOINT = "/users/sign-up";
  private static final String AUTH_LOGIN_ENDPOINT = "/auth/login";
  private static final String AUTH_TOKEN_POST_ENDPOINT = "/auth/token";
  private static final String ROOT_ENDPOINT = "/";
  private static final String DOC_ENDPOINT = "/doc";
  private static final String SWAGGER_UI_ENDPOINT = "/swagger-ui/**";
  private static final String ANY_SUBPATH = "/**";
  private static final String V_3_API_DOCS = "/v3/api-docs/**";
  private static final String V_3_API_DOCS_YAML = "/v3/api-docs.yaml";
  private final TokenFilter tokenFilter;
  private final ObjectMapper om;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http.csrf(
            csrf ->
                csrf.ignoringRequestMatchers(
                    PING_ENDPOINT,
                    HEALTH_ENDPOINT,
                    ACTUATOR_ENDPOINT,
                    USERS_SIGN_UP_ENDPOINT,
                    AUTH_LOGIN_ENDPOINT,
                    AUTH_TOKEN_POST_ENDPOINT,
                    ROOT_ENDPOINT,
                    DOC_ENDPOINT,
                    DOC_ENDPOINT + ANY_SUBPATH,
                    SWAGGER_UI_ENDPOINT,
                    V_3_API_DOCS,
                    V_3_API_DOCS_YAML))
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(GET, PING_ENDPOINT)
                    .permitAll()
                    .requestMatchers(GET, HEALTH_ENDPOINT)
                    .permitAll()
                    .requestMatchers(ACTUATOR_ENDPOINT)
                    .permitAll()
                    .requestMatchers(GET, ROOT_ENDPOINT)
                    .permitAll()
                    .requestMatchers(POST, USERS_SIGN_UP_ENDPOINT)
                    .permitAll()
                    .requestMatchers(POST, AUTH_TOKEN_POST_ENDPOINT)
                    .permitAll()
                    .requestMatchers(POST, AUTH_LOGIN_ENDPOINT)
                    .permitAll()
                    .requestMatchers(
                        ROOT_ENDPOINT,
                        DOC_ENDPOINT,
                        DOC_ENDPOINT + ANY_SUBPATH,
                        SWAGGER_UI_ENDPOINT,
                        V_3_API_DOCS,
                        V_3_API_DOCS_YAML)
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
    source.registerCorsConfiguration(ANY_SUBPATH, configuration);
    return source;
  }
}
