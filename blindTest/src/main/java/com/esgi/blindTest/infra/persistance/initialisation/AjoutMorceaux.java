package com.esgi.blindTest.infra.persistance.initialisation;

import com.esgi.blindTest.infra.persistance.entity.MorceauEntity;
import com.esgi.blindTest.infra.persistance.repository.MorceauJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Jeu de sept morceaux insere au demarrage. Les adresses audio sont des fichiers
 * de demonstration libres, a remplacer par des fichiers locaux sous static/audio.
 */
@Component
@AllArgsConstructor
public class AjoutMorceaux {

    private static final String BASE_AUDIO = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-";

    private final MorceauJpaRepository morceauJpaRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        ajouter("La Lettre a Elise", 1);
        ajouter("Clair de Lune", 2);
        ajouter("Les Quatre Saisons", 3);
        ajouter("La Marche Turque", 4);
        ajouter("Bolero", 5);
        ajouter("La Symphonie du Nouveau Monde", 6);
        ajouter("Le Canon de Pachelbel", 7);
    }

    private void ajouter(String nom, int numeroDuFichier) {
        if (morceauJpaRepository.findByNom(nom).isEmpty()) {
            morceauJpaRepository.save(new MorceauEntity(nom, BASE_AUDIO + numeroDuFichier + ".mp3"));
        }
    }
}
