package com.esgi.blindTest.domain.usecase;

import com.esgi.blindTest.domain.model.BlindTest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Ajout au diagramme : le navigateur interroge l'etat du blind test toutes les secondes.
 * ConsulterBlindTestUseCase ne renvoie que les blind tests rejoignables, il ne permet
 * donc pas d'afficher les scores figes d'un blind test termine.
 */
@Component
@AllArgsConstructor
public class ConsulterEtatBlindTestUseCase {

    public interface OutputPort {
        BlindTest findBlindTest(BlindTest blindTest);
    }

    private final OutputPort output;

    public BlindTest apply(BlindTest blindtest) {
        return output.findBlindTest(blindtest);
    }
}
