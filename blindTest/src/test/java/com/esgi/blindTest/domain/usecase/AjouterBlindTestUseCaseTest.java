package com.esgi.blindTest.domain.usecase;

import com.esgi.blindTest.domain.exception.NombreDeMorceauxInvalideException;
import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.model.Morceau;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AjouterBlindTestUseCaseTest {

    @Mock
    AjouterBlindTestUseCase.OutputPort output;

    @InjectMocks
    AjouterBlindTestUseCase ajouterBlindTestUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void enregistre_un_blind_test_de_sept_morceaux() {
        when(output.tirerLesMorceaux(anyInt())).thenReturn(morceaux(7));

        ajouterBlindTestUseCase.apply("Soiree ESGI");

        verify(output).save(any(BlindTest.class));
    }

    @Test
    void refuse_un_tirage_incomplet() {
        when(output.tirerLesMorceaux(anyInt())).thenReturn(morceaux(5));

        assertThrows(NombreDeMorceauxInvalideException.class,
                () -> ajouterBlindTestUseCase.apply("Soiree ESGI"));
        verify(output, never()).save(any(BlindTest.class));
    }

    private static List<Morceau> morceaux(int nombre) {
        return IntStream.rangeClosed(1, nombre)
                .mapToObj(numero -> new Morceau("Morceau " + numero))
                .toList();
    }
}
