package com.esgi.blindTest.domain.usecase;

import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.model.Morceau;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class AjouterBlindTestUseCase {

    public interface OutputPort {
        List<Morceau> tirerLesMorceaux(int nombre);

        BlindTest save(BlindTest blindTest);
    }

    private final OutputPort output;

    public BlindTest apply(String nom) {
        BlindTest blindTest = new BlindTest(nom);
        // La regle des sept morceaux est portee par le domaine, pas par l'adapter.
        blindTest.ajouterLesMorceaux(output.tirerLesMorceaux(BlindTest.NOMBRE_DE_MORCEAUX));
        return output.save(blindTest);
    }
}
