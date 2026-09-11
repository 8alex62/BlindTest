package com.esgi.blindTest.domain.repository;

import com.esgi.blindTest.domain.model.BlindTest;

import java.util.List;
import java.util.Optional;

/**
 * Port de persistance des blind tests.
 */
public interface BlindTestRepository {

    BlindTest save(BlindTest blindTest);

    Optional<BlindTest> findById(Long id);

    List<BlindTest> findEnCours();

    /**
     * Arbitrage atomique du premier clic, par mise a jour conditionnelle en base.
     * La regle metier reste portee par BlindTest.reserverLaReponse : la base
     * n arbitre pas la regle, elle arbitre l ordre d arrivee.
     *
     * @return vrai si c est bien cet appel qui a pris la main
     */
    boolean reserverLaReponse(BlindTest blindTest);
}
