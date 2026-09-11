package com.esgi.blindTest.security.configuration;

import com.esgi.blindTest.security.filter.JwtFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class SecurityConfiguration {

    private final JwtFilter jwtFilter;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(autorisations -> autorisations
                        .requestMatchers("/", "/inscription", "/connexion",
                                "/api/participants/**", "/css/**", "/js/**", "/audio/**",
                                "/h2-console/**", "/error", "/favicon.ico").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(gestion -> gestion
                        .authenticationEntryPoint(this::refuser))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Un appel REST non authentifie recoit un 401, une page renvoie vers le formulaire
     * de connexion. La reponse est ecrite directement, sans sendError : celui-ci
     * declencherait une seconde passe du filtre sur /error.
     */
    private void refuser(HttpServletRequest requete, HttpServletResponse reponse,
                         AuthenticationException exception) throws IOException {
        if (requete.getRequestURI().startsWith("/api/")) {
            reponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            reponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            reponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
            reponse.getWriter().write("{\"message\":\"Authentification requise.\"}");
        } else {
            reponse.sendRedirect("/connexion");
        }
    }
}
