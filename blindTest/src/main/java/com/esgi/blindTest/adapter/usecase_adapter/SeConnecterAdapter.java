package com.esgi.blindTest.adapter.usecase_adapter;

import com.esgi.blindTest.domain.exception.IdentifiantsInvalidesException;
import com.esgi.blindTest.domain.model.Participant;
import com.esgi.blindTest.domain.repository.ParticipantRepository;
import com.esgi.blindTest.domain.usecase.SeConnecterUseCase;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SeConnecterAdapter implements SeConnecterUseCase.OutputPort {

    private final ParticipantRepository participantRepository;

    @Override
    public Participant get(Participant participant) {
        Participant trouve = participantRepository.findByEmailAndMotDePasse(
                participant.getEmail(), participant.getMotDePasse());
        if (trouve == null) {
            // Meme message que l'email existe ou non, pour ne rien divulguer.
            throw new IdentifiantsInvalidesException();
        }
        return trouve;
    }
}
