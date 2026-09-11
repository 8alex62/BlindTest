package com.esgi.blindTest.domain.usecase;

import com.esgi.blindTest.domain.exception.BlindTestNonDemarreException;
import com.esgi.blindTest.domain.exception.BlindTestTermineException;
import com.esgi.blindTest.domain.exception.ParticipantHorsBlindTestException;
import com.esgi.blindTest.domain.exception.ReponseDejaReserveeException;
import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.model.EtatLecture;
import com.esgi.blindTest.domain.model.Participant;
import com.esgi.blindTest.domain.model.StatutBlindTest;
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

        if (courant.getStatut() == StatutBlindTest.TERMINE) {
            throw new BlindTestTermineException();
        }
        if (courant.getStatut() != StatutBlindTest.EN_COURS) {
            throw new BlindTestNonDemarreException();
        }
        if (!estInscrit(courant, participant)) {
            throw new ParticipantHorsBlindTestException();
        }
        // Regle metier : un seul reservataire de reponse a la fois.
        if (courant.getReservataire() != null) {
            throw new ReponseDejaReserveeException();
        }

        // Regle metier : le premier clic met la lecture en pause et reserve la reponse.
        courant.setEtatLecture(EtatLecture.PAUSE);
        courant.setReservataire(participant);

        // La base n'arbitre pas la regle, elle arbitre l'ordre d'arrivee.
        if (!output.reserverLaReponse(courant)) {
            courant.setReservataire(null);
            courant.setEtatLecture(EtatLecture.LECTURE);
            throw new ReponseDejaReserveeException();
        }
    }

    private static boolean estInscrit(BlindTest blindTest, Participant participant) {
        return participant != null && blindTest.getParticipations().stream()
                .anyMatch(participation -> participation.getParticipant().getEmail()
                        .equals(participant.getEmail()));
    }
}
