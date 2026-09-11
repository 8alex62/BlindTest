package com.esgi.blindTest.domain.usecase;

import com.esgi.blindTest.domain.exception.BlindTestCompletException;
import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.model.Morceau;
import com.esgi.blindTest.domain.model.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RejoindreBlindTestUseCaseTest {

    @Mock
    RejoindreBlindTestUseCase.OutputPort output;

    @InjectMocks
    RejoindreBlindTestUseCase rejoindreBlindTestUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void ajoute_une_participation_et_enregistre() {
        Participant alice = participant(1L, "alice@esgi.fr");
        BlindTest blindTest = blindTest();
        when(output.findParticipant(any())).thenReturn(alice);
        when(output.findBlindTest(any())).thenReturn(blindTest);

        rejoindreBlindTestUseCase.apply(alice, blindTest);

        assertEquals(1, blindTest.getParticipations().size());
        verify(output).save(blindTest);
    }

    @Test
    void refuse_un_quatrieme_participant() {
        BlindTest blindTest = blindTest();
        blindTest.rejoindre(participant(1L, "alice@esgi.fr"));
        blindTest.rejoindre(participant(2L, "bob@esgi.fr"));
        blindTest.rejoindre(participant(3L, "carole@esgi.fr"));

        Participant david = participant(4L, "david@esgi.fr");
        when(output.findParticipant(any())).thenReturn(david);
        when(output.findBlindTest(any())).thenReturn(blindTest);

        assertThrows(BlindTestCompletException.class,
                () -> rejoindreBlindTestUseCase.apply(david, blindTest));
        verify(output, never()).save(any(BlindTest.class));
    }

    private static BlindTest blindTest() {
        BlindTest blindTest = new BlindTest("Soiree ESGI");
        blindTest.setId(1L);
        blindTest.ajouterLesMorceaux(IntStream.rangeClosed(1, BlindTest.NOMBRE_DE_MORCEAUX)
                .mapToObj(numero -> new Morceau("Morceau " + numero))
                .toList());
        return blindTest;
    }

    private static Participant participant(Long id, String email) {
        Participant participant = new Participant(email, "motdepasse");
        participant.setId(id);
        return participant;
    }
}
