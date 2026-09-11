package com.esgi.blindTest.domain.usecase;

import com.esgi.blindTest.domain.exception.NombreDeMorceauxInvalideException;
import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.model.Morceau;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
        List<Morceau> morceaux = output.tirerLesMorceaux(BlindTest.NOMBRE_DE_MORCEAUX);

        // Regle metier : un blind test est une sequence de sept morceaux.
        int nombre = morceaux == null ? 0 : morceaux.size();
        if (nombre != BlindTest.NOMBRE_DE_MORCEAUX) {
            throw new NombreDeMorceauxInvalideException(nombre);
        }

        BlindTest blindTest = new BlindTest(nom);
        blindTest.setMorceaux(new ArrayList<>(morceaux));
        return output.save(blindTest);
    }
}
