package com.esgi.blindTest.presentation.controller.rest;

import com.esgi.blindTest.adapter.mapper.BlindTestMapper;
import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.model.Participant;
import com.esgi.blindTest.domain.usecase.AjouterBlindTestUseCase;
import com.esgi.blindTest.domain.usecase.ConsulterBlindTestUseCase;
import com.esgi.blindTest.domain.usecase.ConsulterEtatBlindTestUseCase;
import com.esgi.blindTest.domain.usecase.FaireUnePropositionUseCase;
import com.esgi.blindTest.domain.usecase.LancerBlindTestUseCase;
import com.esgi.blindTest.domain.usecase.MettreEnPauseBlindTestUseCase;
import com.esgi.blindTest.domain.usecase.RejoindreBlindTestUseCase;
import com.esgi.blindTest.presentation.request.AjoutBlindTestRequest;
import com.esgi.blindTest.presentation.request.PropositionRequest;
import com.esgi.blindTest.presentation.response.BlindTestResponse;
import com.esgi.blindTest.presentation.response.EtatBlindTestResponse;
import com.esgi.blindTest.presentation.response.PropositionResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/blindtests")
@AllArgsConstructor
public class BlindTestRestController {

    private final ConsulterBlindTestUseCase consulterBlindTestUseCase;
    private final ConsulterEtatBlindTestUseCase consulterEtatBlindTestUseCase;
    private final AjouterBlindTestUseCase ajouterBlindTestUseCase;
    private final RejoindreBlindTestUseCase rejoindreBlindTestUseCase;
    private final LancerBlindTestUseCase lancerBlindTestUseCase;
    private final MettreEnPauseBlindTestUseCase mettreEnPauseBlindTestUseCase;
    private final FaireUnePropositionUseCase faireUnePropositionUseCase;
    private final BlindTestMapper blindTestMapper;

    @GetMapping
    public List<BlindTestResponse> getBlindTests() {
        return blindTestMapper.toDtos(consulterBlindTestUseCase.apply());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BlindTestResponse postBlindTest(@RequestBody @Valid AjoutBlindTestRequest requete) {
        return blindTestMapper.toDto(ajouterBlindTestUseCase.apply(requete.nom()));
    }

    @PostMapping("/{id}/rejoindre")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void postRejoindre(@PathVariable Long id,
                              @AuthenticationPrincipal Participant participant) {
        rejoindreBlindTestUseCase.apply(participant, reference(id));
    }

    @PostMapping("/{id}/lancer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void postLancer(@PathVariable Long id) {
        lancerBlindTestUseCase.apply(reference(id));
    }

    /**
     * Clic sur "J'ai trouve". L'arbitrage du premier clic est entierement cote serveur :
    * le controleur ne porte aucune regle.
     */
    @PostMapping("/{id}/pause")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void postPause(@PathVariable Long id,
                          @AuthenticationPrincipal Participant participant) {
        mettreEnPauseBlindTestUseCase.apply(participant, reference(id));
    }

    @PostMapping("/{id}/proposition")
    public PropositionResponse postProposition(@PathVariable Long id,
                                               @AuthenticationPrincipal Participant participant,
                                               @RequestBody @Valid PropositionRequest requete) {
        return new PropositionResponse(
                faireUnePropositionUseCase.apply(reference(id), participant, requete.proposition()));
    }

    @GetMapping("/{id}/etat")
    public EtatBlindTestResponse getEtat(@PathVariable Long id,
                                         @AuthenticationPrincipal Participant participant) {
        return blindTestMapper.toEtatDto(consulterEtatBlindTestUseCase.apply(reference(id)), participant);
    }

    /**
     * Le controleur ne connait que l'identifiant : l'adapter du use case recharge
     * le blind test complet depuis son port.
     */
    private BlindTest reference(Long id) {
        BlindTest blindTest = new BlindTest();
        blindTest.setId(id);
        return blindTest;
    }
}
