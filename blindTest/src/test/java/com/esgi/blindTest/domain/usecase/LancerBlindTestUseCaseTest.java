package com.esgi.blindTest.domain.usecase;

import com.esgi.blindTest.domain.exception.BlindTestDejaDemarreException;
import com.esgi.blindTest.domain.exception.BlindTestIncompletException;
import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.model.EtatLecture;
import com.esgi.blindTest.domain.model.Participation;
import com.esgi.blindTest.domain.model.StatutBlindTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.esgi.blindTest.domain.usecase.RejoindreBlindTestUseCaseTest.blindTestEnAttente;
import static com.esgi.blindTest.domain.usecase.RejoindreBlindTestUseCaseTest.participant;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LancerBlindTestUseCaseTest {

    @Mock
    LancerBlindTestUseCase.OutputPort output;

    @InjectMocks
    LancerBlindTestUseCase lancerBlindTestUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void demarre_un_blind_test_complet_sur_le_premier_morceau() {
        BlindTest blindTest = blindTestComplet();
        when(output.findBlindTest(any())).thenReturn(blindTest);

        lancerBlindTestUseCase.apply(blindTest);

        assertEquals(StatutBlindTest.EN_COURS, blindTest.getStatut());
        assertEquals(EtatLecture.LECTURE, blindTest.getEtatLecture());
        assertEquals(0, blindTest.getIndexMorceauCourant());
        verify(output).save(blindTest);
    }

    @Test
    void refuse_de_demarrer_avec_moins_de_trois_participants() {
        BlindTest blindTest = blindTestEnAttente();
        blindTest.getParticipations().add(new Participation(participant("alice@esgi.fr")));
        when(output.findBlindTest(any())).thenReturn(blindTest);

        assertThrows(BlindTestIncompletException.class, () -> lancerBlindTestUseCase.apply(blindTest));
        verify(output, never()).save(any(BlindTest.class));
    }

    @Test
    void refuse_de_demarrer_deux_fois() {
        BlindTest blindTest = blindTestComplet();
        blindTest.setStatut(StatutBlindTest.EN_COURS);
        when(output.findBlindTest(any())).thenReturn(blindTest);

        assertThrows(BlindTestDejaDemarreException.class, () -> lancerBlindTestUseCase.apply(blindTest));
    }

    private static BlindTest blindTestComplet() {
        BlindTest blindTest = blindTestEnAttente();
        blindTest.getParticipations().add(new Participation(participant("alice@esgi.fr")));
        blindTest.getParticipations().add(new Participation(participant("bob@esgi.fr")));
        blindTest.getParticipations().add(new Participation(participant("carole@esgi.fr")));
        return blindTest;
    }
}
