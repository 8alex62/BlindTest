package com.esgi.blindTest.domain.model;

/**
 * Cycle de vie d'un blind test.
 */
public enum StatutBlindTest {

    /** Ouvert aux inscriptions, il manque encore des participants. */
    EN_ATTENTE,

    /** Les trois participants sont là, la séquence de morceaux est jouée. */
    EN_COURS,

    /** Les sept morceaux sont passés, les scores sont figés. */
    TERMINE
}
