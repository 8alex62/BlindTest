package com.esgi.blindTest.presentation.controller.rest;

import com.esgi.blindTest.domain.model.Participant;
import com.esgi.blindTest.domain.usecase.SInscrireUseCase;
import com.esgi.blindTest.domain.usecase.SeConnecterUseCase;
import com.esgi.blindTest.domain.usecase.SeDeconnecterUseCase;
import com.esgi.blindTest.presentation.request.ConnexionRequest;
import com.esgi.blindTest.presentation.request.InscriptionRequest;
import com.esgi.blindTest.presentation.response.ConnexionResponse;
import com.esgi.blindTest.security.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/participants")
@AllArgsConstructor
public class ParticipantRestController {

    private final SInscrireUseCase sInscrireUseCase;
    private final SeConnecterUseCase seConnecterUseCase;
    private final SeDeconnecterUseCase seDeconnecterUseCase;
    private final AuthenticationService authenticationService;

    // Le controleur valide la requete HTTP, delegue au use case et serialise la reponse.
    @PostMapping("/inscription")
    @ResponseStatus(HttpStatus.CREATED)
    public void postInscription(@RequestBody @Valid InscriptionRequest inscriptionRequest) {
        sInscrireUseCase.apply(inscriptionRequest.email(), inscriptionRequest.motDePasse());
    }

    @PostMapping("/connexion")
    public ResponseEntity<ConnexionResponse> postConnexion(
            @RequestBody @Valid ConnexionRequest connexionRequest) {

        Participant participant = seConnecterUseCase.apply(
                connexionRequest.email(), connexionRequest.motDePasse());
        String jeton = authenticationService.genererLeJeton(participant);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        authenticationService.cookieDeConnexion(jeton).toString())
                .body(new ConnexionResponse(jeton, AuthenticationService.ROLE_PARTICIPANT,
                        participant.getEmail()));
    }

    @PostMapping("/deconnexion")
    public ResponseEntity<Void> postDeconnexion() {
        seDeconnecterUseCase.apply();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE,
                        authenticationService.cookieDeDeconnexion().toString())
                .build();
    }
}
