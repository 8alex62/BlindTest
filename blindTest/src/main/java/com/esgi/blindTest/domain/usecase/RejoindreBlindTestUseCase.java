package com.esgi.blindTest.domain.usecase;

import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.model.Participant;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RejoindreBlindTestUseCase {

    public interface OutputPort {
        Participant findParticipant(Participant participant);

        BlindTest findBlindTest(BlindTest blindTest);

        BlindTest save(BlindTest blindTest);
    }

    private final OutputPort output;

    public void apply(Participant participant, BlindTest blindtest) {
        Participant inscrit = output.findParticipant(participant);
        BlindTest courant = output.findBlindTest(blindtest);
        // Regles portees par le domaine : maximum trois participants, demarrage au troisieme.
        courant.rejoindre(inscrit);
        output.save(courant);
    }
}
