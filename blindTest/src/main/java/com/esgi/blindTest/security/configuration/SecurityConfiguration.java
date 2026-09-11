package com.esgi.blindTest.security.configuration;

import com.esgi.blindTest.security.filter.JwtFilter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

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
                                "/h2-console/**", "/favicon.ico").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(gestion -> gestion
                        .authenticationEntryPoint(this::rediriger))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Une page non authentifiee renvoie vers le formulaire de connexion,
     * un appel REST recoit un 401.
     */
    private void rediriger(jakarta.servlet.http.HttpServletRequest requete,
                           jakarta.servlet.http.HttpServletResponse reponse,
                           org.springframework.security.core.AuthenticationException exception)
            throws IOException {
        if (requete.getRequestURI().startsWith("/api/")) {
            reponse.sendError(HttpStatus.UNAUTHORIZED.value(), "Authentification requise");
        } else {
            reponse.sendRedirect("/connexion");
        }
    }
}
