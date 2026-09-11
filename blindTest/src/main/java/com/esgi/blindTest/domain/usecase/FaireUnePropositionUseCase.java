package com.esgi.blindTest.domain.usecase;

import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.model.Participant;
import com.esgi.blindTest.domain.model.Participation;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class FaireUnePropositionUseCase {

    public interface OutputPort {
        Participant findParticipant(Participant participant);

        BlindTest findBlindTest(BlindTest blindTest);

        void ajouterUnPoint(Participation participation);

        BlindTest save(BlindTest blindTest);
    }

    private final OutputPort output;

    /**
     * @return vrai si la proposition est juste
     */
    public boolean apply(BlindTest blindtest, Participant participant, String proposition) {
        Participant inscrit = output.findParticipant(participant);
        BlindTest courant = output.findBlindTest(blindtest);
        // Regles portees par le domaine : seul le reservataire repond, un point par bonne
        // reponse, morceau suivant si juste, reprise de la lecture si faux.
        boolean juste = courant.repondre(inscrit, proposition);
        if (juste) {
            output.ajouterUnPoint(courant.participationDe(inscrit));
        }
        output.save(courant);
        return juste;
    }
}
