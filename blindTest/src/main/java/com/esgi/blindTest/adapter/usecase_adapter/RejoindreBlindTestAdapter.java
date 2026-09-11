package com.esgi.blindTest.adapter.usecase_adapter;

import com.esgi.blindTest.domain.exception.BlindTestIntrouvableException;
import com.esgi.blindTest.domain.exception.ParticipantIntrouvableException;
import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.model.Participant;
import com.esgi.blindTest.domain.model.Participation;
import com.esgi.blindTest.domain.repository.BlindTestRepository;
import com.esgi.blindTest.domain.repository.ParticipantRepository;
import com.esgi.blindTest.domain.repository.ParticipationRepository;
import com.esgi.blindTest.domain.usecase.RejoindreBlindTestUseCase;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RejoindreBlindTestAdapter implements RejoindreBlindTestUseCase.OutputPort {

    private final ParticipantRepository participantRepository;
    private final ParticipationRepository participationRepository;
    private final BlindTestRepository blindTestRepository;

    @Override
    public Participant findParticipant(Participant participant) {
        return participantRepository.findByEmail(participant.getEmail())
                .orElseThrow(ParticipantIntrouvableException::new);
    }

    @Override
    public BlindTest findBlindTest(BlindTest blindTest) {
        return blindTestRepository.findByNom(blindTest.getNom())
                .orElseThrow(BlindTestIntrouvableException::new);
    }

    @Override
    public BlindTest save(BlindTest blindTest) {
        return blindTestRepository.save(blindTest);
    }

    @Override
    public void enregistrerLaParticipation(BlindTest blindTest, Participation participation) {
        participationRepository.enregistrer(blindTest.getNom(), participation);
    }
}
