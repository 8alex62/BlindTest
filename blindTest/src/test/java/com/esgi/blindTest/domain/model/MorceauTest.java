package com.esgi.blindTest.domain.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * La comparaison du titre ignore la casse, les accents et les espaces superflus.
 */
class MorceauTest {

    private final Morceau morceau = new Morceau("La Lettre à Élise");

    @ParameterizedTest
    @ValueSource(strings = {
            "La Lettre à Élise",
            "la lettre a elise",
            "LA LETTRE A ELISE",
            "  la   lettre   a   elise  ",
            "La lettre à Elise"
    })
    void accepte_les_variantes_de_casse_daccents_et_despaces(String proposition) {
        assertTrue(morceau.correspondA(proposition));
    }

    @ParameterizedTest
    @ValueSource(strings = {"La lettre", "Clair de Lune", "lalettreaelise", ""})
    void refuse_un_titre_different(String proposition) {
        assertFalse(morceau.correspondA(proposition));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Coeur de pirate", "Cœur de pirate", "CŒUR DE PIRATE"})
    void traite_les_ligatures(String proposition) {
        assertTrue(new Morceau("Cœur de pirate").correspondA(proposition));
    }
}
