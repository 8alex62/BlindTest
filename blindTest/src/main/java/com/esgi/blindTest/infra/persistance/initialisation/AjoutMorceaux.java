package com.esgi.blindTest.infra.persistance.initialisation;

import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.infra.persistance.entity.MorceauEntity;
import com.esgi.blindTest.infra.persistance.initialisation.CatalogueDeezer.MorceauTrouve;
import com.esgi.blindTest.infra.persistance.repository.MorceauJpaRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Jeu de sept morceaux insere au demarrage. Les titres et les extraits audio viennent
 * de Deezer ; si l'API est injoignable ou desactivee, on retombe sur un titre en dur
 * et un fichier de demonstration, pour que le catalogue compte toujours sept morceaux.
 */
@Component
@AllArgsConstructor
public class AjoutMorceaux {

    private static final Logger JOURNAL = LoggerFactory.getLogger(AjoutMorceaux.class);

    private static final String AUDIO_DE_SECOURS =
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-";

    private static final List<Choix> CATALOGUE = List.of(
            new Choix("Queen Bohemian Rhapsody", "Bohemian Rhapsody"),
            new Choix("Daft Punk Get Lucky", "Get Lucky"),
            new Choix("Michael Jackson Billie Jean", "Billie Jean"),
            new Choix("Stromae Alors on danse", "Alors on danse"),
            new Choix("Adele Rolling in the Deep", "Rolling in the Deep"),
            new Choix("Nirvana Smells Like Teen Spirit", "Smells Like Teen Spirit"),
            new Choix("Edith Piaf La Vie en rose", "La Vie en Rose"));

    private final MorceauJpaRepository morceauJpaRepository;
    private final CatalogueDeezer catalogueDeezer;

    @EventListener(ApplicationReadyEvent.class)
    // Le catalogue doit exister avant AjoutParticipants et AjoutBlindTest.
    @Order(1)
    public void init() {
        for (int rang = 0; rang < CATALOGUE.size(); rang++) {
            Choix choix = CATALOGUE.get(rang);
            String audioDeSecours = AUDIO_DE_SECOURS + (rang + 1) + ".mp3";

            MorceauTrouve morceau = catalogueDeezer.chercher(choix.requete())
                    .orElseGet(() -> new MorceauTrouve(choix.titreDeSecours(), audioDeSecours));

            ajouter(morceau.titre(), morceau.urlAudio());
        }

        long total = morceauJpaRepository.count();
        if (total < BlindTest.NOMBRE_DE_MORCEAUX) {
            JOURNAL.warn("Le catalogue ne compte que {} morceaux sur {} : aucun blind test"
                    + " ne pourra etre cree.", total, BlindTest.NOMBRE_DE_MORCEAUX);
        }
    }

    private void ajouter(String nom, String urlAudio) {
        if (morceauJpaRepository.findByNom(nom).isEmpty()) {
            morceauJpaRepository.save(new MorceauEntity(nom, urlAudio));
        }
    }

    private record Choix(String requete, String titreDeSecours) {
    }
}
