package com.esgi.blindTest.domain.usecase;

import com.esgi.blindTest.domain.exception.ReponseDejaReserveeException;
import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.model.EtatLecture;
import com.esgi.blindTest.domain.model.Morceau;
import com.esgi.blindTest.domain.model.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.stream.IntStream;

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
        alice = participant(1L, "alice@esgi.fr");
        blindTest = blindTestEnCours(alice);
    }

    @Test
    void reserve_la_reponse_quand_la_base_accorde_la_main() {
        when(output.findBlindTest(any())).thenReturn(blindTest);
        when(output.reserverLaReponse(any())).thenReturn(true);

        mettreEnPauseBlindTestUseCase.apply(alice, blindTest);

        assertEquals(EtatLecture.PAUSE, blindTest.getEtatLecture());
        assertEquals(alice.getId(), blindTest.getIdParticipantReservataire());
    }

    @Test
    void refuse_le_clic_perdant_de_la_course() {
        when(output.findBlindTest(any())).thenReturn(blindTest);
        // Un autre participant a gagne la course : zero ligne modifiee en base.
        when(output.reserverLaReponse(any())).thenReturn(false);

        assertThrows(ReponseDejaReserveeException.class,
                () -> mettreEnPauseBlindTestUseCase.apply(alice, blindTest));
        assertNull(blindTest.getIdParticipantReservataire());
    }

    private static BlindTest blindTestEnCours(Participant... participants) {
        BlindTest blindTest = new BlindTest("Soiree ESGI");
        blindTest.setId(1L);
        blindTest.ajouterLesMorceaux(IntStream.rangeClosed(1, BlindTest.NOMBRE_DE_MORCEAUX)
                .mapToObj(numero -> new Morceau("Morceau " + numero))
                .toList());
        for (Participant participant : participants) {
            blindTest.rejoindre(participant);
        }
        blindTest.rejoindre(participant(98L, "bob@esgi.fr"));
        blindTest.rejoindre(participant(99L, "carole@esgi.fr"));
        return blindTest;
    }

    private static Participant participant(Long id, String email) {
        Participant participant = new Participant(email, "motdepasse");
        participant.setId(id);
        return participant;
    }
}
