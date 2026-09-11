package com.esgi.blindTest.domain.usecase;

import com.esgi.blindTest.domain.exception.ReponseDejaReserveeException;
import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.model.Participant;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Clic sur "J'ai trouve" : le premier appel accepte reserve la reponse.
 */
@Component
@AllArgsConstructor
public class MettreEnPauseBlindTestUseCase {

    public interface OutputPort {
        BlindTest findBlindTest(BlindTest blindTest);

        /**
         * @return vrai si c'est bien cet appel qui a pris la main
         */
        boolean reserverLaReponse(BlindTest blindTest);
    }

    private final OutputPort output;

    public void apply(Participant participant, BlindTest blindtest) {
        BlindTest courant = output.findBlindTest(blindtest);
        // La regle est portee par le domaine.
        courant.reserverLaReponse(participant);
        // La base n'arbitre pas la regle, elle arbitre l'ordre d'arrivee.
        if (!output.reserverLaReponse(courant)) {
            courant.annulerLaReservation();
            throw new ReponseDejaReserveeException();
        }
    }
}
