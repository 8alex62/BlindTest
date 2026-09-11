package com.esgi.blindTest.domain.usecase;

import com.esgi.blindTest.domain.exception.BlindTestNonDemarreException;
import com.esgi.blindTest.domain.exception.BlindTestTermineException;
import com.esgi.blindTest.domain.exception.ParticipantHorsBlindTestException;
import com.esgi.blindTest.domain.exception.PropositionVideException;
import com.esgi.blindTest.domain.exception.ReponseNonReserveeException;
import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.domain.model.EtatLecture;
import com.esgi.blindTest.domain.model.Morceau;
import com.esgi.blindTest.domain.model.Participant;
import com.esgi.blindTest.domain.model.Participation;
import com.esgi.blindTest.domain.model.StatutBlindTest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
@AllArgsConstructor
public class FaireUnePropositionUseCase {

    private static final Pattern DIACRITIQUES = Pattern.compile("\\p{M}+");
    private static final Pattern ESPACES = Pattern.compile("\\s+");

    public interface OutputPort {
        Participant findParticipant(Participant participant);

        BlindTest findBlindTest(BlindTest blindTest);

        void ajouterUnPoint(BlindTest blindTest, Participation participation);

        BlindTest save(BlindTest blindTest);
    }

    private final OutputPort output;

    /**
     * @return vrai si la proposition est juste
     */
    public boolean apply(BlindTest blindtest, Participant participant, String proposition) {
        Participant inscrit = output.findParticipant(participant);
        BlindTest courant = output.findBlindTest(blindtest);

        if (courant.getStatut() == StatutBlindTest.TERMINE) {
            throw new BlindTestTermineException();
        }
        if (courant.getStatut() != StatutBlindTest.EN_COURS) {
            throw new BlindTestNonDemarreException();
        }
        // Regle metier : seul le reservataire peut repondre.
        if (courant.getReservataire() == null
                || !courant.getReservataire().getEmail().equals(inscrit.getEmail())) {
            throw new ReponseNonReserveeException();
        }
        if (proposition == null || proposition.isBlank()) {
            throw new PropositionVideException();
        }

        Morceau morceauCourant = courant.getMorceaux().get(courant.getIndexMorceauCourant());
        boolean juste = normaliser(proposition).equals(normaliser(morceauCourant.getNom()));

        if (juste) {
            // Regle metier : une bonne reponse rapporte un point, puis on passe au suivant.
            Participation participation = participationDe(courant, inscrit);
            participation.setScore(participation.getScore() + 1);
            passerAuMorceauSuivant(courant);
            output.ajouterUnPoint(courant, participation);
        } else {
            // Regle metier : une mauvaise reponse relance la lecture pour tout le monde.
            courant.setReservataire(null);
            courant.setEtatLecture(EtatLecture.LECTURE);
        }

        output.save(courant);
        return juste;
    }

    /**
     * Regle metier : quand les sept morceaux sont passes, le blind test est termine
     * et les scores sont figes.
     */
    private static void passerAuMorceauSuivant(BlindTest blindTest) {
        blindTest.setReservataire(null);
        blindTest.setIndexMorceauCourant(blindTest.getIndexMorceauCourant() + 1);
        if (blindTest.getIndexMorceauCourant() >= BlindTest.NOMBRE_DE_MORCEAUX) {
            blindTest.setStatut(StatutBlindTest.TERMINE);
            blindTest.setEtatLecture(EtatLecture.PAUSE);
            blindTest.setIndexMorceauCourant(BlindTest.NOMBRE_DE_MORCEAUX);
        } else {
            blindTest.setEtatLecture(EtatLecture.LECTURE);
        }
    }

    private static Participation participationDe(BlindTest blindTest, Participant participant) {
        return blindTest.getParticipations().stream()
                .filter(participation -> participation.getParticipant().getEmail()
                        .equals(participant.getEmail()))
                .findFirst()
                .orElseThrow(ParticipantHorsBlindTestException::new);
    }

    /**
     * Regle metier : la comparaison du titre ignore la casse, les accents
     * et les espaces superflus.
     */
    private static String normaliser(String valeur) {
        if (valeur == null) {
            return "";
        }
        String sansLigature = valeur
                .replace('\u2019', '\'')
                .replace("\u0152", "OE").replace("\u0153", "oe")
                .replace("\u00C6", "AE").replace("\u00E6", "ae");
        String sansAccent = DIACRITIQUES
                .matcher(Normalizer.normalize(sansLigature, Normalizer.Form.NFD))
                .replaceAll("");
        return ESPACES.matcher(sansAccent.trim()).replaceAll(" ").toLowerCase(Locale.ROOT);
    }
}
