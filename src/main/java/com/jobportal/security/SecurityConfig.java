package com.jobportal.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .securityContext(context -> context.securityContextRepository(securityContextRepository()))
                .authorizeHttpRequests(auth -> auth
                        // OpenAPI / Swagger endpoints
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml"
                        ).permitAll()

                        // Authentication endpoints
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // Public job endpoints & application tracking
                        .requestMatchers(HttpMethod.GET, "/api/v1/jobs").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/jobs/{id:[0-9]+}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/applications/track/**").permitAll()

                        // Seeker endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/jobs/*/apply").hasRole("SEEKER")
                        .requestMatchers("/api/v1/applications/my").hasRole("SEEKER")

                        // Poster endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/jobs").hasRole("POSTER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/jobs/*").hasRole("POSTER")
                        .requestMatchers("/api/v1/jobs/my-posted").hasRole("POSTER")
                        .requestMatchers("/api/v1/jobs/*/applications").hasRole("POSTER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/applications/*/status").hasRole("POSTER")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"success\":false,\"status\":401,\"error\":\"Unauthorized: Authentication required. Please login first via /api/v1/auth/login or use Authorize lock in Swagger.\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"success\":false,\"status\":403,\"error\":\"Forbidden: Role mismatch. (Seekers can only apply/track; Posters can only post/manage jobs).\"}");
                        })
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
