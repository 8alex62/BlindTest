package com.esgi.blindTest.domain.usecase;

import com.esgi.blindTest.domain.exception.BlindTestCompletException;
import com.esgi.blindTest.domain.exception.ParticipantDejaPresentException;
import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.model.EtatLecture;
import com.esgi.blindTest.domain.model.Morceau;
import com.esgi.blindTest.domain.model.Participant;
import com.esgi.blindTest.domain.model.Participation;
import com.esgi.blindTest.domain.model.StatutBlindTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test unitaire sans Spring : le port de sortie est mocke.
 */
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
    void ajoute_une_participation_et_lenregistre() {
        Participant alice = participant("alice@esgi.fr");
        BlindTest blindTest = blindTestEnAttente();
        when(output.findParticipant(any())).thenReturn(alice);
        when(output.findBlindTest(any())).thenReturn(blindTest);

        rejoindreBlindTestUseCase.apply(alice, blindTest);

        assertEquals(1, blindTest.getParticipations().size());
        verify(output).save(blindTest);
        verify(output).enregistrerLaParticipation(any(BlindTest.class), any(Participation.class));
    }

    @Test
    void demarre_automatiquement_au_troisieme_participant() {
        BlindTest blindTest = blindTestEnAttente();
        blindTest.getParticipations().add(new Participation(participant("bob@esgi.fr")));
        blindTest.getParticipations().add(new Participation(participant("carole@esgi.fr")));

        Participant alice = participant("alice@esgi.fr");
        when(output.findParticipant(any())).thenReturn(alice);
        when(output.findBlindTest(any())).thenReturn(blindTest);

        rejoindreBlindTestUseCase.apply(alice, blindTest);

        assertEquals(StatutBlindTest.EN_COURS, blindTest.getStatut());
        assertEquals(EtatLecture.LECTURE, blindTest.getEtatLecture());
        assertEquals(0, blindTest.getIndexMorceauCourant());
        assertEquals("Morceau 1", blindTest.getMorceaux().get(0).getNom());
    }

    @Test
    void refuse_un_quatrieme_participant() {
        BlindTest blindTest = blindTestEnAttente();
        blindTest.getParticipations().add(new Participation(participant("alice@esgi.fr")));
        blindTest.getParticipations().add(new Participation(participant("bob@esgi.fr")));
        blindTest.getParticipations().add(new Participation(participant("carole@esgi.fr")));

        Participant david = participant("david@esgi.fr");
        when(output.findParticipant(any())).thenReturn(david);
        when(output.findBlindTest(any())).thenReturn(blindTest);

        assertThrows(BlindTestCompletException.class,
                () -> rejoindreBlindTestUseCase.apply(david, blindTest));
        assertEquals(3, blindTest.getParticipations().size());
        verify(output, never()).save(any(BlindTest.class));
    }

    @Test
    void refuse_un_participant_deja_inscrit() {
        Participant alice = participant("alice@esgi.fr");
        BlindTest blindTest = blindTestEnAttente();
        blindTest.getParticipations().add(new Participation(alice));
        when(output.findParticipant(any())).thenReturn(alice);
        when(output.findBlindTest(any())).thenReturn(blindTest);

        assertThrows(ParticipantDejaPresentException.class,
                () -> rejoindreBlindTestUseCase.apply(alice, blindTest));
        assertEquals(1, blindTest.getParticipations().size());
    }

    static BlindTest blindTestEnAttente() {
        BlindTest blindTest = new BlindTest("Soiree ESGI");
        blindTest.setMorceaux(septMorceaux());
        return blindTest;
    }

    static List<Morceau> septMorceaux() {
        return new ArrayList<>(IntStream.rangeClosed(1, BlindTest.NOMBRE_DE_MORCEAUX)
                .mapToObj(numero -> new Morceau("Morceau " + numero))
                .toList());
    }

    static Participant participant(String email) {
        return new Participant(email, "motdepasse");
    }
}
