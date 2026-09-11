package com.esgi.blindTest.domain.usecase;

import com.esgi.blindTest.domain.exception.BlindTestDejaDemarreException;
import com.esgi.blindTest.domain.exception.BlindTestIncompletException;
import com.esgi.blindTest.domain.exception.BlindTestTermineException;
import com.esgi.blindTest.domain.exception.NombreDeMorceauxInvalideException;
import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.model.EtatLecture;
import com.esgi.blindTest.domain.model.StatutBlindTest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LancerBlindTestUseCase {

    public interface OutputPort {
        BlindTest findBlindTest(BlindTest blindTest);

        BlindTest save(BlindTest blindTest);
    }

    private final OutputPort output;

    public void apply(BlindTest blindtest) {
        BlindTest courant = output.findBlindTest(blindtest);

        if (courant.getStatut() == StatutBlindTest.TERMINE) {
            throw new BlindTestTermineException();
        }
        if (courant.getStatut() == StatutBlindTest.EN_COURS) {
            throw new BlindTestDejaDemarreException();
        }
        // Regle metier : un blind test demarre avec trois participants et sept morceaux.
        if (courant.getParticipations().size() != BlindTest.NOMBRE_MAXIMUM_DE_PARTICIPANTS) {
            throw new BlindTestIncompletException();
        }
        if (courant.getMorceaux().size() != BlindTest.NOMBRE_DE_MORCEAUX) {
            throw new NombreDeMorceauxInvalideException(courant.getMorceaux().size());
        }

        // Regle metier : au demarrage, le premier morceau est joue.
        courant.setStatut(StatutBlindTest.EN_COURS);
        courant.setIndexMorceauCourant(0);
        courant.setEtatLecture(EtatLecture.LECTURE);
        courant.setReservataire(null);

        output.save(courant);
    }
}
