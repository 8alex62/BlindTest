package com.esgi.blindTest.domain.usecase;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SeDeconnecterUseCase {

    public interface OutputPort {
        void deconnecter();
    }

    private final OutputPort output;

    public void apply() {
        output.deconnecter();
    }
}
