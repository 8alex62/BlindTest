package com.esgi.blindTest.adapter.usecase_adapter;

import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.repository.BlindTestRepository;
import com.esgi.blindTest.domain.usecase.ConsulterBlindTestUseCase;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class ConsulterBlindTestAdapter implements ConsulterBlindTestUseCase.OutputPort {

    private final BlindTestRepository blindTestRepository;

    @Override
    public List<BlindTest> findBlindTests() {
        return blindTestRepository.findEnCours();
    }
}
