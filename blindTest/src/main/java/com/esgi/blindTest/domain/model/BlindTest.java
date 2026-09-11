package com.esgi.blindTest.domain.model;

import com.esgi.blindTest.domain.exception.BlindTestCompletException;
import com.esgi.blindTest.domain.exception.BlindTestDejaDemarreException;
import com.esgi.blindTest.domain.exception.BlindTestIncompletException;
import com.esgi.blindTest.domain.exception.BlindTestNonDemarreException;
import com.esgi.blindTest.domain.exception.BlindTestTermineException;
import com.esgi.blindTest.domain.exception.NombreDeMorceauxInvalideException;
import com.esgi.blindTest.domain.exception.ParticipantDejaPresentException;
import com.esgi.blindTest.domain.exception.ParticipantHorsBlindTestException;
import com.esgi.blindTest.domain.exception.PropositionVideException;
import com.esgi.blindTest.domain.exception.ReponseDejaReserveeException;
import com.esgi.blindTest.domain.exception.ReponseNonReserveeException;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Racine du domaine : toutes les règles du jeu sont portées ici, jamais par un service,
 * un adapter ou un contrôleur.
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class BlindTest {

    public static final int NOMBRE_MAXIMUM_DE_PARTICIPANTS = 3;
    public static final int NOMBRE_DE_MORCEAUX = 7;

    // Champ technique : identité persistante, utilisée dans les URL REST.
    private Long id;

    @NonNull
    private String nom;

    private List<Participation> participations = new ArrayList<>();

    private List<Morceau> morceaux = new ArrayList<>();

    // Champ technique : distingue « en attente », « en cours » et « terminé ».
    private StatutBlindTest statut = StatutBlindTest.EN_ATTENTE;

    // Champ technique : position dans la séquence des sept morceaux.
    private int indexMorceauCourant;

    // Champ technique : indique au navigateur de jouer ou de mettre en pause.
    private EtatLecture etatLecture = EtatLecture.PAUSE;

    // Champ technique : identifiant du participant qui a gagné le premier clic.
    private Long idParticipantReservataire;

    // Champ technique : jeton de verrou optimiste, transporté entre la base et le domaine.
    private Long version;

    // ------------------------------------------------------------------ commandes

    /**
     * Règle métier : un blind test est une séquence de sept morceaux.
     */
    public void ajouterLesMorceaux(List<Morceau> morceaux) {
        int nombre = morceaux == null ? 0 : morceaux.size();
        if (nombre != NOMBRE_DE_MORCEAUX) {
            throw new NombreDeMorceauxInvalideException(nombre);
        }
        this.morceaux = new ArrayList<>(morceaux);
    }

    /**
     * Règles métier : un blind test accueille au maximum trois participants,
     * et il démarre dès qu'il atteint ce nombre.
     */
    public void rejoindre(Participant participant) {
        if (statut == StatutBlindTest.TERMINE) {
            throw new BlindTestTermineException();
        }
        if (statut != StatutBlindTest.EN_ATTENTE) {
            throw new BlindTestDejaDemarreException();
        }
        if (estInscrit(participant)) {
            throw new ParticipantDejaPresentException();
        }
        if (participations.size() >= NOMBRE_MAXIMUM_DE_PARTICIPANTS) {
            throw new BlindTestCompletException();
        }
        participations.add(new Participation(participant));
        if (estComplet()) {
            demarrer();
        }
    }

    /**
     * Règle métier : au démarrage, le premier morceau est joué.
     */
    public void demarrer() {
        if (statut == StatutBlindTest.TERMINE) {
            throw new BlindTestTermineException();
        }
        if (statut == StatutBlindTest.EN_COURS) {
            throw new BlindTestDejaDemarreException();
        }
        if (!estComplet()) {
            throw new BlindTestIncompletException();
        }
        if (morceaux.size() != NOMBRE_DE_MORCEAUX) {
            throw new NombreDeMorceauxInvalideException(morceaux.size());
        }
        statut = StatutBlindTest.EN_COURS;
        indexMorceauCourant = 0;
        etatLecture = EtatLecture.LECTURE;
        idParticipantReservataire = null;
    }

    /**
     * Règle métier : le premier clic sur "J'ai trouvé" met la lecture en pause et réserve
     * la réponse à ce participant. Les clics suivants sur le même morceau sont refusés.
     */
    public void reserverLaReponse(Participant participant) {
        if (statut == StatutBlindTest.TERMINE) {
            throw new BlindTestTermineException();
        }
        if (statut != StatutBlindTest.EN_COURS) {
            throw new BlindTestNonDemarreException();
        }
        if (!estInscrit(participant)) {
            throw new ParticipantHorsBlindTestException();
        }
        if (idParticipantReservataire != null) {
            throw new ReponseDejaReserveeException();
        }
        etatLecture = EtatLecture.PAUSE;
        idParticipantReservataire = participant.getId();
    }

    /**
     * Libère la main et relance la lecture pour tout le monde.
     */
    public void annulerLaReservation() {
        idParticipantReservataire = null;
        if (statut == StatutBlindTest.EN_COURS) {
            etatLecture = EtatLecture.LECTURE;
        }
    }

    /**
     * Règles métier : seul le réservataire peut répondre ; une bonne réponse rapporte
     * un point et fait passer au morceau suivant ; une mauvaise réponse relance la
     * lecture pour tout le monde.
     *
     * @return vrai si la proposition est juste
     */
    public boolean repondre(Participant participant, String proposition) {
        if (statut == StatutBlindTest.TERMINE) {
            throw new BlindTestTermineException();
        }
        if (statut != StatutBlindTest.EN_COURS) {
            throw new BlindTestNonDemarreException();
        }
        if (idParticipantReservataire == null
                || participant == null
                || !idParticipantReservataire.equals(participant.getId())) {
            throw new ReponseNonReserveeException();
        }
        if (proposition == null || proposition.isBlank()) {
            throw new PropositionVideException();
        }

        boolean juste = morceauCourant().correspondA(proposition);
        if (juste) {
            participationDe(participant).ajouterUnPoint();
            passerAuMorceauSuivant();
        } else {
            annulerLaReservation();
        }
        return juste;
    }

    /**
     * Règle métier : quand les sept morceaux sont passés, le blind test est terminé
     * et les scores sont figés.
     */
    private void passerAuMorceauSuivant() {
        idParticipantReservataire = null;
        indexMorceauCourant++;
        if (indexMorceauCourant >= NOMBRE_DE_MORCEAUX) {
            statut = StatutBlindTest.TERMINE;
            etatLecture = EtatLecture.PAUSE;
            indexMorceauCourant = NOMBRE_DE_MORCEAUX;
        } else {
            etatLecture = EtatLecture.LECTURE;
        }
    }

    // ------------------------------------------------------------------ requêtes

    public Morceau morceauCourant() {
        if (statut != StatutBlindTest.EN_COURS || indexMorceauCourant >= morceaux.size()) {
            return null;
        }
        return morceaux.get(indexMorceauCourant);
    }

    public int numeroDuMorceauCourant() {
        return Math.min(indexMorceauCourant + 1, NOMBRE_DE_MORCEAUX);
    }

    public boolean estComplet() {
        return participations.size() == NOMBRE_MAXIMUM_DE_PARTICIPANTS;
    }

    public boolean estTermine() {
        return statut == StatutBlindTest.TERMINE;
    }

    public boolean estInscrit(Participant participant) {
        return participations.stream().anyMatch(participation -> participation.concerne(participant));
    }

    public Participation participationDe(Participant participant) {
        return participations.stream()
                .filter(participation -> participation.concerne(participant))
                .findFirst()
                .orElseThrow(ParticipantHorsBlindTestException::new);
    }

    public List<Participation> classement() {
        return participations.stream()
                .sorted(Comparator.comparingInt(Participation::getScore).reversed())
                .toList();
    }
}
