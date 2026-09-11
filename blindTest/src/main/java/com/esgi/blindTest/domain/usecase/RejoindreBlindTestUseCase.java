package com.esgi.blindTest.domain.usecase;

import com.esgi.blindTest.domain.exception.BlindTestCompletException;
import com.esgi.blindTest.domain.exception.BlindTestDejaDemarreException;
import com.esgi.blindTest.domain.exception.BlindTestTermineException;
import com.esgi.blindTest.domain.exception.ParticipantDejaPresentException;
import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.model.EtatLecture;
import com.esgi.blindTest.domain.model.Participant;
import com.esgi.blindTest.domain.model.Participation;
import com.esgi.blindTest.domain.model.StatutBlindTest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RejoindreBlindTestUseCase {

    public interface OutputPort {
        Participant findParticipant(Participant participant);

        BlindTest findBlindTest(BlindTest blindTest);

        BlindTest save(BlindTest blindTest);

        void enregistrerLaParticipation(BlindTest blindTest, Participation participation);
    }

    private final OutputPort output;

    public void apply(Participant participant, BlindTest blindtest) {
        Participant inscrit = output.findParticipant(participant);
        BlindTest courant = output.findBlindTest(blindtest);

        if (courant.getStatut() == StatutBlindTest.TERMINE) {
            throw new BlindTestTermineException();
        }
        if (estInscrit(courant, inscrit)) {
            throw new ParticipantDejaPresentException();
        }
        // Regle metier : un blind test accueille au maximum trois participants.
        // La limite est annoncee avant le demarrage : un blind test en cours est
        // toujours complet, et le message est plus parlant pour qui arrive trop tard.
        if (courant.getParticipations().size() >= BlindTest.NOMBRE_MAXIMUM_DE_PARTICIPANTS) {
            throw new BlindTestCompletException();
        }
        if (courant.getStatut() != StatutBlindTest.EN_ATTENTE) {
            throw new BlindTestDejaDemarreException();
        }

        Participation participation = new Participation(inscrit);
        courant.getParticipations().add(participation);

        // Regle metier : le blind test demarre des qu'il atteint trois participants,
        // et le premier morceau est joue.
        if (courant.getParticipations().size() == BlindTest.NOMBRE_MAXIMUM_DE_PARTICIPANTS) {
            courant.setStatut(StatutBlindTest.EN_COURS);
            courant.setIndexMorceauCourant(0);
            courant.setEtatLecture(EtatLecture.LECTURE);
            courant.setReservataire(null);
        }

        output.save(courant);
        output.enregistrerLaParticipation(courant, participation);
    }

    private static boolean estInscrit(BlindTest blindTest, Participant participant) {
        return blindTest.getParticipations().stream()
                .anyMatch(participation -> participation.getParticipant().getEmail()
                        .equals(participant.getEmail()));
    }
}
