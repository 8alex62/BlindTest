package com.esgi.blindTest.infra.persistance.initialisation;

import com.esgi.blindTest.infra.persistance.entity.ParticipantEntity;
import com.esgi.blindTest.infra.persistance.repository.ParticipantJpaRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Deux comptes de demonstration, pour eviter de repasser par le formulaire d'inscription
 * a chaque demarrage. Le mot de passe respecte la regle des huit caracteres minimum.
 *
 * Comme dans le reste du projet, il est stocke en clair : c'est la convention du projet
 * de reference, a ne pas reproduire en production.
 */
@Component
@AllArgsConstructor
public class AjoutParticipants {

    private static final Logger JOURNAL = LoggerFactory.getLogger(AjoutParticipants.class);

    private static final String MOT_DE_PASSE = "12345678";

    private static final List<String> EMAILS = List.of("user1@test.fr", "user2@test.fr");

    private final ParticipantJpaRepository participantJpaRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Order(2)
    public void init() {
        EMAILS.forEach(this::ajouter);
    }

    private void ajouter(String email) {
        if (participantJpaRepository.findByEmail(email).isEmpty()) {
            participantJpaRepository.save(new ParticipantEntity(email, MOT_DE_PASSE));
            JOURNAL.info("Compte de demonstration cree : {}", email);
        }
    }
}
