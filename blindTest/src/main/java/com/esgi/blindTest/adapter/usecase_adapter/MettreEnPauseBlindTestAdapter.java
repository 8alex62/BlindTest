package com.esgi.blindTest.adapter.usecase_adapter;

import com.esgi.blindTest.domain.exception.BlindTestIntrouvableException;
import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.repository.BlindTestRepository;
import com.esgi.blindTest.domain.usecase.MettreEnPauseBlindTestUseCase;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MettreEnPauseBlindTestAdapter implements MettreEnPauseBlindTestUseCase.OutputPort {

    private final BlindTestRepository blindTestRepository;

    @Override
    public BlindTest findBlindTest(BlindTest blindTest) {
        return blindTestRepository.findByNom(blindTest.getNom())
                .orElseThrow(BlindTestIntrouvableException::new);
    }

    @Override
    public boolean reserverLaReponse(BlindTest blindTest) {
        return blindTestRepository.reserverLaReponse(blindTest);
    }
}
