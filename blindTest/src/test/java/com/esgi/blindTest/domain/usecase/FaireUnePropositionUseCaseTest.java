package com.esgi.blindTest.domain.usecase;

import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.model.Morceau;
import com.esgi.blindTest.domain.model.Participant;
import com.esgi.blindTest.domain.model.Participation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FaireUnePropositionUseCaseTest {

    @Mock
    FaireUnePropositionUseCase.OutputPort output;

    @InjectMocks
    FaireUnePropositionUseCase faireUnePropositionUseCase;

    private Participant alice;
    private BlindTest blindTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        alice = participant(1L, "alice@esgi.fr");
        blindTest = blindTestEnCours();
        blindTest.reserverLaReponse(alice);
        when(output.findParticipant(any())).thenReturn(alice);
        when(output.findBlindTest(any())).thenReturn(blindTest);
    }

    @Test
    void une_bonne_reponse_ajoute_un_point() {
        boolean juste = faireUnePropositionUseCase.apply(blindTest, alice, "morceau 1");

        assertTrue(juste);
        verify(output).ajouterUnPoint(any(Participation.class));
        verify(output).save(blindTest);
    }

    @Test
    void une_mauvaise_reponse_najoute_aucun_point() {
        boolean juste = faireUnePropositionUseCase.apply(blindTest, alice, "autre chose");

        assertFalse(juste);
        verify(output, never()).ajouterUnPoint(any(Participation.class));
        verify(output).save(blindTest);
    }

    private BlindTest blindTestEnCours() {
        BlindTest courant = new BlindTest("Soiree ESGI");
        courant.setId(1L);
        courant.ajouterLesMorceaux(IntStream.rangeClosed(1, BlindTest.NOMBRE_DE_MORCEAUX)
                .mapToObj(numero -> new Morceau("Morceau " + numero))
                .toList());
        courant.rejoindre(alice);
        courant.rejoindre(participant(2L, "bob@esgi.fr"));
        courant.rejoindre(participant(3L, "carole@esgi.fr"));
        return courant;
    }

    private static Participant participant(Long id, String email) {
        Participant participant = new Participant(email, "motdepasse");
        participant.setId(id);
        return participant;
    }
}
