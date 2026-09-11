package com.esgi.blindTest.domain.usecase;

import com.esgi.blindTest.domain.exception.ReponseNonReserveeException;
import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.model.EtatLecture;
import com.esgi.blindTest.domain.model.Morceau;
import com.esgi.blindTest.domain.model.Participant;
import com.esgi.blindTest.domain.model.Participation;
import com.esgi.blindTest.domain.model.StatutBlindTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static com.esgi.blindTest.domain.usecase.RejoindreBlindTestUseCaseTest.participant;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        alice = participant("alice@esgi.fr");
        when(output.findParticipant(any())).thenReturn(alice);
    }

    @Test
    void une_bonne_reponse_ajoute_un_point_et_passe_au_morceau_suivant() {
        BlindTest blindTest = avecLaMain("Morceau 1");

        boolean juste = faireUnePropositionUseCase.apply(blindTest, alice, "  MORCEAU 1 ");

        assertTrue(juste);
        assertEquals(1, scoreDe(blindTest, alice));
        assertEquals(1, blindTest.getIndexMorceauCourant());
        assertEquals(EtatLecture.LECTURE, blindTest.getEtatLecture());
        assertEquals(null, blindTest.getReservataire());
        verify(output).ajouterUnPoint(any(BlindTest.class), any(Participation.class));
        verify(output).save(blindTest);
    }

    @Test
    void une_mauvaise_reponse_relance_la_lecture_sans_changer_de_morceau() {
        BlindTest blindTest = avecLaMain("Morceau 1");

        boolean juste = faireUnePropositionUseCase.apply(blindTest, alice, "Une autre chanson");

        assertFalse(juste);
        assertEquals(0, scoreDe(blindTest, alice));
        assertEquals(0, blindTest.getIndexMorceauCourant());
        assertEquals(EtatLecture.LECTURE, blindTest.getEtatLecture());
        assertEquals(null, blindTest.getReservataire());
        verify(output, never()).ajouterUnPoint(any(BlindTest.class), any(Participation.class));
        verify(output).save(blindTest);
    }

    @Test
    void seul_le_reservataire_peut_repondre() {
        BlindTest blindTest = blindTestEnCours("Morceau 1");
        blindTest.setReservataire(participant("bob@esgi.fr"));
        when(output.findBlindTest(any())).thenReturn(blindTest);

        assertThrows(ReponseNonReserveeException.class,
                () -> faireUnePropositionUseCase.apply(blindTest, alice, "Morceau 1"));
    }

    @Test
    void le_blind_test_se_termine_apres_le_septieme_morceau_et_fige_les_scores() {
        BlindTest blindTest = avecLaMain("Morceau 1");
        blindTest.setIndexMorceauCourant(BlindTest.NOMBRE_DE_MORCEAUX - 1);

        boolean juste = faireUnePropositionUseCase.apply(blindTest, alice, "Morceau 7");

        assertTrue(juste);
        assertEquals(StatutBlindTest.TERMINE, blindTest.getStatut());
        assertEquals(EtatLecture.PAUSE, blindTest.getEtatLecture());
        assertEquals(BlindTest.NOMBRE_DE_MORCEAUX, blindTest.getIndexMorceauCourant());
        assertEquals(1, scoreDe(blindTest, alice));
    }

    // La comparaison du titre ignore la casse, les accents et les espaces superflus.
    @ParameterizedTest
    @ValueSource(strings = {
            "La Lettre à Élise",
            "la lettre a elise",
            "LA LETTRE A ELISE",
            "  la   lettre   a   elise  ",
            "La lettre à Elise"
    })
    void accepte_les_variantes_de_casse_daccents_et_despaces(String proposition) {
        BlindTest blindTest = avecLaMain("La Lettre à Élise");

        assertTrue(faireUnePropositionUseCase.apply(blindTest, alice, proposition));
    }

    @ParameterizedTest
    @ValueSource(strings = {"La lettre", "Clair de Lune", "lalettreaelise"})
    void refuse_un_titre_different(String proposition) {
        BlindTest blindTest = avecLaMain("La Lettre à Élise");

        assertFalse(faireUnePropositionUseCase.apply(blindTest, alice, proposition));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Coeur de pirate", "Cœur de pirate", "CŒUR DE PIRATE"})
    void traite_les_ligatures(String proposition) {
        BlindTest blindTest = avecLaMain("Cœur de pirate");

        assertTrue(faireUnePropositionUseCase.apply(blindTest, alice, proposition));
    }

    private BlindTest avecLaMain(String premierTitre) {
        BlindTest blindTest = blindTestEnCours(premierTitre);
        blindTest.setReservataire(alice);
        blindTest.setEtatLecture(EtatLecture.PAUSE);
        when(output.findBlindTest(any())).thenReturn(blindTest);
        return blindTest;
    }

    private static int scoreDe(BlindTest blindTest, Participant participant) {
        return blindTest.getParticipations().stream()
                .filter(participation -> participation.getParticipant().getEmail()
                        .equals(participant.getEmail()))
                .findFirst()
                .orElseThrow()
                .getScore();
    }

    /**
     * Un blind test demarre, avec alice, bob et carole, dont le morceau courant porte
     * le titre demande.
     */
    static BlindTest blindTestEnCours(String premierTitre) {
        BlindTest blindTest = new BlindTest("Soiree ESGI");

        List<Morceau> morceaux = new ArrayList<>();
        morceaux.add(new Morceau(premierTitre));
        for (int numero = 2; numero <= BlindTest.NOMBRE_DE_MORCEAUX; numero++) {
            morceaux.add(new Morceau("Morceau " + numero));
        }
        blindTest.setMorceaux(morceaux);

        blindTest.getParticipations().add(new Participation(participant("alice@esgi.fr")));
        blindTest.getParticipations().add(new Participation(participant("bob@esgi.fr")));
        blindTest.getParticipations().add(new Participation(participant("carole@esgi.fr")));

        blindTest.setStatut(StatutBlindTest.EN_COURS);
        blindTest.setEtatLecture(EtatLecture.LECTURE);
        return blindTest;
    }
}
