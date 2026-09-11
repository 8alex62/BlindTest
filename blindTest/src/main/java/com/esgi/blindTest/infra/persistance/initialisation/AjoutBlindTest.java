package com.esgi.blindTest.infra.persistance.initialisation;

import com.esgi.blindTest.domain.model.BlindTest;
import com.esgi.blindTest.infra.persistance.entity.BlindTestEntity;
import com.esgi.blindTest.infra.persistance.entity.MorceauEntity;
import com.esgi.blindTest.infra.persistance.repository.BlindTestJpaRepository;
import com.esgi.blindTest.infra.persistance.repository.MorceauJpaRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Un blind test de demonstration, deja pourvu de ses sept morceaux, pret a etre rejoint.
 *
 * Il est cree en attente et sans participant : le demarrage reste manuel, et il faut donc
 * trois participants puis un clic sur "Lancer le blind test".
 *
 * S'execute apres AjoutMorceaux, dont il consomme le catalogue : d'ou l'ordre explicite.
 */
@Component
@AllArgsConstructor
public class AjoutBlindTest {

    private static final Logger JOURNAL = LoggerFactory.getLogger(AjoutBlindTest.class);

    private static final String NOM = "Blind Test n°1";

    private final BlindTestJpaRepository blindTestJpaRepository;
    private final MorceauJpaRepository morceauJpaRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Order(3)
    // Les morceaux sont lus et rattaches dans la meme transaction, pour qu'ils soient geres
    // par Hibernate au moment d'ecrire la table de jointure.
    @Transactional
    public void init() {
        if (blindTestJpaRepository.findByNom(NOM).isPresent()) {
            return;
        }

        List<MorceauEntity> catalogue = morceauJpaRepository.findAll();
        if (catalogue.size() < BlindTest.NOMBRE_DE_MORCEAUX) {
            JOURNAL.warn("Catalogue incomplet ({} morceaux sur {}) : « {} » n'est pas cree.",
                    catalogue.size(), BlindTest.NOMBRE_DE_MORCEAUX, NOM);
            return;
        }

        BlindTestEntity blindTest = new BlindTestEntity();
        blindTest.setNom(NOM);
        blindTest.setMorceaux(
                new ArrayList<>(catalogue.subList(0, BlindTest.NOMBRE_DE_MORCEAUX)));

        blindTestJpaRepository.save(blindTest);
        JOURNAL.info("Blind test de demonstration cree : {}", NOM);
    }
}
