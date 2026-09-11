package com.esgi.blindTest.domain.usecase;

import com.esgi.blindTest.domain.model.Participant;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SeConnecterUseCase {

    public interface OutputPort {
        Participant get(Participant participant);
    }

    private final OutputPort output;

    public Participant apply(String email, String motDePasse) {
        return output.get(new Participant(email, motDePasse));
    }
}
