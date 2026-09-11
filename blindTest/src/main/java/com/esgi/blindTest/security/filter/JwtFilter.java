package com.esgi.blindTest.security.filter;

import com.esgi.blindTest.domain.model.Participant;
import com.esgi.blindTest.security.service.AuthenticationService;
import com.esgi.blindTest.security.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AuthenticationService authenticationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Optional<String> jeton = jetonDuCookie(request).or(() -> jetonDeLEntete(request));

        if (jeton.isPresent() && jwtUtil.isTokenValid(jeton.get())) {
            // Le principal est le participant du domaine : les controleurs n'ont donc
            // pas besoin d'un repository pour savoir qui parle.
            Participant participant = new Participant();
            participant.setId(jwtUtil.extractId(jeton.get()));
            participant.setEmail(jwtUtil.extractUsername(jeton.get()));

            var authentification = new UsernamePasswordAuthenticationToken(
                    participant, null,
                    List.of(new SimpleGrantedAuthority(jwtUtil.extractRole(jeton.get()))));
            SecurityContextHolder.getContext().setAuthentication(authentification);
        }

        filterChain.doFilter(request, response);
    }

    private Optional<String> jetonDuCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> authenticationService.nomDuCookie().equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(valeur -> !valeur.isBlank())
                .findFirst();
    }

    private Optional<String> jetonDeLEntete(HttpServletRequest request) {
        String entete = request.getHeader("Authorization");
        if (entete != null && entete.startsWith("Bearer ")) {
            return Optional.of(entete.substring(7));
        }
        return Optional.empty();
    }
}
