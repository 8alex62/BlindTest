package com.esgi.blindTest.adapter.usecase_adapter;

import com.esgi.blindTest.domain.exception.EmailDejaUtiliseException;
import com.esgi.blindTest.domain.model.Participant;
import com.esgi.blindTest.domain.repository.ParticipantRepository;
import com.esgi.blindTest.domain.usecase.SInscrireUseCase;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SInscrireAdapter implements SInscrireUseCase.OutputPort {

    // On depend d'une interface du domaine, pas d'une technologie de persistance.
    private final ParticipantRepository participantRepository;

    @Override
    public void save(Participant participant) {
        if (participantRepository.findByEmail(participant.getEmail()).isPresent()) {
            throw new EmailDejaUtiliseException();
        }
        participantRepository.save(participant);
    }
}
