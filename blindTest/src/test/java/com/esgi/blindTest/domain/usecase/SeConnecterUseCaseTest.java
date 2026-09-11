package com.esgi.blindTest.domain.usecase;

import com.esgi.blindTest.domain.model.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SeConnecterUseCaseTest {

    @Mock
    SeConnecterUseCase.OutputPort output;

    @InjectMocks
    SeConnecterUseCase seConnecterUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testApply() {
        when(output.get(any(Participant.class)))
                .thenReturn(new Participant("alice@esgi.fr", "motdepasse"));

        Participant resultat = seConnecterUseCase.apply("alice@esgi.fr", "motdepasse");

        assertEquals(new Participant("alice@esgi.fr", "motdepasse"), resultat);
    }
}
