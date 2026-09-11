package com.esgi.blindTest.adapter.usecase_adapter;

import com.esgi.blindTest.domain.usecase.SeDeconnecterUseCase;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Le jeton JWT est sans etat : la deconnexion se limite a vider le contexte de securite,
 * le controleur se chargeant d'effacer le cookie. Aucun repository n'est donc necessaire.
 */
@Component
public class SeDeconnecterAdapter implements SeDeconnecterUseCase.OutputPort {

    @Override
    public void deconnecter() {
        SecurityContextHolder.clearContext();
    }
}
