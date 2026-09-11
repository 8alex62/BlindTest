package com.esgi.blindTest.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Participation {

    // Champ technique : identité persistante de la participation.
    private Long id;

    @NonNull
    private Participant participant;

    private int score;

    /**
     * Règle métier : une bonne réponse rapporte un point.
     */
    public void ajouterUnPoint() {
        score++;
    }

    public boolean concerne(Participant autre) {
        return autre != null
                && participant != null
                && participant.getEmail() != null
                && participant.getEmail().equals(autre.getEmail());
    }
}
