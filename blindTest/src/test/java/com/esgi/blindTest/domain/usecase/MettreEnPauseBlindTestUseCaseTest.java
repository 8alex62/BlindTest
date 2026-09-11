package com.esgi.blindTest.domain.usecase;

import com.esgi.blindTest.domain.exception.ParticipantHorsBlindTestException;
import com.esgi.blindTest.domain.exception.ReponseDejaReserveeException;
import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.model.EtatLecture;
import com.esgi.blindTest.domain.model.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.esgi.blindTest.domain.usecase.FaireUnePropositionUseCaseTest.blindTestEnCours;
import static com.esgi.blindTest.domain.usecase.RejoindreBlindTestUseCaseTest.participant;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class MettreEnPauseBlindTestUseCaseTest {

    @Mock
    MettreEnPauseBlindTestUseCase.OutputPort output;

    @InjectMocks
    MettreEnPauseBlindTestUseCase mettreEnPauseBlindTestUseCase;

    private Participant alice;
    private BlindTest blindTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        alice = participant("alice@esgi.fr");
        blindTest = blindTestEnCours("Morceau 1");
    }

    @Test
    void le_premier_clic_met_en_pause_et_reserve_la_reponse() {
        when(output.findBlindTest(any())).thenReturn(blindTest);
        when(output.reserverLaReponse(any())).thenReturn(true);

        mettreEnPauseBlindTestUseCase.apply(alice, blindTest);

        assertEquals(EtatLecture.PAUSE, blindTest.getEtatLecture());
        assertEquals(alice.getEmail(), blindTest.getReservataire().getEmail());
    }

    @Test
    void refuse_le_second_clic_sur_le_meme_morceau() {
        blindTest.setReservataire(participant("bob@esgi.fr"));
        blindTest.setEtatLecture(EtatLecture.PAUSE);
        when(output.findBlindTest(any())).thenReturn(blindTest);

        ReponseDejaReserveeException erreur = assertThrows(ReponseDejaReserveeException.class,
                () -> mettreEnPauseBlindTestUseCase.apply(alice, blindTest));

        assertEquals("bob@esgi.fr", blindTest.getReservataire().getEmail());
        assertEquals("Un autre participant a déjà pris la main sur ce morceau.",
                erreur.getMessage());
    }

    @Test
    void refuse_le_clic_perdant_de_la_course() {
        when(output.findBlindTest(any())).thenReturn(blindTest);
        // Un autre participant a gagne la course : zero ligne modifiee en base.
        when(output.reserverLaReponse(any())).thenReturn(false);

        assertThrows(ReponseDejaReserveeException.class,
                () -> mettreEnPauseBlindTestUseCase.apply(alice, blindTest));
        assertNull(blindTest.getReservataire());
        assertEquals(EtatLecture.LECTURE, blindTest.getEtatLecture());
    }

    @Test
    void refuse_un_participant_qui_ne_joue_pas() {
        when(output.findBlindTest(any())).thenReturn(blindTest);

        assertThrows(ParticipantHorsBlindTestException.class,
                () -> mettreEnPauseBlindTestUseCase.apply(participant("david@esgi.fr"), blindTest));
    }
}
