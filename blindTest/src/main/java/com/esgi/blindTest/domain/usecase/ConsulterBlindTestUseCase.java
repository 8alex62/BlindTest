package com.esgi.blindTest.domain.usecase;

import com.esgi.blindTest.domain.model.BlindTest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class ConsulterBlindTestUseCase {

    public interface OutputPort {
        List<BlindTest> findBlindTests();
    }

    private final OutputPort output;

    public List<BlindTest> apply() {
        return output.findBlindTests();
    }
}
