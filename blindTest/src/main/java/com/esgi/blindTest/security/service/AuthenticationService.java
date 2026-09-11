package com.esgi.blindTest.security.service;

import com.esgi.blindTest.domain.model.Participant;
import com.esgi.blindTest.security.util.JwtUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthenticationService {

    public static final String ROLE_PARTICIPANT = "ROLE_PARTICIPANT";

    private final JwtUtil jwtUtil;
    private final String nomDuCookie;
    private final long expiration;

    public AuthenticationService(JwtUtil jwtUtil,
                                 @Value("${app.jwt.cookie}") String nomDuCookie,
                                 @Value("${app.jwt.expiration}") long expiration) {
        this.jwtUtil = jwtUtil;
        this.nomDuCookie = nomDuCookie;
        this.expiration = expiration;
    }

    public String genererLeJeton(Participant participant) {
        return jwtUtil.generateToken(participant.getId(), participant.getEmail(), ROLE_PARTICIPANT);
    }

    /**
     * Le jeton voyage dans un cookie HttpOnly : les pages Thymeleaf sont authentifiees
     * sans que le navigateur ait a manipuler le jeton lui-meme.
     */
    public ResponseCookie cookieDeConnexion(String jeton) {
        return ResponseCookie.from(nomDuCookie, jeton)
                .httpOnly(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofMillis(expiration))
                .build();
    }

    public ResponseCookie cookieDeDeconnexion() {
        return ResponseCookie.from(nomDuCookie, "")
                .httpOnly(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ZERO)
                .build();
    }

    public String nomDuCookie() {
        return nomDuCookie;
    }
}
