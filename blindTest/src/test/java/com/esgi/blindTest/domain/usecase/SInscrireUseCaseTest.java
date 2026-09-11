package com.esgi.blindTest.domain.usecase;

import com.esgi.blindTest.domain.model.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class SInscrireUseCaseTest {

    @Mock
    SInscrireUseCase.OutputPort output;

    @InjectMocks
    SInscrireUseCase sInscrireUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testApply() {
        sInscrireUseCase.apply("alice@esgi.fr", "motdepasse");

        verify(output).save(any(Participant.class));
    }
}
