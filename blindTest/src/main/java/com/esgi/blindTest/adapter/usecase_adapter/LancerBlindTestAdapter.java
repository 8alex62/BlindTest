package com.esgi.blindTest.adapter.usecase_adapter;

import com.esgi.blindTest.domain.exception.BlindTestIntrouvableException;
import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.repository.BlindTestRepository;
import com.esgi.blindTest.domain.usecase.LancerBlindTestUseCase;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LancerBlindTestAdapter implements LancerBlindTestUseCase.OutputPort {

    private final BlindTestRepository blindTestRepository;

    @Override
    public BlindTest findBlindTest(BlindTest blindTest) {
        return blindTestRepository.findById(blindTest.getId())
                .orElseThrow(BlindTestIntrouvableException::new);
    }

    @Override
    public BlindTest save(BlindTest blindTest) {
        return blindTestRepository.save(blindTest);
    }
}
