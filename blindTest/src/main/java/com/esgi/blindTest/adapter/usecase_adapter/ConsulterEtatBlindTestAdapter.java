package com.esgi.blindTest.adapter.usecase_adapter;

import com.esgi.blindTest.domain.exception.BlindTestIntrouvableException;
import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.repository.BlindTestRepository;
import com.esgi.blindTest.domain.usecase.ConsulterEtatBlindTestUseCase;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ConsulterEtatBlindTestAdapter implements ConsulterEtatBlindTestUseCase.OutputPort {

    private final BlindTestRepository blindTestRepository;

    @Override
    public BlindTest findBlindTest(BlindTest blindTest) {
        return blindTestRepository.findByNom(blindTest.getNom())
                .orElseThrow(BlindTestIntrouvableException::new);
    }
}
