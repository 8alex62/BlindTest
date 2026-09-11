package com.esgi.blindTest.domain.usecase;

import com.esgi.blindTest.domain.model.BlindTest;
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
        // Regle portee par le domaine : le premier morceau est joue au demarrage.
        courant.demarrer();
        output.save(courant);
    }
}
