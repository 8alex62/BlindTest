package com.esgi.blindTest.domain.model;

import com.esgi.blindTest.domain.exception.BlindTestCompletException;
import com.esgi.blindTest.domain.exception.BlindTestIncompletException;
import com.esgi.blindTest.domain.exception.NombreDeMorceauxInvalideException;
import com.esgi.blindTest.domain.exception.ReponseDejaReserveeException;
import com.esgi.blindTest.domain.exception.ReponseNonReserveeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests du domaine : aucune dependance a Spring, a JPA ni a la base.
 */
class BlindTestTest {

    private BlindTest blindTest;
    private Participant alice;
    private Participant bob;
    private Participant carole;

    @BeforeEach
    void setUp() {
        blindTest = new BlindTest("Soiree ESGI");
        blindTest.ajouterLesMorceaux(septMorceaux());
        alice = participant(1L, "alice@esgi.fr");
        bob = participant(2L, "bob@esgi.fr");
        carole = participant(3L, "carole@esgi.fr");
    }

    // ------------------------------------------------------------- participants

    @Test
    void refuse_un_quatrieme_participant() {
        blindTest.rejoindre(alice);
        blindTest.rejoindre(bob);
        blindTest.rejoindre(carole);

        Participant david = participant(4L, "david@esgi.fr");
        assertThrows(BlindTestCompletException.class, () -> blindTest.rejoindre(david));
        assertEquals(3, blindTest.getParticipations().size());
    }

    @Test
    void demarre_automatiquement_au_troisieme_participant() {
        blindTest.rejoindre(alice);
        blindTest.rejoindre(bob);
        assertEquals(StatutBlindTest.EN_ATTENTE, blindTest.getStatut());

        blindTest.rejoindre(carole);

        assertEquals(StatutBlindTest.EN_COURS, blindTest.getStatut());
        assertEquals(EtatLecture.LECTURE, blindTest.getEtatLecture());
        assertEquals("Morceau 1", blindTest.morceauCourant().getNom());
    }

    @Test
    void refuse_de_demarrer_avec_moins_de_trois_participants() {
        blindTest.rejoindre(alice);
        assertThrows(BlindTestIncompletException.class, () -> blindTest.demarrer());
    }

    // ------------------------------------------------------------- morceaux

    @Test
    void exige_exactement_sept_morceaux() {
        BlindTest autre = new BlindTest("Trop court");
        List<Morceau> six = septMorceaux().subList(0, 6);

        assertThrows(NombreDeMorceauxInvalideException.class, () -> autre.ajouterLesMorceaux(six));
    }

    // ------------------------------------------------------------- premier clic

    @Test
    void le_premier_clic_met_en_pause_et_reserve_la_reponse() {
        demarrer();

        blindTest.reserverLaReponse(bob);

        assertEquals(EtatLecture.PAUSE, blindTest.getEtatLecture());
        assertEquals(bob.getId(), blindTest.getIdParticipantReservataire());
    }

    @Test
    void refuse_le_second_clic_sur_le_meme_morceau() {
        demarrer();
        blindTest.reserverLaReponse(bob);

        ReponseDejaReserveeException erreur = assertThrows(ReponseDejaReserveeException.class,
                () -> blindTest.reserverLaReponse(carole));

        assertTrue(erreur.getMessage().contains("deja pris la main")
                || erreur.getMessage().contains("déjà pris la main"));
        assertEquals(bob.getId(), blindTest.getIdParticipantReservataire());
    }

    @Test
    void seul_le_reservataire_peut_repondre() {
        demarrer();
        blindTest.reserverLaReponse(bob);

        assertThrows(ReponseNonReserveeException.class,
                () -> blindTest.repondre(carole, "Morceau 1"));
    }

    // ------------------------------------------------------------- propositions

    @Test
    void une_bonne_reponse_rapporte_un_point_et_passe_au_morceau_suivant() {
        demarrer();
        blindTest.reserverLaReponse(bob);

        boolean juste = blindTest.repondre(bob, "  MORCEAU 1 ");

        assertTrue(juste);
        assertEquals(1, blindTest.participationDe(bob).getScore());
        assertEquals("Morceau 2", blindTest.morceauCourant().getNom());
        assertEquals(EtatLecture.LECTURE, blindTest.getEtatLecture());
        assertNull(blindTest.getIdParticipantReservataire());
    }

    @Test
    void une_mauvaise_reponse_relance_la_lecture_sans_changer_de_morceau() {
        demarrer();
        blindTest.reserverLaReponse(bob);

        boolean juste = blindTest.repondre(bob, "Une autre chanson");

        assertFalse(juste);
        assertEquals(0, blindTest.participationDe(bob).getScore());
        assertEquals("Morceau 1", blindTest.morceauCourant().getNom());
        assertEquals(EtatLecture.LECTURE, blindTest.getEtatLecture());
        assertNull(blindTest.getIdParticipantReservataire());
    }

    @Test
    void le_participant_qui_sest_trompe_peut_recliquer() {
        demarrer();
        blindTest.reserverLaReponse(bob);
        blindTest.repondre(bob, "Faux");

        blindTest.reserverLaReponse(bob);

        assertEquals(bob.getId(), blindTest.getIdParticipantReservataire());
    }

    // ------------------------------------------------------------- fin de partie

    @Test
    void se_termine_apres_sept_morceaux_et_fige_les_scores() {
        demarrer();

        for (int numero = 1; numero <= BlindTest.NOMBRE_DE_MORCEAUX; numero++) {
            blindTest.reserverLaReponse(alice);
            assertTrue(blindTest.repondre(alice, "Morceau " + numero));
        }

        assertTrue(blindTest.estTermine());
        assertEquals(StatutBlindTest.TERMINE, blindTest.getStatut());
        assertEquals(EtatLecture.PAUSE, blindTest.getEtatLecture());
        assertEquals(7, blindTest.participationDe(alice).getScore());
        assertNull(blindTest.morceauCourant());
    }

    @Test
    void le_classement_trie_par_score_decroissant() {
        demarrer();
        blindTest.reserverLaReponse(carole);
        blindTest.repondre(carole, "Morceau 1");

        List<Participation> classement = blindTest.classement();

        assertEquals(carole.getEmail(), classement.get(0).getParticipant().getEmail());
        assertEquals(1, classement.get(0).getScore());
    }

    // ------------------------------------------------------------- utilitaires

    private void demarrer() {
        blindTest.rejoindre(alice);
        blindTest.rejoindre(bob);
        blindTest.rejoindre(carole);
    }

    private static List<Morceau> septMorceaux() {
        return IntStream.rangeClosed(1, BlindTest.NOMBRE_DE_MORCEAUX)
                .mapToObj(numero -> new Morceau("Morceau " + numero))
                .toList();
    }

    private static Participant participant(Long id, String email) {
        Participant participant = new Participant(email, "motdepasse");
        participant.setId(id);
        return participant;
    }
}
