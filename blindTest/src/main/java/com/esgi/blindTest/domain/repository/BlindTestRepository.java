package com.esgi.blindTest.domain.repository;

import com.esgi.blindTest.domain.model.BlindTest;

import java.util.List;
import java.util.Optional;

/**
 * Port de persistance des blind tests. Un blind test est designe par son nom,
 * comme l'enchere est designee par le sien dans le projet de reference.
 */
public interface BlindTestRepository {

    BlindTest save(BlindTest blindTest);

    Optional<BlindTest> findByNom(String nom);

    List<BlindTest> findEnCours();

    /**
     * Arbitrage atomique du premier clic, par mise a jour conditionnelle en base.
     * La regle metier reste appliquee par MettreEnPauseBlindTestUseCase : la base
     * n arbitre pas la regle, elle arbitre l ordre d arrivee.
     *
     * @return vrai si c est bien cet appel qui a pris la main
     */
    boolean reserverLaReponse(BlindTest blindTest);
}
