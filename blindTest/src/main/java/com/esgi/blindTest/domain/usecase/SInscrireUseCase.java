package com.esgi.blindTest.domain.usecase;

import com.esgi.blindTest.domain.model.Participant;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SInscrireUseCase {

    // KISS : une seule methode publique, apply.
    // SRP : le use case ne fait qu'orchestrer, il ne connait que son port de sortie.
    public interface OutputPort {
        void save(Participant participant);
    }

    private final OutputPort output;

    public void apply(String email, String motDePasse) {
        output.save(new Participant(email, motDePasse));
    }
}
