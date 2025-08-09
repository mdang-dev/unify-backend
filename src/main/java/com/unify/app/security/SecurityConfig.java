package com.unify.app.security;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final String[] ACCESS_ENDPOINTS = {
    "/auth/**",
    "/ws/**",
    "/send-email",
    "/liked-posts",
    "/users/logout",
    "/webhooks/livekit",
    "/actuator/**",
    "/swagger-ui/**",
    "/v3/api-docs/**",
    "/swagger-resources/**",
    "/webjars/**",
  };
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final CustomUserDetailsService customUserDetailsService;
  private final CustomLogoutHandler logoutHandler;

  SecurityConfig(
      @Lazy JwtAuthenticationFilter jwtAuthenticationFilter,
      CustomUserDetailsService customUserDetailsService,
      CustomLogoutHandler logoutHandler) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.customUserDetailsService = customUserDetailsService;
    this.logoutHandler = logoutHandler;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    return http.csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(ACCESS_ENDPOINTS)
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .userDetailsService(customUserDetailsService)
        .exceptionHandling(
            ex ->
                ex.accessDeniedHandler(
                        (request, response, accessDeniedException) -> response.setStatus(403))
                    .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .logout(
            log ->
                log.logoutUrl("/users/logout")
                    .addLogoutHandler(logoutHandler)
                    .logoutSuccessHandler(
                        ((request, response, authentication) ->
                            SecurityContextHolder.clearContext())))
        .httpBasic(Customizer.withDefaults())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {

    CorsConfiguration configuration = new CorsConfiguration();

    // Thay vì sử dụng wildcard *, chỉ định các origin cụ thể
    configuration.setAllowedOriginPatterns(
        List.of(
            "http://localhost:3000", // Frontend development
            "http://localhost:3001", // Frontend alternative port
            "https://unify.qzz.io", // Production domain
            "https://*.unify.qzz.io" // Subdomains
            ));

    // Cho phép credentials (cookies, authorization headers)
    configuration.setAllowCredentials(true);

    // Các HTTP methods được phép
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

    // Các headers được phép
    configuration.setAllowedHeaders(
        List.of(
            HttpHeaders.AUTHORIZATION,
            HttpHeaders.CONTENT_TYPE,
            HttpHeaders.ACCEPT,
            HttpHeaders.ORIGIN,
            "X-Requested-With",
            "token" // Cho WebSocket authentication
            ));

    // Headers mà client có thể đọc
    configuration.setExposedHeaders(
        List.of(HttpHeaders.AUTHORIZATION, "X-Total-Count", "X-Page-Count"));

    // Thời gian cache preflight requests (1 giờ)
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
      throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10);
  }
}
